package com.tenny.controller.api;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tenny.annotation.AuthRequired;
import com.tenny.common.ApiResult;
import com.tenny.common.UserContext;
import com.tenny.entity.Conversation;
import com.tenny.entity.dto.ChangeTitleReq;
import com.tenny.service.ConversationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @AuthRequired
    public Flux<String> chat(@RequestParam String query, @RequestParam(required = false) String conversationId, HttpServletResponse response) {
        if(StringUtils.isEmpty(conversationId)){
            Conversation conversation = conversationService.create();
            conversationId = conversation.getConversationId();
            response.setHeader("X-Conversation-Id", conversationId);
            conversationService.generateTitleAsync(conversationId, query);
        }
        return conversationService.chat(query, conversationId);
    }

    @GetMapping("/list")
    @AuthRequired
    public ApiResult<List<Conversation>> list() {
        return ApiResult.success(conversationService.listByUserId(UserContext.getUserId()));
    }

    @GetMapping("/messages/{id}")
    @AuthRequired
    public ApiResult<Map<String, Object>> messages(@PathVariable String conversationId) {
        return ApiResult.success(conversationService.getMessages(conversationId));
    }

    @DeleteMapping("/delete/{id}")
    @AuthRequired
    public ApiResult<Void> delete(@PathVariable Long id) {
        conversationService.delete(id, UserContext.getUserId());
        return ApiResult.success(null);
    }

    @PatchMapping("/rename/{id}")
    @AuthRequired
    public ApiResult<Void> rename(@RequestBody ChangeTitleReq changeTitleReq) {
        conversationService.rename(changeTitleReq.getConversationId(), changeTitleReq.getNewTitle());
        return ApiResult.success();
    }

}
