package com.tenny.ZhipuAiApplication.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.*;

public class SimpleMessageChatMemoryAdvisor implements BaseAdvisor {

    public final static Map<String, List<Message>> chatMemory = new HashMap<>();

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String conversationId = "tenny";
        List<Message> messages = chatMemory.get(conversationId);
        if (messages == null){
            messages = new ArrayList<>();
        }
        List<Message> instructions = chatClientRequest.prompt().getInstructions();
        messages.addAll(instructions);
        chatMemory.put(conversationId, messages);

        Prompt oldPrompt = chatClientRequest.prompt();
        Prompt newPrompt = oldPrompt.mutate().messages(messages).build();
        ChatClientRequest clientRequest = chatClientRequest.mutate().prompt(newPrompt).build();

        return clientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        String conversationId = "tenny";
        List<Message> messages = chatMemory.get(conversationId);
        if (messages == null){
            messages = new ArrayList<>();
        }

        AssistantMessage assistantMessage = chatClientResponse.chatResponse().getResult().getOutput();
        messages.add(assistantMessage);
        chatMemory.put(conversationId, messages);
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
