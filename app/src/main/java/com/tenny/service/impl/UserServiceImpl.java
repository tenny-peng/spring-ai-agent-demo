package com.tenny.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tenny.common.BusinessException;
import com.tenny.entity.Conversation;
import com.tenny.entity.User;
import com.tenny.entity.dto.LoginResponse;
import com.tenny.entity.dto.UserPageReq;
import com.tenny.entity.dto.UserPageVO;
import com.tenny.mapper.ConversationMapper;
import com.tenny.mapper.UserMapper;
import com.tenny.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String TOKEN_PREFIX = "token:";
    private static final long TOKEN_TTL = 7 * 24 * 60 * 60;

    private final RedisTemplate<String, String> redisTemplate;

    private final ConversationMapper conversationMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String username, String email, String password) {
        User existing = lambdaQuery().eq(User::getUsername, username).one();
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        existing = lambdaQuery().eq(User::getEmail, email).one();
        if (existing != null) {
            throw new BusinessException("邮箱已存在");
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
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
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

    @Override
    public Page<UserPageVO> getUserPage(UserPageReq req) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(req.getUsername()), User::getUsername, req.getUsername())
                .orderByDesc(User::getCreatedAt);
        Page<User> userPage = page(new Page<>(req.getPage(), req.getSize()), wrapper);

        List<Long> userIds = userPage.getRecords().stream().map(User::getId).toList();
        List<Map<String, Object>> countList = conversationMapper.selectMaps(
                new QueryWrapper<Conversation>()
                        .select("user_id", "count(*) as cnt")
                        .in("user_id", userIds)
                        .groupBy("user_id")
        );
        Map<Long, Integer> countMap = countList.stream()
                .collect(Collectors.toMap(
                        m -> (Long) m.get("user_id"),
                        m -> ((Long) m.get("cnt")).intValue()
                ));

        List<UserPageVO> voList = userPage.getRecords().stream().map(user -> {
            UserPageVO vo = new UserPageVO();
            BeanUtils.copyProperties(user, vo);
            vo.setConversationCount(countMap.getOrDefault(user.getId(), 0));
            return vo;
        }).toList();

        Page<UserPageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

}
