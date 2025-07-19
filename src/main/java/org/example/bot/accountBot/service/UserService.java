package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.UserMapper;

import org.example.bot.accountBot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Component
@Service
@Slf4j
public class UserService {
    @Autowired
    UserMapper mapper;
    public List<User> selectAll() {
       return mapper.selectList(null);
    }
    //查询是否已经在数据库的操作员
    public User findByUserId(String UserId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserId);
        return mapper.selectOne(queryWrapper);
    }

    //查询是否已经在数据库的操作员
    public User findByUsername(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userName).or().eq("history_username",userName);
        return mapper.selectOne(queryWrapper);
    }

    public void insertUser(User user) {
        mapper.insert(user);
    }

    public void deleteHandler(String deleteName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", deleteName);
        mapper.delete(queryWrapper);
    }

    public void updateUsername(User user) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.set("user_id", user.getUserId());
        wrapper.set("last_name", user.getLastName());
        wrapper.set("first_name", user.getFirstName());
        wrapper.eq("username", user.getUsername());
        mapper.update(user,wrapper);
    }

    public void updateUserid(User user) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.set("username", user.getUsername());
        wrapper.set("last_name", user.getLastName());
        wrapper.set("first_name", user.getFirstName());
        wrapper.eq("user_id", user.getUserId());
        mapper.update(user,wrapper);
    }

    public void updateUser(User user) {
        mapper.updateById(user);
    }

    public void updateUserValidTime(User user, Date date) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.eq("user_id", user.getUserId());
        wrapper.set("valid_time", date);
        mapper.update(user,wrapper);
    }
    public User selectUserNameOrUserId(String username,String userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.or().eq("user_id", userId);
        return mapper.selectOne(queryWrapper);
    }

    public User findByFirstName(String deleteName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("first_name", deleteName);
        return mapper.selectOne(queryWrapper);
    }
}
