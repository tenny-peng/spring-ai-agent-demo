package com.tenny.graphnode;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

import java.util.Map;

public class ChatNode implements NodeAction {

    private final ChatClient chatClient;

    public ChatNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String query = state.value("query", "");
        Flux<String> content = chatClient.prompt()
                .system("你是一个有用的AI助手")
                .user(query)
                .stream().content();

        return Map.of("output", content);
    }
}