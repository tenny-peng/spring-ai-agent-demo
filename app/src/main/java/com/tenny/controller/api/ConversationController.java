package com.tenny.controller.api;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tenny.annotation.AuthRequired;
import com.tenny.common.ApiResult;
import com.tenny.common.UserContext;
import com.tenny.entity.Conversation;
import com.tenny.entity.dto.ChangeTitleReq;
import com.tenny.entity.dto.ConversationReq;
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

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @AuthRequired
    public Flux<String> chat(@RequestBody ConversationReq req, HttpServletResponse response) {
        String conversationId = req.getConversationId();
        String message = req.getMessage();
        if(StringUtils.isEmpty(conversationId) || conversationId.startsWith("temp_")){
            Conversation conversation = conversationService.create();
            req.setConversationId(conversation.getConversationId());
            response.setHeader("X-Conversation-Id", req.getConversationId());
            response.setHeader("Access-Control-Expose-Headers", "X-Conversation-Id");
            conversationService.generateTitleAsync(req.getConversationId(), conversation.getUserId(), message);
        }
        return conversationService.chat(req);
    }

    @GetMapping("/list")
    @AuthRequired
    public ApiResult<List<Conversation>> list() {
        return ApiResult.success(conversationService.listByUserId(UserContext.getUserId()));
    }

    @GetMapping("getOne/{conversationId}")
    @AuthRequired
    public ApiResult<Conversation> getConversation(@PathVariable String conversationId) {
        Conversation conversation = conversationService.getByConversationId(conversationId);
        if (conversation == null) {
            return ApiResult.error(404, "会话不存在");
        }
        return ApiResult.success(conversation);
    }

    @GetMapping("/messages/{conversationId}")
    @AuthRequired
    public ApiResult<Map<String, Object>> messages(@PathVariable String conversationId) {
        if(StringUtils.isEmpty(conversationId)){
            return ApiResult.success(Map.of("messages", List.of()));
        }
        return ApiResult.success(conversationService.getMessages(conversationId));
    }

    @DeleteMapping("/delete/{conversationId}")
    @AuthRequired
    public ApiResult<Void> delete(@PathVariable String conversationId) {
        conversationService.deleteByConversationId(conversationId);
        return ApiResult.success(null);
    }

    @PostMapping("/rename")
    @AuthRequired
    public ApiResult<Void> rename(@RequestBody ChangeTitleReq changeTitleReq) {
        conversationService.rename(changeTitleReq.getConversationId(), changeTitleReq.getNewTitle());
        return ApiResult.success();
    }

}
