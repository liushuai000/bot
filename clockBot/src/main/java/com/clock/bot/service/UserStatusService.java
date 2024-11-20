package com.clock.bot.service;

import com.clock.bot.dto.*;
import com.clock.bot.pojo.User;
import com.clock.bot.pojo.UserStatus;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface UserStatusService {

    UserStatus selectUserStatus(UserDTO userDTO);

    void insertUserStatus(UserStatus userStatus);

    void updateUserStatus(UserStatus userStatus);

    List<UserStatus> selectTodayUserStatus(String userId, String groupId);

    List<StatusFromType> findClockList(QueryType queryType);

    List<OperationFromType> findOperationList(OperationType queryType);

    List<UserStatus> findUserStatusWithUserOperationId();

    List<UserStatus> findByGroupId(String groupId);
}
