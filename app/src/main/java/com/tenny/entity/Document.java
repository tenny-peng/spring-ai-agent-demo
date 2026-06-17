package com.tenny.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tenny.enums.DocumentStatus;
import com.tenny.enums.FileType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document")
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String filename;

    /**
     * @see FileType
     */
    private String fileType;

    private Integer chunkCount;

    /**
     * @see DocumentStatus
     */
    private String status;

    private Long uploadedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
