package com.tenny.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.Document;
import com.tenny.entity.dto.DocumentPageReq;
import com.tenny.entity.dto.DocumentPageVO;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService extends IService<Document> {
    void upload(MultipartFile file);

    Page<DocumentPageVO> getDocumentPage(DocumentPageReq req);

    void delete(Long id);


}
