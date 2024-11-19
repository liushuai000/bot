package com.clock.bot.service;

import com.clock.bot.dto.QueryType;
import com.clock.bot.dto.ReturnClockFromType;
import com.clock.bot.dto.UserDTO;
import com.clock.bot.pojo.User;
import com.clock.bot.pojo.UserStatus;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface UserStatusService {

    UserStatus selectUserStatus(UserDTO userDTO);

    void insertUserStatus(UserStatus userStatus);

    void updateUserStatus(UserStatus userStatus);

    List<UserStatus> selectTodayUserStatus(String userId, String groupId);

    List<ReturnClockFromType> findClockList(QueryType queryType);
}
