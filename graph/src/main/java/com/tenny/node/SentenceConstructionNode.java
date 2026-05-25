package com.tenny.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class SentenceConstructionNode implements NodeAction {

    private final ChatClient chatClient;

    public SentenceConstructionNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String word = state.value("word", "");
        PromptTemplate promptTemplate = new PromptTemplate("你是一个英语造句专家，能够根据给定的单词造句。只返回最终的句子，不要其他内容。给定的单词:{word}");
        promptTemplate.add("word", word);
        String render = promptTemplate.render();
        String content = chatClient.prompt().user(render).call().content();
        return Map.of("sentence", content);
    }
}
