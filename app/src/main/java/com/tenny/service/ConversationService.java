package com.tenny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.Conversation;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface ConversationService extends IService<Conversation> {

    Conversation create();

    void generateTitleAsync(String conversationId, Long userId, String firstQuery);

    Flux<String> chat(String message, String conversationId);

    List<Conversation> listByUserId(Long userId);

    Conversation getByConversationId(String conversationId);

    Map<String, Object> getMessages(String conversationId);

    void deleteByConversationId(String conversationId);

    void rename(String conversationId, String newTitle);

}
