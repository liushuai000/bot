package org.example.bot.accountBot.service;

import lombok.extern.slf4j.Slf4j;
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



    public List selectAll() {
        System.out.println("selectAll");
       return mapper.selectAll();
    }

    public void insertUser(User user) {
        mapper.insertUser(user);
    }

    public void deleteHandele(String deleteName) {
        mapper.deleteHandle(deleteName);
    }
}
