package com.tenny.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class GenerateJokeNode implements NodeAction {

    private final ChatClient chatClient;

    public GenerateJokeNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String topic = state.value("topic", "");
        PromptTemplate promptTemplate = new PromptTemplate("你是一个笑话专家，能够根据给定的主题写笑话。只返回最终的笑话，不要其他内容。给定的主题:{topic}");
        promptTemplate.add("topic", topic);
        String render = promptTemplate.render();
        String content = chatClient.prompt().user(render).call().content();
        return Map.of("joke", content);
    }
}
