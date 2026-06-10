package com.tenny.entity.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Long id;
    String token;
    String username;
}
