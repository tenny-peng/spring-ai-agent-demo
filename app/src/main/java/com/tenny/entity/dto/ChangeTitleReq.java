package com.tenny.entity.dto;

import lombok.Data;

@Data
public class ChangeTitleReq {

    private String conversationId;
    private String newTitle;

}
