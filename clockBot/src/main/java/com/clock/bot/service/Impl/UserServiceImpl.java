package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.clock.bot.mapper.UserMapper;
import com.clock.bot.pojo.User;
import com.clock.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;

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

    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    public void updateUserValidTime(User user, Date date) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.eq("user_id", user.getUserId());
        wrapper.set("valid_time", date);
        userMapper.update(user,wrapper);
    }
}
