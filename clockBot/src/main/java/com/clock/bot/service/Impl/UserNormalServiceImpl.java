package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.mapper.UserNormalMapper;
import com.clock.bot.pojo.UserNormal;
import com.clock.bot.service.UserNormalService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
@Slf4j
public class UserNormalServiceImpl implements UserNormalService {
    @Autowired
    UserNormalMapper mapper;
    public List<UserNormal> selectAll() {
       return mapper.selectList(null);
    }


    public UserNormal selectByUserAndGroupId(String UserId,String groupId) {
        QueryWrapper<UserNormal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserId);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public void insertUserNormal(UserNormal user) {
        mapper.insert(user);
    }
    public UserNormal repeatByUserIdOrUserName(String userId,String username,String groupId ){
        QueryWrapper<UserNormal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).or().eq("user_id", userId);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }



    public UserNormal findByUsername(String username, String groupId) {
        QueryWrapper<UserNormal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public void update(UserNormal UserNormal) {
        mapper.updateById(UserNormal);
    }

    public UserNormal selectByUserId(String userId,String groupId) {
        QueryWrapper<UserNormal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("group_id", groupId);
        queryWrapper.orderByDesc("create_time");
        return mapper.selectList(queryWrapper).get(0);
    }

    public List<UserNormal> selectByUserOperator(String groupId, boolean isOperator) {
        QueryWrapper<UserNormal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_operation", isOperator);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectList(queryWrapper);
    }

    public UserNormal selectByGroupId(String groupId) {
        List<UserNormal> userNormals = mapper.selectList(new QueryWrapper<UserNormal>().eq("group_id", groupId).orderByDesc("create_time"));
        if (userNormals.isEmpty()){
            return null;
        }else{
            return userNormals.get(0);
        }
    }
}
