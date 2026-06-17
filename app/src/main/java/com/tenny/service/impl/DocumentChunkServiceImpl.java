package com.tenny.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.entity.DocumentChunk;
import com.tenny.mapper.DocumentChunkMapper;
import com.tenny.service.DocumentChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentChunkServiceImpl extends ServiceImpl<DocumentChunkMapper, DocumentChunk> implements DocumentChunkService {


}
