package com.tenny.utils;

import jakarta.servlet.http.HttpServletRequest;

public class TokenUtils {

    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
