package com.tenny.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.entity.Message;
import com.tenny.mapper.MessageMapper;
import com.tenny.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

}
