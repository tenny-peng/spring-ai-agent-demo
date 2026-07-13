package com.tenny.interceptor;

import com.alibaba.fastjson2.JSON;
import com.tenny.annotation.AdminRequired;
import com.tenny.annotation.AuthRequired;
import com.tenny.common.UserContext;
import com.tenny.entity.dto.LoginResponse;
import com.tenny.enums.UserRole;
import com.tenny.utils.AuthTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "token:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        boolean requiresAuth = hm.hasMethodAnnotation(AuthRequired.class);
        boolean requiresAdmin = hm.hasMethodAnnotation(AdminRequired.class);
        if (!requiresAuth && !requiresAdmin) return true;

        String token = AuthTokenUtils.extractToken(request);
        if (token == null) {
            writeUnauthorized(response);
            return false;
        }

        String loginResponseStr = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (loginResponseStr == null) {
            writeUnauthorized(response);
            return false;
        }

        LoginResponse loginResponse = JSON.parseObject(loginResponseStr, LoginResponse.class);

        if (requiresAdmin && !UserRole.ADMIN.name().equals(loginResponse.getRole())) {
            writeForbidden(response);
            return false;
        }

        UserContext.setUserId(loginResponse.getId());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = "{\"code\":401,\"message\":\"未登录或token已失效\"}";
        response.getWriter().write(json);
    }

    private void writeForbidden(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
        String json = "{\"code\":403,\"message\":\"无权限访问\"}";
        response.getWriter().write(json);
    }
}
