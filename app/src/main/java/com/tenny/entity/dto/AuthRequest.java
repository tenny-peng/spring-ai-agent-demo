package com.tenny.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度2-50")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Size(min = 5, max = 100, message = "邮箱长度5-100")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 100, message = "密码长度4-100")
    private String password;

}
