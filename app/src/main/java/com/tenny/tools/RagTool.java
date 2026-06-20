package com.tenny.tools;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RagTool {

    private final VectorStore vectorStore;

    public RagTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(description = "查询公司内部知识库，获取公司业务信息、产品介绍、内部政策等，当用户询问公司相关问题时必须使用此工具")
    public String ragSearch(@ToolParam(description = "搜索关键词") String query) {
        List<Document> docs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.5)
                .build()
        );
        if (docs.isEmpty()) return "未找到相关文档";
        return docs.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n---\n"));
    }
}