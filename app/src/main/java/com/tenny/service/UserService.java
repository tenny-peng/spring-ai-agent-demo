package com.tenny.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tenny.entity.User;
import com.tenny.entity.dto.LoginResponse;
import com.tenny.entity.dto.UserPageReq;
import com.tenny.entity.dto.UserPageVO;

public interface UserService extends IService<User> {
    User register(String username,  String email, String password);
    LoginResponse login(String username, String password);
    void logout(String token);

    Page<UserPageVO> getUserPage(UserPageReq req);
}
