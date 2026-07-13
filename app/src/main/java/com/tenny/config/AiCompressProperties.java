package com.tenny.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.compress")
@Data
public class AiCompressProperties {
    private Long maxTokens = 128000L;
    private double ratio = 0.5;
    private double targetRatio = 0.25;
    private double summaryRatio = 0.1;
}