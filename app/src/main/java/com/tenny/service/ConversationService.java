package com.tenny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.Conversation;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface ConversationService extends IService<Conversation> {

    Conversation create();

    void generateTitleAsync(String conversationId, String firstQuery);

    Flux<String> chat(String query, String conversationId);

    List<Conversation> listByUserId(Long userId);

    Map<String, Object> getMessages(String conversationId);

    void delete(Long id, Long userId);

    void rename(String conversationId, String newTitle);

}
