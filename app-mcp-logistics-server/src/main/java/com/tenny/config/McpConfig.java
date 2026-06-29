package com.tenny.config;

import com.tenny.tool.LogisticsTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider getToolCallbackProvider(LogisticsTool logisticsTool) {
        return MethodToolCallbackProvider.builder().toolObjects(logisticsTool).build();
    }
}
