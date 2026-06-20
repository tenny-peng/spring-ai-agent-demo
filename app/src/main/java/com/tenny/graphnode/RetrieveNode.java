package com.tenny.graphnode;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RetrieveNode implements NodeAction {

    private final VectorStore vectorStore;

    public RetrieveNode(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Map<String, Object> apply(OverAllState state) {
        String query = state.value("message", "");
        List<Document> docs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.5)
                .build()
        );
        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
        return Map.of("ragContext", context);
    }
}