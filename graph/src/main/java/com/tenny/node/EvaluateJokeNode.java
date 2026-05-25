package com.tenny.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class EvaluateJokeNode implements NodeAction {

    private final ChatClient chatClient;

    public EvaluateJokeNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String joke = state.value("joke", "");
        PromptTemplate promptTemplate = new PromptTemplate("你是一个笑话评分专家，能够根据给定的笑话给与评定，基于笑话的搞笑程度0到10分给与评定：大于等于5分，返回优秀；否则返回不够优秀。只返回最终的评定，不要其他内容。给定的笑话:{joke}");
        promptTemplate.add("joke", joke);
        String render = promptTemplate.render();
        String content = chatClient.prompt().user(render).call().content();
        return Map.of("result", content.trim());
    }
}
