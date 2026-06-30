package com.tenny.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tenny.enums.MemoryCategory;
import com.tenny.enums.MemorySource;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_memory")
public class UserMemory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    /**
     * @see MemoryCategory
     */
    private String category;
    /**
     * @see MemorySource
     */
    private String source;
    private String sourceConversationId;
    private Integer confidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}