package com.tenny.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public CoffeeController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
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

}
