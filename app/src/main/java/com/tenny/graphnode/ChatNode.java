package com.tenny.graphnode;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.tenny.tools.RagTool;
import com.tenny.tools.WebSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatNode implements NodeAction {

    private final ChatClient chatClient;

    private final WebSearchTool webSearchTool;

    private final RagTool ragTool;

    public ChatNode(ChatClient.Builder builder, WebSearchTool webSearchTool, RagTool ragTool) {
        this.chatClient = builder.build();
        this.webSearchTool = webSearchTool;
        this.ragTool = ragTool;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        Boolean webSearchEnabled = state.value("webSearchEnabled", false);
        List<Message> historyMessages = state.value("messages", new ArrayList<>());

        String systemPrompt = "你是一个公司内部智能助手。"
                + "\n\n回答规则："
                + "\n- 对于公司业务相关问题，使用 ragSearch 工具查询知识库来获取准确信息。"
                + "\n- 直接给出答案，不要解释你是怎么知道的，不要提及你使用了什么工具。"
                + "\n- 回答中禁止出现以下任何表述："
                + "\n  · 使用了工具/调用了工具"
                + "\n  · 搜索了/查询了/获取了"
                + "\n  · 我可以/让我来/我需要"
                + "\n  · 根据搜索结果/根据我的知识"
                + "\n\n正确示例："
                + "\n  用户：你们公司是做什么的？"
                + "\n  助手：我们是一家跨境智能物流公司，主营业务包括国际空运、海运、FBA头程等。"
                + "\n\n错误示例（绝对禁止）："
                + "\n  用户：你们公司是做什么的？"
                + "\n  助手：我查询了知识库。我们是一家跨境智能物流公司...";
        var promptBuilder = chatClient.prompt()
                .system(systemPrompt)
                .messages(historyMessages)
                .tools(ragTool);
        if (Boolean.TRUE.equals(webSearchEnabled)) {
            promptBuilder.tools(webSearchTool);
        }
        Flux<String> assistantResponse  = promptBuilder.stream().content();

        return Map.of("assistant", assistantResponse);
    }
}