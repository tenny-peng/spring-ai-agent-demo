package com.tenny.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.common.BusinessException;
import com.tenny.common.UserContext;
import com.tenny.entity.Document;
import com.tenny.entity.DocumentChunk;
import com.tenny.entity.User;
import com.tenny.entity.dto.DocumentChunkVO;
import com.tenny.entity.dto.DocumentPageReq;
import com.tenny.entity.dto.DocumentPageVO;
import com.tenny.enums.DocumentStatus;
import com.tenny.mapper.DocumentMapper;
import com.tenny.mapper.UserMapper;
import com.tenny.service.DocumentChunkService;
import com.tenny.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    private final UserMapper userMapper;

    private final DocumentChunkService documentChunkService;

    private final VectorStore vectorStore;

    @Override
    public void upload(MultipartFile file) {
        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1).toUpperCase())
                .orElse("");
        if(!"CSV".equalsIgnoreCase(ext)){
            throw new BusinessException("只支持csv文件");
        }
        if(exists(new LambdaQueryWrapper<Document>().eq(Document::getFilename, file.getOriginalFilename()))){
            throw new BusinessException("文件已存在，请先删除旧文件再上传");
        }
        com.tenny.entity.Document documentEntity = new com.tenny.entity.Document();
        documentEntity.setFilename(file.getOriginalFilename());
        documentEntity.setFileType(ext);
        documentEntity.setUploadedBy(UserContext.getUserId());
        documentEntity.setStatus(DocumentStatus.IMPORTING.name());
        documentEntity.setCreatedAt(LocalDateTime.now());
        documentEntity.setUpdatedAt(LocalDateTime.now());
        this.save(documentEntity);

        List<DocumentChunk> documentChunks = new ArrayList<>();
        List<org.springframework.ai.document.Document> documents = null;
        try {
            documents = this.parseCsv(file);
        } catch (IOException e) {
            documentEntity.setStatus(DocumentStatus.FAILED.name());
            this.updateById(documentEntity);
        }
        if (documents == null || documents.isEmpty()) {
            throw new BusinessException("文档问答内容为空");
        }

        vectorStore.add(documents);

        for (int i = 0; i < documents.size(); i++) {
            org.springframework.ai.document.Document document = documents.get(i);
            DocumentChunk documentChunk = new DocumentChunk();
            documentChunk.setDocumentId(documentEntity.getId());
            documentChunk.setChunkIndex(i);
            documentChunk.setVectorId(document.getId());
            documentChunk.setContent(document.getText());
            documentChunks.add(documentChunk);
        }

        documentChunkService.saveBatch(documentChunks);

        documentEntity.setChunkCount(documents.size());
        documentEntity.setStatus(DocumentStatus.COMPLETED.name());
        this.updateById(documentEntity);
    }

    private List<org.springframework.ai.document.Document> parseCsv(MultipartFile file) throws IOException {
        InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
        CSVParser csvParser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);
        List<org.springframework.ai.document.Document> documents = new ArrayList<>();
        for (CSVRecord record : csvParser) {
            String question = record.get("问题");
            String answer = record.get("答案");

            String vectorId = UUID.randomUUID().toString();

            // 组合成文档内容
            String content = String.format("问题：%s\n答案：%s", question, answer);

            org.springframework.ai.document.Document document = new org.springframework.ai.document.Document(vectorId, content, new HashMap<>());
            documents.add(document);
        }
        csvParser.close();
        reader.close();
        return documents;
    }

    @Override
    public Page<DocumentPageVO> getDocumentPage(DocumentPageReq req) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(req.getFilename()), Document::getFilename, req.getFilename())
                .orderByDesc(Document::getCreatedAt);
        Page<Document> documentPage = page(new Page<>(req.getPage(), req.getSize()), wrapper);

        List<Long> userIds = documentPage.getRecords().stream().map(Document::getUploadedBy).toList();
        Map<Long, String> usernameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .select(User::getId, User::getUsername)
                            .in(User::getId, userIds)
            ).forEach(u -> usernameMap.put(u.getId(), u.getUsername()));
        }

        List<DocumentPageVO> voList = documentPage.getRecords().stream().map(document -> {
            DocumentPageVO vo = new DocumentPageVO();
            BeanUtils.copyProperties(document, vo);
            vo.setUploadedByName(usernameMap.get(document.getUploadedBy()));
            return vo;
        }).toList();

        Page<DocumentPageVO> voPage = new Page<>(documentPage.getCurrent(), documentPage.getSize(), documentPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Transactional
    @Override
    public void delete(Long id) {
        List<DocumentChunk> documentChunks = documentChunkService.list(new LambdaQueryWrapper<DocumentChunk>().eq(DocumentChunk::getDocumentId, id));
        List<String> vectorIds = documentChunks.stream().map(DocumentChunk::getVectorId).toList();
        vectorStore.delete(vectorIds);
        documentChunkService.remove(new LambdaQueryWrapper<DocumentChunk>().eq(DocumentChunk::getDocumentId, id));
        this.removeById(id);
    }

    @Override
    public List<DocumentChunkVO> getDetail(Long id) {
        Document doc = this.getById(id);
        if (doc == null) throw new BusinessException("文档不存在");
        List<DocumentChunk> documentChunks = documentChunkService.list(new LambdaQueryWrapper<DocumentChunk>().eq(DocumentChunk::getDocumentId, id));
        return documentChunks.stream().map(documentChunk -> {
            DocumentChunkVO vo = new DocumentChunkVO();
            BeanUtils.copyProperties(documentChunk, vo);
            return vo;
        }).toList();
    }

}
