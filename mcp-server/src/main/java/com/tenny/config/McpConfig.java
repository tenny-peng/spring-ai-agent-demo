package com.tenny.config;

import com.tenny.tool.TimeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider getToolCallbackProvider(TimeTool timeTool) {
        return MethodToolCallbackProvider.builder().toolObjects(timeTool).build();
    }
}
