package com.tenny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.UserMemory;

import java.util.List;

public interface UserMemoryService extends IService<UserMemory> {

    List<UserMemory> listByUserId(Long userId);

    void addMemory(Long userId, String content, String category);

    void deleteMemory(Long id, Long userId);

    String getMemoriesSummary(Long userId);

    void extractFromConversation(String conversationId, Long userId);
}