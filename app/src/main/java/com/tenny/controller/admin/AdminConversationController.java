package com.tenny.controller.admin;

import com.tenny.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/conversation")
@RequiredArgsConstructor
public class AdminConversationController {

    private final ConversationService conversationService;
}
