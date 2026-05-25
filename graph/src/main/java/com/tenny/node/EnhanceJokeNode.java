package com.tenny.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class EnhanceJokeNode implements NodeAction {

    private final ChatClient chatClient;

    public EnhanceJokeNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String joke = state.value("joke", "");
        PromptTemplate promptTemplate = new PromptTemplate("你是一个笑话优化专家，能够优化给定的笑话。只返回最终优化的笑话，不要其他内容。给定的笑话:{joke}");
        promptTemplate.add("joke", joke);
        String render = promptTemplate.render();
        String content = chatClient.prompt().user(render).call().content();
        return Map.of("newJoke", content);
    }
}
