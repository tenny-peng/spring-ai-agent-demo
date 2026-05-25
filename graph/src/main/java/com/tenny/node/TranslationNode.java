package com.tenny.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

public class TranslationNode implements NodeAction {

    private final ChatClient chatClient;

    public TranslationNode(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String sentence = state.value("sentence", "");
        PromptTemplate promptTemplate = new PromptTemplate("你是一个英语翻译专家，能够对英语句子进行翻译。只返回最终的翻译结果，不要其他内容。给定的句子:{sentence}");
        promptTemplate.add("sentence", sentence);
        String render = promptTemplate.render();
        String content = chatClient.prompt().user(render).call().content();
        return Map.of("translation", content);
    }
}
