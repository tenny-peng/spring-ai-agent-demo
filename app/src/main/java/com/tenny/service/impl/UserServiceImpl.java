package com.tenny.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.entity.User;
import com.tenny.entity.dto.LoginResponse;
import com.tenny.mapper.UserMapper;
import com.tenny.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_TTL = 7 * 24 * 60 * 60;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String username, String email, String password) {
        User existing = lambdaQuery().eq(User::getUsername, username).one();
        if (existing != null) {
            throw new RuntimeException("用户名已存在");
        }
        existing = lambdaQuery().eq(User::getEmail, email).one();
        if (existing != null) {
            throw new RuntimeException("邮箱已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        save(user);
        return user;
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        LoginResponse loginResponse = new LoginResponse();
        BeanUtils.copyProperties(user, loginResponse);
        loginResponse.setToken(token);
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, JSON.toJSONString(loginResponse), TOKEN_TTL, TimeUnit.SECONDS);
        return loginResponse;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

}
