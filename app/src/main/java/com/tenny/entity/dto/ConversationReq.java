package com.tenny.entity.dto;

import lombok.Data;

@Data
public class ConversationReq {

    private String message;

    private String conversationId;

    private Boolean webSearchEnabled;
}
