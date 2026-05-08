package com.tenny.ZhipuAiApplication.controller;

import com.tenny.ZhipuAiApplication.advisor.SGCallAdvisor1;
import com.tenny.ZhipuAiApplication.advisor.SGCallAdvisor2;
import com.tenny.ZhipuAiApplication.advisor.SimpleMessageChatMemoryAdvisor;
import com.tenny.ZhipuAiApplication.entity.Book;
import org.springframework.ai.chat.client.ChatClient;
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
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("chatClient")
public class ZhipuChatClientController {

    private final ChatClient chatClient;

    public ZhipuChatClientController(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @GetMapping("simple")
    public String simple(@RequestParam("query") String query){
        ZhiPuAiChatOptions zhiPuAiChatOptions = ZhiPuAiChatOptions.builder()
                        .maxTokens(15536)
                        .temperature(0.0)
                        .model("glm-4-flash")
                        .build();
        return chatClient.prompt()
                .system("你是一个有用的AI助手")
                .user(query)
                .options(zhiPuAiChatOptions)
                .call()
                .content();
    }

    @GetMapping("chatResponse")
    public ChatResponse chatResponse(@RequestParam("query") String query){
        ZhiPuAiChatOptions zhiPuAiChatOptions = ZhiPuAiChatOptions.builder()
                .maxTokens(15536)
                .temperature(0.0)
                .model("glm-4-flash")
                .build();
        return chatClient.prompt()
                .system("你是一个有用的AI助手")
                .user(query)
                .options(zhiPuAiChatOptions)
                .call()
                .chatResponse();
    }

    @GetMapping("entity")
    public Book entity(){
        return chatClient
                .prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .call()
                .entity(Book.class);
    }

    @GetMapping("stream")
    public Flux<String> stream(){
        return chatClient
                .prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .stream()
                .content();
    }

    @GetMapping("advisor")
    public Book advisor(){
        return chatClient
                .prompt()
                .user("给我随机生成一本书，要求书名和作者都是中文")
                .advisors(new SGCallAdvisor1(), new SGCallAdvisor2())
                .call()
                .entity(Book.class);
    }

    @GetMapping("SimpleMessageChatMemory")
    public String SimpleMessageChatMemory(@RequestParam("query") String query){
        return chatClient
                .prompt()
                .user(query)
                .advisors(new SimpleMessageChatMemoryAdvisor())
                .call()
                .content();
    }

}
