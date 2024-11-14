package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.mapper.UserMapper;
import com.clock.bot.pojo.User;
import com.clock.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

/********************************   以下是对数据的处理    **********************************/
    @Override
    public User findByUserId(String userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public void insertUser(User byUser) {
        userMapper.insert(byUser);
    }
}
