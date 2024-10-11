package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    public void insertUser(User user) {
        mapper.insert(user);
    }

    public void deleteHandler(String deleteName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", deleteName);
        mapper.delete(queryWrapper);
    }
    //查询是否已经在数据库的操作员
    public User findByUsername(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userName);
        return mapper.selectOne(queryWrapper);
    }

    public List<User> findByUsernames(List<String> userNames) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("username", userNames);
        return mapper.selectList(queryWrapper);
    }
}
