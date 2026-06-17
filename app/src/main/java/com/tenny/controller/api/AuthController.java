package com.tenny.controller.api;

import com.tenny.annotation.AuthRequired;
import com.tenny.common.ApiResult;
import com.tenny.entity.dto.AuthRequest;
import com.tenny.entity.dto.LoginResponse;
import com.tenny.utils.TokenUtils;
import com.tenny.entity.User;
import com.tenny.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ApiResult<?> register(@RequestBody AuthRequest request) {
        User user = userService.register(request.getUsername(), request.getEmail(), request.getPassword());
        LoginResponse loginResponse = new LoginResponse();
        BeanUtils.copyProperties(user, loginResponse);
        return ApiResult.success(loginResponse);
    }

    @PostMapping("/login")
    public ApiResult<?> login(@RequestBody AuthRequest request) {
        return ApiResult.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/logout")
    @AuthRequired
    public ApiResult<?> logout(HttpServletRequest request) {
        String token = TokenUtils.extractToken(request);
        if (token != null) {
            userService.logout(token);
        }
        return ApiResult.success(null);
    }

}
