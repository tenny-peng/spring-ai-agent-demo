package com.tenny.ZhipuAiApplication.controller;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("zhipu")
public class ZhipuAiController {

    private final ChatModel chatModel;

    public ZhipuAiController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("hello")
    public String hello(){
        return "hello";
    }

    @GetMapping("simple")
    public String simple(@RequestParam("query") String query){
        return chatModel.call(query);
    }

    @GetMapping("message")
    public String message(@RequestParam("query") String query){
        SystemMessage systemMessage = new SystemMessage("你是一个有用的AI助手");
        UserMessage userMessage = new UserMessage(query);
        return chatModel.call(systemMessage, userMessage);
    }

    @GetMapping("chatOptions")
    public String chatOptions(@RequestParam("query") String query){
        SystemMessage systemMessage = new SystemMessage("你是一个有用的AI助手");
        UserMessage userMessage = new UserMessage(query);
        ZhiPuAiChatOptions zhiPuAiChatOptions = new ZhiPuAiChatOptions();
        zhiPuAiChatOptions.setModel("glm-4-flash");
        zhiPuAiChatOptions.setTemperature(0.0);
        zhiPuAiChatOptions.setMaxTokens(15536);
        ChatResponse chatResponse = chatModel.call(new Prompt(List.of(systemMessage, userMessage), zhiPuAiChatOptions));
        return chatResponse.getResult().getOutput().getText();
    }


}
