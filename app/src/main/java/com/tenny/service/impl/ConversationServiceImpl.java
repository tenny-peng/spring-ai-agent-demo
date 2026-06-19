package com.tenny.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.common.ChatConstants;
import com.tenny.common.UserContext;
import com.tenny.entity.Conversation;
import com.tenny.entity.dto.ConversationReq;
import com.tenny.mapper.ConversationMapper;
import com.tenny.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final CompiledGraph compiledGraph;
    private final ChatClient chatClient;
    private final RedissonClient redissonClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public ConversationServiceImpl(CompiledGraph compiledGraph, ChatModel chatModel, RedissonClient redissonClient) {
        this.compiledGraph = compiledGraph;
        this.chatClient = ChatClient.builder(chatModel).build();
        this.redissonClient = redissonClient;
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

        RunnableConfig config = RunnableConfig.builder()
                .threadId(req.getConversationId())
                .build();

        StringBuilder fullResponse = new StringBuilder();

        return compiledGraph.stream(Map.of(
                "message", req.getMessage(),
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
                        saveConversationState(req.getConversationId(), fullResponse.toString());
                    }
                });
    }

    private void saveConversationState(String threadId, String assistantMessage) {
        RLock lock = redissonClient.getLock("graph:state:lock:" + threadId);

        try {
            if (lock.tryLock(3, 5, TimeUnit.SECONDS)) {
                try {
                    // 获取当前状态
                    StateSnapshot snapshot = compiledGraph.getState(
                            RunnableConfig.builder().threadId(threadId).build()
                    );

                    if(snapshot == null){
                        return;
                    }

                    List<Message> messages = snapshot.state().value("messages", new ArrayList<>());

                    messages.add(new AssistantMessage(assistantMessage));

                    // 手动更新状态
                    compiledGraph.updateState(
                            RunnableConfig.builder().threadId(threadId).build(),
                            Map.of("messages", messages)
                    );

                    log.info("手动保存会话状态成功: {}, 消息数: {}", threadId, messages.size());

                } finally {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("保存会话状态失败", e);
        }
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
        Conversation conversation = getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId));
        if(conversation == null){
            return Map.of("messages", List.of());
        }
        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();

        try {
            StateSnapshot snapshot = compiledGraph.getState(config);
            return Map.of("messages", snapshot.state().value("messages"));
        } catch (Exception e) {
            log.warn("获取会话状态失败，会话可能没有消息: {}", conversationId);
            return Map.of("messages", List.of());
        }
    }

    @Override
    @Transactional
    public void deleteByConversationId(String conversationId) {
        remove(new LambdaQueryWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId));
    }

    @Override
    public void rename(String conversationId, String newTitle) {
        update(new LambdaUpdateWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId).set(Conversation::getTitle, newTitle));
    }

}
