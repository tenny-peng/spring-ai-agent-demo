package com.tenny.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tenny.annotation.AdminRequired;
import com.tenny.common.ApiResult;
import com.tenny.entity.dto.UserPageReq;
import com.tenny.entity.dto.UserPageVO;
import com.tenny.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PostMapping("/pageList")
    @AdminRequired
    public ApiResult<Page<UserPageVO>> pageList(@RequestBody UserPageReq req) {
        return ApiResult.success(userService.getUserPage(req));
    }
}
