package com.tenny.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("coffee")
public class CoffeeController {

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    public CoffeeController(VectorStore vectorStore, ChatClient.Builder clientBuilder) {
        this.vectorStore = vectorStore;

        VectorStoreDocumentRetriever vectorStoreDocumentRetriever = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(this.vectorStore)
                .build();
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(vectorStoreDocumentRetriever)
                .build();

        this.chatClient = clientBuilder
                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }

    @GetMapping("importData")
    public String importData(){
        try {
            ClassPathResource resource = new ClassPathResource("qa.csv");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            List<Document> documents = new ArrayList<>();

            for (CSVRecord record : csvParser) {
                String question = record.get("问题");
                String answer = record.get("答案");

                // 组合成文档内容
                String content = String.format("问题：%s\n答案：%s", question, answer);
                Document document = new Document(content);
                documents.add(document);
            }

            csvParser.close();
            reader.close();

            vectorStore.add(documents);

            return "成功导入" + documents.size() + "条记录到向量数据库";
        } catch (IOException e) {
            return("加载 CSV 文件失败: " + e.getMessage());
        }
    }

    @GetMapping("rag-ask")
    public String ragAsk(@RequestParam("question") String question){
        return chatClient.prompt()
                .system("你是一个咖啡店的服务员，你需要回答用户的问题")
                .user(question)
                .call()
                .content();
    }

}
