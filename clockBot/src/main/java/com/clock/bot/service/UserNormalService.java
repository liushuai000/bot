package com.clock.bot.service;

import com.clock.bot.pojo.Status;
import com.clock.bot.pojo.UserNormal;

import java.util.List;

public interface UserNormalService {


    UserNormal selectByUserAndGroupId(String userId, String chatId);

    void insertUserNormal(UserNormal userNormal);

    void update(UserNormal userNormal);

    UserNormal selectByGroupId(String groupId);

    UserNormal selectByUserId(String userId, String groupId);
}
