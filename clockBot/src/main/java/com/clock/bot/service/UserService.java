package com.clock.bot.service;

import com.clock.bot.pojo.User;

import java.util.Date;

public interface UserService {
    User findByUserId(String userId);

    void insertUser(User byUser);

    void updateUser(User user);

    void updateUserValidTime(User user, Date validTime);
}
