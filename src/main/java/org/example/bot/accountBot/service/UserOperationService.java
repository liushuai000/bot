package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.UserOperationMapper;
import org.example.bot.accountBot.pojo.UserOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
@Slf4j
public class UserOperationService {
    @Autowired
    UserOperationMapper mapper;
    public List<UserOperation> selectAll() {
       return mapper.selectList(null);
    }


    public UserOperation selectByUserAndGroupId(String UserId,String groupId) {
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", UserId);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public void insertUserOperation(UserOperation user) {
        mapper.insert(user);
    }

    public UserOperation findByUsername(String username, String groupId) {
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public void update(UserOperation UserOperation) {
        mapper.updateById(UserOperation);
    }

    public UserOperation selectByUserName(String username,String groupId) {
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
    }

    public List<UserOperation> selectByUserOperator(String groupId, boolean isOperator) {
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_operation", isOperator);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectList(queryWrapper);
    }

    public void deleteByUsername(String deleteName, String groupId) {
        mapper.delete(new QueryWrapper<UserOperation>().eq("username", deleteName).eq("group_id", groupId));
    }
    public void deleteByUserId(String userId, String groupId) {
        mapper.delete(new QueryWrapper<UserOperation>().eq("user_id", userId).eq("group_id", groupId));
    }
}
