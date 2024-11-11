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
    public UserOperation repeat(UserOperation ua,String groupId ){
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", ua.getUsername()).or().eq("user_id", ua.getUserId());
        queryWrapper.eq("group_id", groupId);
        return mapper.selectOne(queryWrapper);
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

    public UserOperation selectByUserIdAndName(String userId,String username,String groupId) {
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).or().eq("user_id", userId).eq("group_id", groupId);
        queryWrapper.orderByDesc("create_time");
        if (mapper.selectList(queryWrapper).isEmpty()){
            return null;
        }else {
            return mapper.selectList(queryWrapper).get(0);
        }
    }

    public List<UserOperation> selectByUserOperator(String groupId, boolean isOperator) {
        QueryWrapper<UserOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_operation", isOperator);
        queryWrapper.eq("group_id", groupId);
        return mapper.selectList(queryWrapper);
    }
}
