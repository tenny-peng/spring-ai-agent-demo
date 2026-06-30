package com.tenny.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.entity.Message;
import com.tenny.entity.UserMemory;
import com.tenny.enums.MemoryCategory;
import com.tenny.enums.MemorySource;
import com.tenny.enums.MessageRole;
import com.tenny.mapper.UserMemoryMapper;
import com.tenny.service.MessageService;
import com.tenny.service.UserMemoryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserMemoryServiceImpl extends ServiceImpl<UserMemoryMapper, UserMemory> implements UserMemoryService {

    private final MessageService messageService;

    private final ChatClient chatClient;

    public UserMemoryServiceImpl(MessageService messageService, ChatModel chatModel) {
        this.messageService = messageService;
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public List<UserMemory> listByUserId(Long userId) {
        return lambdaQuery()
                .eq(UserMemory::getUserId, userId)
                .orderByDesc(UserMemory::getUpdatedAt)
                .list();
    }

    @Override
    public void addMemory(Long userId, String content, String category) {
        UserMemory memory = new UserMemory();
        memory.setUserId(userId);
        memory.setContent(content);
        memory.setCategory(category != null ? category : MemoryCategory.OTHER.name());
        memory.setSource(MemorySource.MANUAL.name());
        memory.setConfidence(1);
        save(memory);
    }

    @Override
    public void deleteMemory(Long id, Long userId) {
        remove(new LambdaQueryWrapper<UserMemory>()
                .eq(UserMemory::getId, id)
                .eq(UserMemory::getUserId, userId));
    }

    @Override
    public String getMemoriesSummary(Long userId) {
        List<UserMemory> memories = listByUserId(userId);
        if (memories.isEmpty()) return "";
        return memories.stream()
                .map(m -> "- " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void extractFromConversation(String conversationId, Long userId) {
        List<Message> messages = messageService.lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .orderByDesc(Message::getCreatedAt)
                .last("LIMIT 6")
                .list();
        Collections.reverse(messages);
        if (messages.size() < 2) return;
        String dialogText = messages.stream()
                .map(m -> (MessageRole.USER.name().equals(m.getRole()) ? "用户" : "AI") + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
        List<UserMemory> existingMemories = listByUserId(userId);
        String existingText = existingMemories.isEmpty() ? "无" :
                existingMemories.stream().map(UserMemory::getContent).collect(Collectors.joining("\n"));
        String prompt = """
            以下是最近一段对话。请从中提取用户透露的个人信息、偏好、习惯等特征。
            
            要求：
            - 只提取关于**用户本人**的特征，不要提取AI的表达
            - 分类请从以下选择：PREFERENCE（偏好）、PERSONAL_INFO（个人信息）、HABIT（习惯）
            - 输出格式：分类|内容，每行一条
            - 如果对话中没有新信息，直接输出"无"
            
            以下是用户已有的记忆，请忽略已存在的内容：
            %s
            
            对话：
            %s
            """.formatted(existingText, dialogText);
        String result = chatClient.prompt().user(prompt).call().content();
        if (result == null || result.isBlank() || "无".equals(result.trim())) return;

        for (String line : result.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String category = MemoryCategory.OTHER.name();
            String content = line;
            if (line.contains("|")) {
                String[] parts = line.split("\\|", 2);
                String cat = parts[0].trim().toUpperCase();
                if (cat.equals(MemoryCategory.PREFERENCE.name()) || cat.equals(MemoryCategory.PERSONAL_INFO.name()) || cat.equals(MemoryCategory.HABIT.name())) {
                    category = cat;
                }
                content = parts[1].trim();
            }
            UserMemory existMemory = getOne(new LambdaQueryWrapper<UserMemory>().eq(UserMemory::getUserId, userId).eq(UserMemory::getContent, content));
            if(existMemory != null){
                update(new LambdaUpdateWrapper<UserMemory>().eq(UserMemory::getId, existMemory.getId()).set(UserMemory::getConfidence, existMemory.getConfidence() + 1));
            }else{
                UserMemory userMemory = new UserMemory();
                userMemory.setUserId(userId);
                userMemory.setContent(content);
                userMemory.setCategory(category);
                userMemory.setSource(MemorySource.CHAT_EXTRACT.name());
                userMemory.setSourceConversationId(conversationId);
                userMemory.setConfidence(1);
                save(userMemory);
            }
        }
    }
}