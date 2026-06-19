package com.tenny.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class WebSearchTool {

    @Value("${tavily.api-key}")
    private String apiKey;

    @Value("${tavily.endpoint}")
    private String endpoint;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public WebSearchTool(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder.build();
        this.objectMapper = objectMapper;
    }

    @Tool(description = "搜索互联网获取最新信息，当用户需要实时信息、新闻、或当前事件时使用")
    public String webSearch(String query) {
        try {
            TavilyRequest request = new TavilyRequest();
            request.setApiKey(apiKey);
            request.setQuery(query);
            request.setSearchDepth("basic");
            request.setIncludeAnswer(true);
            request.setMaxResults(5);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String response = restTemplate.postForObject(endpoint, entity, String.class);
            TavilyResponse tavilyResponse = objectMapper.readValue(response, TavilyResponse.class);

            StringBuilder result = new StringBuilder();
            if (tavilyResponse.getAnswer() != null && !tavilyResponse.getAnswer().isEmpty()) {
                result.append("摘要：").append(tavilyResponse.getAnswer()).append("\n\n");
            }
            result.append("搜索结果：\n");
            for (int i = 0; i < tavilyResponse.getResults().size(); i++) {
                TavilyResult r = tavilyResponse.getResults().get(i);
                result.append(i + 1).append(". ").append(r.getTitle()).append("\n");
                result.append("   链接：").append(r.getUrl()).append("\n");
                result.append("   内容：").append(r.getContent()).append("\n\n");
            }
            return result.toString();

        } catch (Exception e) {
            return "搜索失败：" + e.getMessage();
        }
    }

    @Data
    static class TavilyRequest {
        @JsonProperty("api_key")
        private String apiKey;
        private String query;
        @JsonProperty("search_depth")
        private String searchDepth;
        @JsonProperty("include_answer")
        private boolean includeAnswer;
        @JsonProperty("max_results")
        private int maxResults;
    }

    @Data
    static class TavilyResponse {
        private String answer;
        private List<TavilyResult> results;
    }

    @Data
    static class TavilyResult {
        private String title;
        private String url;
        private String content;
        private double score;
    }
}