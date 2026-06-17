package com.tenny.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tenny.annotation.AdminRequired;
import com.tenny.common.ApiResult;
import com.tenny.entity.dto.DocumentChunkVO;
import com.tenny.entity.dto.DocumentPageReq;
import com.tenny.entity.dto.DocumentPageVO;
import com.tenny.service.DocumentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=import_template.csv");
        // 从 resources 读取
        ClassPathResource resource = new ClassPathResource("QA_template.csv");
        IOUtils.copy(resource.getInputStream(), response.getOutputStream());
    }

    @AdminRequired
    @PostMapping("upload")
    public ApiResult<?> upload(@RequestParam("file") MultipartFile file){
        if (file.isEmpty()) return ApiResult.error(400, "文件为空");
        documentService.upload(file);
        return ApiResult.success();
    }

    @PostMapping("/pageList")
    @AdminRequired
    public ApiResult<Page<DocumentPageVO>> pageList(@RequestBody DocumentPageReq req) {
        return ApiResult.success(documentService.getDocumentPage(req));
    }

    @DeleteMapping("delete/{id}")
    @AdminRequired
    public ApiResult<?> delete(@PathVariable("id") Long id) {
        documentService.delete(id);
        return ApiResult.success();
    }

    @GetMapping("detail/{id}")
    @AdminRequired
    public ApiResult<List<DocumentChunkVO>> detail(@PathVariable Long id) {
        return ApiResult.success(documentService.getDetail(id));
    }

}
