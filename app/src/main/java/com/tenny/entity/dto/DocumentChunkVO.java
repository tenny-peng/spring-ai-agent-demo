package com.tenny.entity.dto;

import lombok.Data;

@Data
public class DocumentChunkVO {

    private Integer chunkIndex;
    private String content;
    private String vectorId;
}
