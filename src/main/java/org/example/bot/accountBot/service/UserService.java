package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.mapper.UserMapper;

import org.example.bot.accountBot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public List<User> selectByNormal(boolean isNormal) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_normal", isNormal);
        return mapper.selectList(queryWrapper);//显示操作人is_operation
    }
    //查询是否已经在数据库的操作员
    public User findByUsername(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userName);
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
//    public void updateUserByNormal(User user) {
//        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
//        wrapper.eq("username", user.getUsername());
//        wrapper.eq("user_id", user.getUserId());
//        wrapper.set("is_normal", user.isNormal());
//        mapper.update(user,wrapper);
//    }
    public void updateUserByOperation(User user) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.eq("username", user.getUsername());
        wrapper.eq("user_id", user.getUserId());
        wrapper.set("is_operation", user.isOperation());
        mapper.update(user,wrapper);
    }
//
    public void updateIsNormal(boolean isNormal,String username,User user) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.eq("username", username);
        wrapper.set("is_normal", isNormal);
        mapper.update(user,wrapper);
    }

    public void updateUserValidTime(User user, Date date) {
        UpdateWrapper<User> wrapper= new UpdateWrapper<>();
        wrapper.eq("user_id", user.getUserId());
        wrapper.set("valid_time", date);
        mapper.update(user,wrapper);
    }
}
