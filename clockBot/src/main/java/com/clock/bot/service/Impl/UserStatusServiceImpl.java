package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.dto.UserDTO;
import com.clock.bot.mapper.UserStatusMapper;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.UserStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Service
@Slf4j
public class UserStatusServiceImpl implements UserStatusService {

    @Autowired
    private UserStatusMapper userStatusMapper;


    @Override
    public UserStatus selectUserStatus( UserDTO userDTO) {
        QueryWrapper<UserStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userDTO.getUserId());
        wrapper.eq("group_id", userDTO.getGroupId());
        wrapper.eq("status", true);
        wrapper.orderByDesc("create_time");
        wrapper.last(" limit 1");
        return userStatusMapper.selectOne(wrapper);
    }

    @Override
    public void insertUserStatus(UserStatus userStatus) {
        userStatusMapper.insert(userStatus);
    }

    @Override
    public void updateUserStatus(UserStatus userStatus) {
        userStatusMapper.updateById(userStatus);
    }
}
