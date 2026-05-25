package com.tenny.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("graph")
public class GraphController {

    private static final Logger log = LoggerFactory.getLogger(GraphController.class);
    private final CompiledGraph compiledGraph;
    private final CompiledGraph simpleGraph;

    public GraphController(@Qualifier("quickStartGraph") CompiledGraph compiledGraph, @Qualifier("simpleGraph") CompiledGraph simpleGraph) {
        this.compiledGraph = compiledGraph;
        this.simpleGraph = simpleGraph;
    }

    @GetMapping("quickStartGraph")
    public String quickStartGraph() {
        Optional<OverAllState> optionalOverAllState = compiledGraph.invoke(Map.of("input3", 3));
        log.info("optionalOverAllState: {}", optionalOverAllState);
        return "ok";
    }

    @GetMapping("simpleGraph")
    public Map<String, Object> simpleGraph(@RequestParam("word") String word) {
        Optional<OverAllState> optionalOverAllState = simpleGraph.invoke(Map.of("word", word));
        Map<String, Object> data = optionalOverAllState.map(OverAllState::data).orElse(Map.of());
        return data;
    }

}
