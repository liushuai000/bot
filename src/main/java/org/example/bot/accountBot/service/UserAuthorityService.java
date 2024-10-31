package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.UserAuthorityMapper;
import org.example.bot.accountBot.mapper.UserMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.pojo.UserAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Component
@Service
@Slf4j
public class UserAuthorityService {
    @Autowired
    UserAuthorityMapper mapper;
    public List<UserAuthority> selectAll() {
       return mapper.selectList(null);
    }


    public UserAuthority selectByUserAndGroupId(String UserId,String groupId) {
        QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserId);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public void insertUserAuthority(UserAuthority user) {
        mapper.insert(user);
    }
    public UserAuthority repeat(UserAuthority ua,String groupId ){
        QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", ua.getUsername()).or().eq("user_id", ua.getUserId());
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }



    public UserAuthority findByUsername(String username, String groupId) {
        QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public void update(UserAuthority userAuthority) {
        mapper.updateById(userAuthority);
    }

    public UserAuthority selectByUserId(String userId) {
        QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return mapper.selectOne(queryWrapper);
    }

    public List<UserAuthority> selectByUserOperator(String groupId, boolean isOperator) {
        QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_operation", isOperator);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectList(queryWrapper);
    }
}
