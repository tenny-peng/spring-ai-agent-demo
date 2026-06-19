package com.tenny.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tenny.enums.MessageRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String conversationId;
    /**
     * @see MessageRole
     */
    private String role;
    private String content ;
    private LocalDateTime createdAt;
}
