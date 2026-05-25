package com.tenny.config;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.tenny.node.SentenceConstructionNode;
import com.tenny.node.TranslationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class GraphConfig {

    private static final Logger log = LoggerFactory.getLogger(GraphConfig.class);

    @Bean("quickStartGraph")
    public CompiledGraph quickStartGraph() throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {
                return Map.of("input1", new ReplaceStrategy(),
                        "input2", new ReplaceStrategy());
            }
        };

        StateGraph stateGraph = new StateGraph("quickStartGraph", keyStrategyFactory);

        stateGraph.addNode("node1", AsyncNodeAction.node_async(new NodeAction() {
            @Override
            public Map<String, Object> apply(OverAllState state) throws Exception {
                log.info("node1 state:{}", state);
                return Map.of("input1", 1, "input2", 1);
            }
        }));

        stateGraph.addNode("node2", AsyncNodeAction.node_async(new NodeAction() {
            @Override
            public Map<String, Object> apply(OverAllState state) throws Exception {
                log.info("node2 state:{}", state);
                return Map.of("input1", 2, "input2", 2);
            }
        }));

        stateGraph.addEdge(StateGraph.START, "node1");
        stateGraph.addEdge("node1", "node2");
        stateGraph.addEdge("node2", StateGraph.END);

        return stateGraph.compile();
    }

    @Bean("simpleGraph")
    public CompiledGraph simpleGraph(ChatClient.Builder builder) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {
                return Map.of("word", new ReplaceStrategy());
            }
        };

        StateGraph stateGraph = new StateGraph("simpleGraph", keyStrategyFactory);

        stateGraph.addNode("SentenceConstructionNode", AsyncNodeAction.node_async(new SentenceConstructionNode(builder)));
        stateGraph.addNode("TranslationNode", AsyncNodeAction.node_async(new TranslationNode(builder)));

        stateGraph.addEdge(StateGraph.START, "SentenceConstructionNode");
        stateGraph.addEdge("SentenceConstructionNode", "TranslationNode");
        stateGraph.addEdge("TranslationNode", StateGraph.END);

        return stateGraph.compile();
    }
}
