package com.clock.bot.service;

import com.clock.bot.pojo.User;

public interface UserService {
    User findByUserId(String userId);

    void insertUser(User byUser);
}
