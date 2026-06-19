package com.tenny.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.common.ChatConstants;
import com.tenny.common.UserContext;
import com.tenny.entity.Conversation;
import com.tenny.entity.Message;
import com.tenny.entity.dto.ConversationReq;
import com.tenny.enums.MessageRole;
import com.tenny.mapper.ConversationMapper;
import com.tenny.service.ConversationService;
import com.tenny.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final CompiledGraph compiledGraph;
    private final ChatClient chatClient;
    private final RedissonClient redissonClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final MessageService messageService;

    public ConversationServiceImpl(CompiledGraph compiledGraph, ChatModel chatModel, RedissonClient redissonClient, MessageService messageService) {
        this.compiledGraph = compiledGraph;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.redissonClient = redissonClient;
        this.messageService = messageService;
    }

    @Override
    public Conversation create() {
        Conversation conversation = new Conversation();
        conversation.setUserId(UserContext.getUserId());
        String conversationId = UUID.randomUUID().toString();
        conversation.setConversationId(conversationId);
        conversation.setTitle(ChatConstants.DEFAULT_TITLE);
        conversation.setStatus("ACTIVE");
        conversation.setMessageCount(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        save(conversation);
        return conversation;
    }

    @Override
    public void generateTitleAsync(String conversationId, Long userId, String firstQuery) {
        Conversation conversation = this.getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId));
        if (conversation == null || !ChatConstants.DEFAULT_TITLE.equals(conversation.getTitle())) {
            return;
        }
        executor.submit(() -> {
            try {
                String title = generateTitleByAI(firstQuery);
                update(new LambdaUpdateWrapper<Conversation>().eq(Conversation::getUserId, userId).eq(Conversation::getConversationId, conversationId).set(Conversation::getTitle, title));
                log.info("会话标题生成成功: {} -> {}", conversationId, title);
            } catch (Exception e) {
                log.error("生成标题失败: {}", e.getMessage());
            }
        });
    }

    private String generateTitleByAI(String firstQuery) {
        String prompt = """
            请把下面用户的问题，总结成一个简短的标题（不超过10个字）。
            只输出标题，不要输出其他任何内容。
            
            用户问题：%s
            """.formatted(firstQuery);

        String title = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 清理标题（去除引号、换行等）
        assert title != null;
        title = title.trim()
                .replaceAll("^[\"']|[\"']$", "")  // 去掉首尾引号
                .replaceAll("[\n\r]", "");         // 去掉换行

        // 限制长度
        if (title.length() > 20) {
            title = title.substring(0, 20) + "...";
        }

        return title;
    }

    @Override
    public Flux<String> chat(ConversationReq req) {
        assert req.getConversationId() != null;

        // 流开始前：先存用户消息到 MySQL
        Message userMsg = new Message();
        userMsg.setUserId(UserContext.getUserId());
        userMsg.setConversationId(req.getConversationId());
        userMsg.setRole(MessageRole.USER.name());
        userMsg.setContent(req.getMessage());
        userMsg.setCreatedAt(LocalDateTime.now());
        messageService.save(userMsg);
        // 更新会话消息数+1
        lambdaUpdate()
                .eq(Conversation::getUserId, UserContext.getUserId())
                .eq(Conversation::getConversationId, req.getConversationId())
                .setSql("message_count = message_count + 1")
                .update();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(req.getConversationId())
                .build();

        StringBuilder fullResponse = new StringBuilder();

        List<Message> messageList = getMessageList(req.getConversationId());
        List<org.springframework.ai.chat.messages.Message> historyMessages = messageList.stream()
                .map(msg -> {
                    if (MessageRole.USER.name().equals(msg.getRole())) {
                        return new UserMessage(msg.getContent());
                    } else {
                        return new AssistantMessage(msg.getContent());
                    }
                })
                .collect(Collectors.toList());

        return compiledGraph.stream(Map.of(
                "message", req.getMessage(),
                "messages", historyMessages,
                "webSearchEnabled", req.getWebSearchEnabled() != null && req.getWebSearchEnabled()
                ), config)
                .ofType(StreamingOutput.class)
                .map(so -> {
                    Object data = so.getOriginData();
                    if (data instanceof String text) {
//                        log.info("text: {}", text);
                        fullResponse.append(text);
                        return text;
                    }
                    return "";
                })
                .doFinally(signalType -> {
                    if (!fullResponse.isEmpty()) {
//                        log.info("assistant: {}", fullResponse.toString());
                        // 流结束后：存 AI 回复到 MySQL
                        Message assistantMsg = new Message();
                        assistantMsg.setUserId(userMsg.getUserId());
                        assistantMsg.setConversationId(req.getConversationId());
                        assistantMsg.setRole(MessageRole.ASSISTANT.name());
                        assistantMsg.setContent(fullResponse.toString());
                        assistantMsg.setCreatedAt(LocalDateTime.now());
                        messageService.save(assistantMsg);
                        // 更新会话消息数+1
                        lambdaUpdate()
                                .eq(Conversation::getUserId, userMsg.getUserId())
                                .eq(Conversation::getConversationId, req.getConversationId())
                                .setSql("message_count = message_count + 1")
                                .update();
                    }
                });
    }

    @Override
    public List<Conversation> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdatedAt)
                .list();
    }

    @Override
    public Conversation getByConversationId(String conversationId) {
        return getOne(new  LambdaQueryWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId));
    }

    @Override
    public Map<String, Object> getMessages(String conversationId) {
        List<Message> msgList = getMessageList(conversationId);
        return Map.of("messages", msgList);
    }

    private List<Message> getMessageList(String conversationId) {
        return messageService.list(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getUserId, UserContext.getUserId())
                        .eq(Message::getConversationId, conversationId)
                        .orderByAsc(Message::getCreatedAt)
        );
    }

    @Override
    @Transactional
    public void deleteByConversationId(String conversationId) {
        // 1. 删 MySQL
        messageService.remove(new LambdaQueryWrapper<Message>().eq(Message::getUserId, UserContext.getUserId()).eq(Message::getConversationId, conversationId));
        remove(new LambdaQueryWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId));

        // 2. 删 Redis checkpoint
        String metaKey = "graph:thread:meta:" + conversationId;

        // 用 Redisson RMap 读取 meta 内容
        RMap<String, String> meta = redissonClient.getMap(metaKey);
        String checkpointId = meta.get("thread_id");

        if (checkpointId != null) {
            redissonClient.getKeys().delete(
                    "graph:checkpoint:content:" + checkpointId,
                    "graph:thread:reverse:" + checkpointId
            );
        }

        redissonClient.getKeys().delete(metaKey);
    }

    @Override
    public void rename(String conversationId, String newTitle) {
        update(new LambdaUpdateWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId).set(Conversation::getTitle, newTitle));
    }

}
