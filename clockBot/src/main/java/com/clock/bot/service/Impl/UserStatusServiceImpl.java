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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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

    @Override
    public List<UserStatus> selectTodayUserStatus(String userId, String groupId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        QueryWrapper<UserStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("group_id", groupId);
        wrapper.ge("create_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                .le("create_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
        return userStatusMapper.selectList(wrapper);
    }
}
