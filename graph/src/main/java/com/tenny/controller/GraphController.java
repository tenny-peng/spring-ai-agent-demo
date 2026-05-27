package com.tenny.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
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
    private final CompiledGraph conditionalGraph;
    private final CompiledGraph loopGraph;
    private final CompiledGraph saveGraph;

    public GraphController(@Qualifier("quickStartGraph") CompiledGraph compiledGraph,
                           @Qualifier("simpleGraph") CompiledGraph simpleGraph,
                           @Qualifier("conditionalGraph") CompiledGraph conditionalGraph,
                           @Qualifier("loopGraph") CompiledGraph loopGraph,
                           @Qualifier("saveGraph") CompiledGraph saveGraph) {
        this.compiledGraph = compiledGraph;
        this.simpleGraph = simpleGraph;
        this.conditionalGraph = conditionalGraph;
        this.loopGraph = loopGraph;
        this.saveGraph = saveGraph;
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

    @GetMapping("conditionalGraph")
    public Map<String, Object> conditionalGraph(@RequestParam("topic") String topic) {
        Optional<OverAllState> optionalOverAllState = conditionalGraph.invoke(Map.of("topic", topic));
        Map<String, Object> data = optionalOverAllState.map(OverAllState::data).orElse(Map.of());
        return data;
    }

    @GetMapping("loopGraph")
    public Map<String, Object> loopGraph(@RequestParam("topic") String topic) {
        Optional<OverAllState> optionalOverAllState = loopGraph.invoke(Map.of("topic", topic));
        Map<String, Object> data = optionalOverAllState.map(OverAllState::data).orElse(Map.of());
        return data;
    }

    @GetMapping("saveGraph")
    public Map<String, Object> saveGraph(@RequestParam("msg") String msg, @RequestParam("conversationId") String conversationId) {
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(conversationId).build();
        Optional<OverAllState> optionalOverAllState = saveGraph.invoke(Map.of("msg", msg), runnableConfig);
        Map<String, Object> data = optionalOverAllState.map(OverAllState::data).orElse(Map.of());
        return data;
    }

}
