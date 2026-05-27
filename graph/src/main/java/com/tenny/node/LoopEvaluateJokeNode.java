package com.tenny.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class LoopEvaluateJokeNode implements NodeAction {

    private final ChatClient chatClient;
    private final Integer targetScore;
    private final Integer maxLoopCount;

    public LoopEvaluateJokeNode(ChatClient.Builder builder, Integer targetScore, Integer maxLoopCount) {
        this.chatClient = builder.build();
        this.targetScore = targetScore;
        this.maxLoopCount = maxLoopCount;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String joke = state.value("joke", "");
        Integer loopCount = state.value("loopCount", 1);
        if(loopCount >= maxLoopCount){
            return Map.of("result", "break", "loopCount", loopCount);
        }

        PromptTemplate promptTemplate = new PromptTemplate("你是一个笑话评分专家，根据给定的笑话，基于笑话的搞笑程度从0到10分给与评分。只返回最终的评分，不要其他内容。给定的笑话:{joke}");
        promptTemplate.add("joke", joke);
        String render = promptTemplate.render();
        String content = chatClient.prompt().user(render).call().content();

        int score = Integer.parseInt(content.trim());
        String result = "break";
        if(score < targetScore) {
            result = "loop";
        }
        loopCount++;
        return Map.of("score", score, "result", result, "loopCount", loopCount);
    }
}
