package com.tenny.entity.dto;

import lombok.Data;

@Data
public class UserPageReq {
    private int page = 1;
    private int size = 20;
    private String username;  // 模糊搜索
}