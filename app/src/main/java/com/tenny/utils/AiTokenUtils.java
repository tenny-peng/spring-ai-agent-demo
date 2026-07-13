package com.tenny.utils;

import com.tenny.entity.Message;

import java.util.List;

public class AiTokenUtils {

    public static int estimateToken(String text) {
        if (text == null) return 0;
        return text.length() * 2;  // 中文约 2 token/字
    }

    public static int estimateToken(List<Message> messages) {
        return messages.stream()
                .mapToInt(m -> estimateToken(m.getContent()))
                .sum();
    }
}
