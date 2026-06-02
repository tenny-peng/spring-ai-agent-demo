package com.tenny.config;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.EdgeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.tenny.node.ChatNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Configuration
public class GraphConfig {

    @Bean("chatbotGraph")
    public CompiledGraph chatbotGraph(ChatClient.Builder builder) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = new KeyStrategyFactory() {
            @Override
            public Map<String, KeyStrategy> apply() {
                return Map.of("query", new ReplaceStrategy());
            }
        };

        StateGraph stateGraph = new StateGraph("chatbotGraph", keyStrategyFactory);

        stateGraph.addNode("ChatNode", AsyncNodeAction.node_async(new ChatNode(builder)));

        stateGraph.addEdge(StateGraph.START, "ChatNode");
        stateGraph.addEdge("ChatNode", StateGraph.END);

        return stateGraph.compile();
    }

}
