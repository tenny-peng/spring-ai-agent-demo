package com.tenny.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String conversationId;
    private String title;
    private String status;
    private Integer messageCount;
    private String compressSummary;
    private Long compressLastIndex;
    private Integer compressVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
