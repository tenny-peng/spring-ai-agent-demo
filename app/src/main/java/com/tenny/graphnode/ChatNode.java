package com.tenny.graphnode;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.tenny.tools.WebSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallbackProvider;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatNode implements NodeAction {

    private final ChatClient chatClient;

    private final WebSearchTool webSearchTool;

    private final ToolCallbackProvider toolCallbackProvider;

    public ChatNode(ChatClient.Builder builder, WebSearchTool webSearchTool, ToolCallbackProvider toolCallbackProvider) {
        this.chatClient = builder.build();
        this.webSearchTool = webSearchTool;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        Boolean webSearchEnabled = state.value("webSearchEnabled", false);
        List<Message> historyMessages = state.value("messages", new ArrayList<>());
        String ragContext = state.value("ragContext", "");
        String systemPrompt = "你是一个公司内部智能助手。"
                + "\n\n回答规则："
                + "\n- 直接给出答案，不要提及你使用了什么工具。"
                + "\n- 回答中禁止出现以下任何表述："
                + "\n  · 使用了工具/调用了工具"
                + "\n  · 搜索了/查询了/获取了"
                + "\n  · 我可以/让我来/我需要"
                + "\n  · 根据搜索结果/根据我的知识库";
        String conversationSummary = state.value("conversationSummary", "");
        if (!conversationSummary.isEmpty()) {
            systemPrompt += "\n\n以下是之前的对话摘要（其中包含了历史对话的关键信息）：\n" + conversationSummary;
        }
        if (!ragContext.isEmpty()) {
            systemPrompt += "\n\n以下是知识库内容，请酌情参考这些信息回答：\n" + ragContext;
        }
        String userMemorySummary = state.value("userMemorySummary", "");
        if (!userMemorySummary.isEmpty()) {
            systemPrompt += "\n\n以下是你对用户的了解（请基于这些信息个性化回答）：\n" + userMemorySummary;
        }
        var promptBuilder = chatClient.prompt()
                .system(systemPrompt)
                .toolCallbacks(toolCallbackProvider.getToolCallbacks())
                .messages(historyMessages);
        if (Boolean.TRUE.equals(webSearchEnabled)) {
            promptBuilder.tools(webSearchTool);
        }
        Flux<String> assistantResponse  = promptBuilder.stream().content();

        return Map.of("assistant", assistantResponse);
    }
}