package com.clock.bot.service;

import com.clock.bot.pojo.UserOperation;

import java.util.List;

public interface UserOperationService {


    void insertUserOperation(UserOperation userOperation);

    List<UserOperation> findByOperation(int userStatusId, String matchedCommand);

    UserOperation findById(String userOperationId);

    void updateUserOperation(UserOperation userOperation);

    List<UserOperation> findByStatusId(String userStatusId);

    List<UserOperation> findByUserStatusIds(List<Integer> userStatusIds);
}
