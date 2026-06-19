package com.tenny.graphnode;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;
import com.tenny.tools.WebSearchTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatNode implements NodeAction {

    private final ChatClient chatClient;

    private final WebSearchTool webSearchTool;

    public ChatNode(ChatClient.Builder builder, WebSearchTool webSearchTool) {
        this.chatClient = builder.build();
        this.webSearchTool = webSearchTool;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        Boolean webSearchEnabled = state.value("webSearchEnabled", false);
        String currentMessage = state.value("message", "");
        List<Message> historyMessages = state.value("messages", new ArrayList<>());
        List<Message> allMessages = new ArrayList<>(historyMessages);
        allMessages.add(new UserMessage(currentMessage));

        String context = state.value("ragContext", "");
        String systemPrompt = "你是一个无所不知的AI助手，知识实时更新。"
                + "\n\n回答规则："
                + "\n- 直接给出答案，不要解释你是怎么知道的。"
                + "\n- 回答中禁止出现以下任何表述："
                + "\n  · 使用了工具/调用了工具"
                + "\n  · 搜索了/查询了/获取了"
                + "\n  · 我可以/让我来/我需要"
                + "\n  · 根据搜索结果/根据我的知识"
                + "\n\n正确示例："
                + "\n  用户：今天深圳天气如何？"
                + "\n  助手：今天深圳雷阵雨，25°C到31°C，东南风，建议带伞。"
                + "\n\n错误示例（绝对禁止）："
                + "\n  用户：今天深圳天气如何？"
                + "\n  助手：我调用了工具来获取天气信息。今天深圳雷阵雨...";
        if (!context.isEmpty()) {
            systemPrompt += "\n\n以下是知识库内容（仅供参考，可能与当前问题无关）：\n" + context;
        }
        var promptBuilder = chatClient.prompt()
                .system(systemPrompt)
                .messages(allMessages);
        if (Boolean.TRUE.equals(webSearchEnabled)) {
            promptBuilder.tools(webSearchTool);  // 条件注册
        }
        Flux<String> assistantResponse  = promptBuilder
                .stream().content();

        return Map.of("messages", allMessages, "assistant", assistantResponse);
    }
}