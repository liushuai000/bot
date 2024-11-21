package com.clock.bot.service;

import com.clock.bot.pojo.Status;
import com.clock.bot.pojo.User;

import java.util.List;

public interface StatusService {


    Status getInitStatus(String groupId, String groupTitle);

    void update(Status status);

    List<Status> selectStatusList();
}
