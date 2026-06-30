package com.tenny.controller.api;

import com.tenny.annotation.AuthRequired;
import com.tenny.common.ApiResult;
import com.tenny.common.UserContext;
import com.tenny.entity.UserMemory;
import com.tenny.entity.dto.AddMemoryReq;
import com.tenny.service.UserMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/userMemory")
@RequiredArgsConstructor
public class UserMemoryController {

    private final UserMemoryService userMemoryService;

    @GetMapping("/getMemories")
    @AuthRequired
    public ApiResult<List<UserMemory>> getMemories() {
        return ApiResult.success(userMemoryService.listByUserId(UserContext.getUserId()));
    }

    @PostMapping("/add")
    @AuthRequired
    public ApiResult<Void> add(@RequestBody AddMemoryReq req) {
        userMemoryService.addMemory(req);
        return ApiResult.success(null);
    }

    @DeleteMapping("/delete/{id}")
    @AuthRequired
    public ApiResult<Void> delete(@PathVariable Long id) {
        userMemoryService.deleteMemory(id, UserContext.getUserId());
        return ApiResult.success(null);
    }
}