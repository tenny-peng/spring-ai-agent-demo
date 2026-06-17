package com.tenny.entity.dto;

import lombok.Data;

@Data
public class DocumentPageReq {
    private int page = 1;
    private int size = 20;
    private String filename;  // 模糊搜索
}