package com.tenny.graphnode;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatNode implements NodeAction {

    private final ChatClient chatClient;

    public ChatNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String currentMessage = state.value("message", "");
        List<Message> historyMessages = state.value("messages", new ArrayList<>());
        List<Message> allMessages = new ArrayList<>(historyMessages);
        allMessages.add(new UserMessage(currentMessage));

        String context = state.value("ragContext", "");
        String systemPrompt = "你是一个有用的AI助手。";
        if (!context.isEmpty()) {
            systemPrompt += "\n\n以下是相关的知识库内容，请基于这些信息回答：\n" + context;
        }
        Flux<String> assistantResponse  = chatClient.prompt()
                .system(systemPrompt)
                .messages(allMessages)
                .stream().content();

        return Map.of("messages", allMessages, "assistant", assistantResponse);
    }
}