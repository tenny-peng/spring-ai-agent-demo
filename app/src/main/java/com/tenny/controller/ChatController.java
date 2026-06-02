package com.tenny.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("chat")
@RequiredArgsConstructor
public class ChatController {

    private final CompiledGraph compiledGraph;
    private final ChatClient.Builder chatClientBuilder;

    @GetMapping("query")
    public String query(@RequestParam String query) {
        StringBuilder sb = new StringBuilder();
        compiledGraph.stream(Map.of("query", query))
                .ofType(StreamingOutput.class)
                .map(so -> (String) so.getOriginData())
                .doOnNext(sb::append)
                .then()
                .block();
        return sb.toString();
    }

    @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String query) {
        return compiledGraph.stream(Map.of("query", query))
                .ofType(StreamingOutput.class)
                .map(so -> (String) so.getOriginData());
    }

    @GetMapping(value = "streamDirect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamDirect(@RequestParam String query) {
        return chatClientBuilder.build()
                .prompt()
                .system("你是一个有用的AI助手")
                .user(query)
                .stream()
                .content();
    }
}
