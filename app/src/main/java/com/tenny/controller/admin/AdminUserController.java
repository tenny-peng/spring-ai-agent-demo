package com.tenny.controller.admin;

import com.tenny.annotation.AdminRequired;
import com.tenny.common.ApiResult;
import com.tenny.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/list")
    @AdminRequired
    public ApiResult<?> list() {
        return ApiResult.success(userService.list());
    }
}
