package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.dto.QueryType;
import com.clock.bot.dto.ReturnClockFromType;
import com.clock.bot.dto.UserDTO;
import com.clock.bot.mapper.UserMapper;
import com.clock.bot.mapper.UserOperationMapper;
import com.clock.bot.mapper.UserStatusMapper;
import com.clock.bot.pojo.User;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.UserStatusService;
import com.clock.bot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Service
@Slf4j
public class UserStatusServiceImpl implements UserStatusService {
    @Autowired
    private UserStatusMapper userStatusMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserOperationMapper userOperationMapper;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateUtils dateUtils=new DateUtils();

    @Override
    public List<ReturnClockFromType> findClockList(QueryType queryType) {
        List<ReturnClockFromType> returnClockFromTypeList=new ArrayList<>();
        QueryWrapper<UserStatus> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("group_id", queryType.getGroupId());
        if(queryType.getAddTime()!=null){
            queryWrapper.ge("create_time", queryType.getAddTime());
        }
        if(queryType.getAddEndTime()!=null){
            queryWrapper.le("create_time", queryType.getAddEndTime());
        }
        if (StringUtils.isNotBlank(queryType.getUsername())){
            queryWrapper.like("username", queryType.getUsername());
        }
        List<UserStatus> userStatuses = userStatusMapper.selectList(queryWrapper);
        if (userStatuses.isEmpty())return new ArrayList<>();
        userStatuses.stream().filter(Objects::nonNull).forEach(status->{
            ReturnClockFromType returnClockFromType=new ReturnClockFromType();
            String userOperationId = status.getUserOperationId();//这个是最近一次的操作记录
            QueryWrapper<UserOperation> wrapper = new QueryWrapper<>();
            wrapper.eq("user_status_id", status.getId());
            List<UserOperation> operationList = userOperationMapper.selectList(wrapper);
            Map<String, Integer> map = new HashMap<>();//操作次数
            Map<String, Long> operationTime = new HashMap<>();//操作时长 吃饭:5秒
            Map<String, StringBuilder> operationHistoryTime = new HashMap<>();//吃饭:2024/11/18 13:30:00- 14:30:00
            StringBuilder operationTimeBuilder = new StringBuilder();
            if (!operationList.isEmpty()) operationTimeBuilder.append("操作记录时长: ");
            operationList.stream().filter(Objects::nonNull).forEach(operation -> {
                if (map.get(operation.getOperation())!=null) {
                    map.put(operation.getOperation(), map.getOrDefault(operation.getOperation(), 0) + 1);
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    operationTime.put(operation.getOperation(), operationTime.getOrDefault(operation.getOperation(), 0L) + between.getSeconds());
                }else {
                    map.put(operation.getOperation(), 1);
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    operationTime.put(operation.getOperation(), between.getSeconds());
                }
                operationHistoryTime.put(operation.getOperation(), operationTimeBuilder.append(operation.getOperation()
                        +": "+sdf.format(operation.getStartTime())+"-"+sdf.format(operation.getEndTime())+"\n"));
            });
            this.assemblerOperationMap(map, operationTime,operationList,operationHistoryTime,returnClockFromType);
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("user_id", status.getUserId());
            User user = userMapper.selectOne(userWrapper);
            returnClockFromType.setFirstname(user.getFirstName()+user.getLastName());
            returnClockFromType.setUsername(user.getUsername());
            returnClockFromType.setCreateTime(status.getCreateTime());
            returnClockFromType.setStatus(status.isStatus()==true?"上班":"下班");
            returnClockFromType.setUserId(status.getUserId());
            returnClockFromType.setGroupId(status.getGroupId());
            returnClockFromType.setGroupTitle(status.getGroupTitle());
            returnClockFromType.setWorkTime(status.getWorkTime());
            returnClockFromType.setWorkDownTime(status.getWorkDownTime());
            returnClockFromType.setReturnHome(status.isReturnHome());
            returnClockFromTypeList.add(returnClockFromType);
        });
        return returnClockFromTypeList;
    }

    public void assemblerOperationMap(Map<String, Integer> map,Map<String, Long> operationTime,List<UserOperation> operationList,
                                      Map<String, StringBuilder> operationHistoryTime,ReturnClockFromType returnClockFromType) {
        StringBuilder stringBuilder=new StringBuilder();
        StringBuilder stringBuilderTimeText=new StringBuilder();
        if (operationList.isEmpty())return;
        stringBuilder.append("操作次数: ");
        stringBuilderTimeText.append("操作时长: ");
        for (String key : map.keySet()) {
            stringBuilder.append(key + ":" + map.get(key) + "次"+" ");
            stringBuilderTimeText.append(key + ":" + DateUtils.formatDuration(operationTime.get(key))+" ");
            returnClockFromType.setOperationHistoryTime(operationHistoryTime.get(key).toString());
        }
        returnClockFromType.setOperationText(stringBuilder.toString());
        returnClockFromType.setOperationTime(stringBuilderTimeText.toString());
    }


    /*************************      以下是对数据的处理           *************************/
    @Override
    public UserStatus selectUserStatus( UserDTO userDTO) {
        QueryWrapper<UserStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userDTO.getUserId());
        wrapper.eq("group_id", userDTO.getGroupId());
        wrapper.eq("status", true);
        wrapper.orderByDesc("create_time");
        wrapper.last(" limit 1");
        return userStatusMapper.selectOne(wrapper);
    }

    @Override
    public void insertUserStatus(UserStatus userStatus) {
        userStatusMapper.insert(userStatus);
    }

    @Override
    public void updateUserStatus(UserStatus userStatus) {
        userStatusMapper.updateById(userStatus);
    }

    @Override
    public List<UserStatus> selectTodayUserStatus(String userId, String groupId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        QueryWrapper<UserStatus> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("group_id", groupId);
        wrapper.ge("create_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                .le("create_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
        return userStatusMapper.selectList(wrapper);
    }
}
