package com.tenny.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("chat")
@RequiredArgsConstructor
public class ChatController {

    private final CompiledGraph compiledGraph;

    @GetMapping("query")
    public String query(@RequestParam String query) {
        Optional<OverAllState> optionalOverAllState = compiledGraph.invoke(Map.of("query", query));
        Map<String, Object> data = optionalOverAllState.map(OverAllState::data).orElse(Map.of());
        return data.get("output").toString();
    }
}
