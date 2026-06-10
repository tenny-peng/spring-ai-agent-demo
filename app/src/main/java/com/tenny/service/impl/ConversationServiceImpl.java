package com.tenny.service.impl;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.common.ChatConstants;
import com.tenny.common.UserContext;
import com.tenny.entity.Conversation;
import com.tenny.mapper.ConversationMapper;
import com.tenny.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final CompiledGraph compiledGraph;
    private final ChatClient chatClient;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public ConversationServiceImpl(CompiledGraph compiledGraph, ChatModel chatModel) {
        this.compiledGraph = compiledGraph;
        this.chatClient = ChatClient.builder(chatModel).build();
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
    public void generateTitleAsync(String conversationId, String firstQuery) {
        Conversation conversation = this.getOne(new LambdaQueryWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId));
        if (conversation == null || !ChatConstants.DEFAULT_TITLE.equals(conversation.getTitle())) {
            return;
        }
        executor.submit(() -> {
            try {
                String title = generateTitleByAI(firstQuery);
                update(new LambdaUpdateWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId).set(Conversation::getTitle, title));
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
    public Flux<String> chat(String query, String conversationId) {
        assert conversationId != null;

        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();

        StringBuilder fullResponse = new StringBuilder();

        return compiledGraph.stream(Map.of("query", query), config)
                .ofType(StreamingOutput.class)
                .map(so -> {
                    Object data = so.getOriginData();
                    if (data instanceof String text) {
                        fullResponse.append(text);
                        return text;
                    }
                    return "";
                })
                .doFinally(signalType -> {
                    if (!fullResponse.isEmpty()) {
                        log.info("assistant: {}", fullResponse.toString());
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
    public Map<String, Object> getMessages(String conversationId) {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(conversationId)
                .build();

        StateSnapshot snapshot = compiledGraph.getState(config);
        if (snapshot == null) {
            return Map.of("messages", List.of());
        }
        // 从 state 中提取消息列表（根据你的实际结构）
        List<Map<String, String>> messages = extractMessages(snapshot.state());
        return Map.of("messages", messages);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        removeById(id);
    }

    @Override
    public void rename(String conversationId, String newTitle) {
        update(new LambdaUpdateWrapper<Conversation>().eq(Conversation::getUserId, UserContext.getUserId()).eq(Conversation::getConversationId, conversationId).set(Conversation::getTitle, newTitle));
    }


    private List<Map<String, String>> extractMessages(OverAllState state) {
        return new ArrayList<>();
    }

}
