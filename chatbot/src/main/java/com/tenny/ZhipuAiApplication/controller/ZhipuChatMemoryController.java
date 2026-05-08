package com.tenny.ZhipuAiApplication.controller;

import com.tenny.ZhipuAiApplication.advisor.SGCallAdvisor1;
import com.tenny.ZhipuAiApplication.advisor.SGCallAdvisor2;
import com.tenny.ZhipuAiApplication.advisor.SimpleMessageChatMemoryAdvisor;
import com.tenny.ZhipuAiApplication.entity.Book;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@RestController
@RequestMapping("chatMemory")
public class ZhipuChatMemoryController {

    private final ChatClient chatClient;

    public ZhipuChatMemoryController(ChatClient.Builder builder) {
        MessageWindowChatMemory windowChatMemory = MessageWindowChatMemory.builder().build();

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(windowChatMemory).build();

        this.chatClient = builder
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
    }

    @GetMapping("simpleMessageChatMemory")
    public String simpleMessageChatMemory(@RequestParam("query") String query, @RequestParam("conversationId") String conversationId){
        return chatClient
                .prompt()
                .user(query)
                .advisors(new Consumer<ChatClient.AdvisorSpec>() {
                    @Override
                    public void accept(ChatClient.AdvisorSpec advisorSpec) {
                        advisorSpec.param("conversationId", conversationId);
                    }
                })
                .advisors(new SimpleMessageChatMemoryAdvisor())
                .call()
                .content();
    }

    @GetMapping("messageChatMemoryAdvisor")
    public String messageChatMemoryAdvisor(@RequestParam("query") String query, @RequestParam("conversationId") String conversationId){
        return chatClient
                .prompt()
                .user(query)
                .advisors(new Consumer<ChatClient.AdvisorSpec>() {
                    @Override
                    public void accept(ChatClient.AdvisorSpec advisorSpec) {
                        advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId);
                    }
                })
                .call()
                .content();
    }

}
