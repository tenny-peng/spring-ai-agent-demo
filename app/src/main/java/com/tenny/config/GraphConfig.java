package com.tenny.config;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.tenny.graphnode.ChatNode;
import com.tenny.graphnode.RetrieveNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.tenny.tools.WebSearchTool;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GraphConfig {

    private final RedissonClient redissonClient;

    private final VectorStore vectorStore;

    private final WebSearchTool webSearchTool;

    @Bean("chatbotGraph")
    public CompiledGraph chatbotGraph(ChatClient.Builder builder) throws GraphStateException {
        KeyStrategyFactory keyStrategyFactory = () -> Map.of(
                "message", new ReplaceStrategy(),
                "assistant", new ReplaceStrategy(),
                "messages", new ReplaceStrategy(),
                "ragContext", new ReplaceStrategy(),
                "webSearchEnabled", new ReplaceStrategy()
        );

        StateGraph stateGraph = new StateGraph("chatbotGraph", keyStrategyFactory);

        stateGraph.addNode("RetrieveNode", AsyncNodeAction.node_async(new RetrieveNode(vectorStore)));
        stateGraph.addNode("ChatNode", AsyncNodeAction.node_async(new ChatNode(builder, webSearchTool)));

        stateGraph.addEdge(StateGraph.START, "RetrieveNode");
        stateGraph.addEdge("RetrieveNode", "ChatNode");
        stateGraph.addEdge("ChatNode", StateGraph.END);

        SaverConfig saverConfig = SaverConfig.builder()
                .register(RedisSaver.builder().redisson(redissonClient).build())
                .build();

        return stateGraph.compile(CompileConfig.builder()
                .saverConfig(saverConfig)
                .build());
    }

}