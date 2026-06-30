package com.tenny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.UserMemory;
import com.tenny.entity.dto.AddMemoryReq;

import java.util.List;

public interface UserMemoryService extends IService<UserMemory> {

    List<UserMemory> listByUserId(Long userId);

    void addMemory(AddMemoryReq req);

    void deleteMemory(Long id, Long userId);

    String getMemoriesSummary(Long userId);

    void extractFromConversation(String conversationId, Long userId);
}