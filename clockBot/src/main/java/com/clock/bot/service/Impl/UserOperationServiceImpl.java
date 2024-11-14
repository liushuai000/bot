package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.mapper.UserMapper;
import com.clock.bot.mapper.UserOperationMapper;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.service.UserOperationService;
import com.clock.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Service
@Slf4j
public class UserOperationServiceImpl implements UserOperationService {
    @Autowired
    UserOperationMapper userOperationMapper;


    @Override
    public void insertUserOperation(UserOperation userOperation) {
        userOperationMapper.insert(userOperation);
    }

    @Override
    public List<UserOperation> findByOperation(int userStatusId, String matchedCommand) {
        QueryWrapper<UserOperation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_status_id", userStatusId);
        wrapper.eq("operation", matchedCommand);
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        wrapper.ge("create_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                .le("create_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
        return userOperationMapper.selectList(wrapper);

    }

    @Override
    public UserOperation findById(String userOperationId) {
        return userOperationMapper.selectById(userOperationId);
    }

    @Override
    public void updateUserOperation(UserOperation userOperation) {
        userOperationMapper.updateById(userOperation);
    }

    @Override
    public List<UserOperation> findByStatusId(String userStatusId) {
        QueryWrapper<UserOperation> wrapper = new QueryWrapper<>();
        wrapper.eq("user_status_id", userStatusId);
        return userOperationMapper.selectList(wrapper);
    }

    @Override
    public List<UserOperation> findByUserStatusIds(List<Integer> userStatusIds) {
        QueryWrapper<UserOperation> wrapper = new QueryWrapper<>();
        wrapper.in("user_status_id", userStatusIds);
        return userOperationMapper.selectList(wrapper);
    }


}
