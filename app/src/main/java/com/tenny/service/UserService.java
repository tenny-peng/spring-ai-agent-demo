package com.tenny.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.User;
import com.tenny.entity.dto.LoginResponse;

public interface UserService extends IService<User> {
    User register(String username,  String email, String password);
    LoginResponse login(String username, String password);
    void logout(String token);
}
