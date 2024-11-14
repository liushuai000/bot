package com.clock.bot.service;

import com.clock.bot.pojo.UserOperation;

import java.util.List;

public interface UserOperationService {


    void insertUserOperation(UserOperation userOperation);

    List<UserOperation> findByOperation(int id, String matchedCommand);
}
