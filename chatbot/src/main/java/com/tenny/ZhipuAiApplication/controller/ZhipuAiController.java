package com.tenny.ZhipuAiApplication.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
