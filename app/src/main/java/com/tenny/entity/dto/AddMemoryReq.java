package com.tenny.entity.dto;

import lombok.Data;

@Data
public class AddMemoryReq {
    private String content;
    private String category = "OTHER";
}