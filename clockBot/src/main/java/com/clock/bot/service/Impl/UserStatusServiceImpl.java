package com.clock.bot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.clock.bot.dto.*;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
    public List<OperationFromType> findOperationList(OperationType queryType) {
        QueryWrapper<UserStatus> queryWrapper = new QueryWrapper<>();
        if(queryType.getUserId()==null){
            return new ArrayList<>();
        }
        queryWrapper.eq("group_id", queryType.getGroupId());
        queryWrapper.eq("user_id", queryType.getUserId());
        if(queryType.getAddTime()!=null){
            queryWrapper.ge("create_time", queryType.getAddTime());
        }
        if(queryType.getAddEndTime()!=null){
            queryWrapper.le("create_time", queryType.getAddEndTime());
        }
        if (StringUtils.isNotBlank(queryType.getUsername())){
            queryWrapper.like("username", queryType.getUsername());
        }
        List<OperationFromType> fromTypeList=new ArrayList<>();
        List<UserStatus> userStatuses = userStatusMapper.selectList(queryWrapper);
        Map<String, AtomicLong > huoDongTimeMap = new HashMap<>();//总活动时长
        Map<String, AtomicLong > pureWorkTimeMap = new HashMap<>();//总工作时长  -总活动时长=有效工作时长
        userStatuses.stream().filter(Objects::nonNull).forEach(status->{
            if (status.getWorkDownTime()!=null){
                // 计算总工作时间
                Duration totalDuration = Duration.between(status.getWorkTime().toInstant(), status.getWorkDownTime().toInstant());
                AtomicLong wcTemp =pureWorkTimeMap.getOrDefault(status.getUserId(), new AtomicLong(0L)); // 纯工作时间
                wcTemp.addAndGet(totalDuration.getSeconds());
                pureWorkTimeMap.put(status.getUserId(), wcTemp);
            }

            QueryWrapper<UserOperation> wrapper = new QueryWrapper<>();
            wrapper.eq("user_status_id", status.getId());
            List<UserOperation> operationList = userOperationMapper.selectList(wrapper);
            operationList.stream().filter(Objects::nonNull).forEach(operation->{
                OperationFromType operationFromType = new OperationFromType();
                operationFromType.setUserId(status.getUserId());
                operationFromType.setUsername(status.getUsername());
                QueryWrapper<User> userWrapper = new QueryWrapper<>();
                userWrapper.eq("user_id", status.getUserId());
                User user = userMapper.selectOne(userWrapper);
                String firstName;
                if (user.getLastName()==null) firstName=user.getFirstName(); else firstName=user.getFirstName()+user.getLastName();
                operationFromType.setFirstname(firstName);
                operationFromType.setWorkTime(status.getWorkTime());
                operationFromType.setWorkDownTime(status.getWorkDownTime());
                if (operation.getOperation().equals("吃饭") && operation.getEndTime()!=null){
                    operationFromType.setEat(sdf.format(operation.getStartTime())+"至"+sdf.format(operation.getEndTime()));
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    operationFromType.setEatDateText(dateUtils.formatDuration(between.getSeconds()));
                } else if (operation.getOperation().equals("上厕所") && operation.getEndTime()!=null) {
                    operationFromType.setToilet(sdf.format(operation.getStartTime())+"至"+sdf.format(operation.getEndTime()));
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    operationFromType.setToiletDateText(dateUtils.formatDuration(between.getSeconds()));
                } else if (operation.getOperation().equals("抽烟") && operation.getEndTime()!=null) {
                    operationFromType.setSmoking(sdf.format(operation.getStartTime())+"至"+sdf.format(operation.getEndTime()));
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    operationFromType.setSmokingDateText(dateUtils.formatDuration(between.getSeconds()));
                }else if (operation.getOperation().equals("其它") && operation.getEndTime()!=null) {
                    operationFromType.setOther(sdf.format(operation.getStartTime())+"至"+sdf.format(operation.getEndTime()));
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    operationFromType.setOtherDateText(dateUtils.formatDuration(between.getSeconds()));
                }
                if (operation.getEndTime()!=null){
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    AtomicLong wcTemp = huoDongTimeMap.getOrDefault(status.getUserId(), new AtomicLong(0L));
                    wcTemp.addAndGet(between.getSeconds());
                    huoDongTimeMap.put(status.getUserId(),wcTemp);
                }
                fromTypeList.add(operationFromType);
            });

        });
        return fromTypeList;
    }

    @Override
    public List<UserStatus> findUserStatusWithUserOperationId() {
        //查询不为空的操作记录
        QueryWrapper<UserStatus> queryWrapper=new QueryWrapper<>();
        queryWrapper.ne("user_operation_id", "");
        return userStatusMapper.selectList(queryWrapper);
    }

    @Override
    public List<UserStatus> findByGroupId(String groupId) {
        QueryWrapper<UserStatus> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        return userStatusMapper.selectList(queryWrapper);
    }


    @Override
    public List<StatusFromType> findClockList(QueryType queryType) {
        List<StatusFromType> fromTypeList=new ArrayList<>();
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

        // 创建一个Map来存储每个用户的吃饭次数
        Map<String, Integer> eatCountMap = new HashMap<>();
        Map<String, Integer> wcCountMap = new HashMap<>();
        Map<String, Integer> smokingCountMap = new HashMap<>();
        Map<String, Integer> otherCountMap = new HashMap<>();
        // 创建一个Map来存储每个用户的吃饭次数
        Map<String, AtomicLong > eatTimeMap = new HashMap<>();
        Map<String, AtomicLong > wcTimeMap = new HashMap<>();
        Map<String, AtomicLong > smokingTimeMap = new HashMap<>();
        Map<String, AtomicLong > otherTimeMap = new HashMap<>();
        Map<String, AtomicLong > huoDongTimeMap = new HashMap<>();//总活动时长
        Map<String, AtomicLong > pureWorkTimeMap = new HashMap<>();//总工作时长  -总活动时长=有效工作时长
        Map<String, StatusFromType> statusFromTypeMap = new HashMap<>();
        Map<String,Date> workFirstTimeMap = new HashMap<>();
        Map<String,Date> workEndDownTimeMap = new HashMap<>();
        userStatuses.stream().filter(Objects::nonNull).forEach(status->{
            StatusFromType statusFromType=new StatusFromType();
            QueryWrapper<UserOperation> wrapper = new QueryWrapper<>();
            wrapper.eq("user_status_id", status.getId());
            List<UserOperation> operationList = userOperationMapper.selectList(wrapper);
            this.assemblerOperationList(status.getUserId(),operationList, eatCountMap,wcCountMap,smokingCountMap,otherCountMap,
                    eatTimeMap,wcTimeMap,smokingTimeMap,otherTimeMap,huoDongTimeMap);
            if (status.getWorkDownTime()!=null){
                // 计算总工作时间
                Duration totalDuration = Duration.between(status.getWorkTime().toInstant(), status.getWorkDownTime().toInstant());
                AtomicLong wcTemp =pureWorkTimeMap.getOrDefault(status.getUserId(), new AtomicLong(0L)); // 纯工作时间
                wcTemp.addAndGet(totalDuration.getSeconds());
                pureWorkTimeMap.put(status.getUserId(), wcTemp);
            }
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("user_id", status.getUserId());
            User user = userMapper.selectOne(userWrapper);
            statusFromType.setUserId(user.getUserId());
            String firstName;
            if (user.getLastName()==null) firstName=user.getFirstName(); else firstName=user.getFirstName()+user.getLastName();
            statusFromType.setFirstname(firstName);
            statusFromType.setUsername(user.getUsername());
            statusFromType.setGroupId(status.getGroupId());
            // 处理最早工作时间
            Date currentWorkTime = status.getWorkTime();
            if (currentWorkTime != null) {
                Date existingWorkFirstTime = workFirstTimeMap.get(status.getUserId());
                if (existingWorkFirstTime == null || currentWorkTime.before(existingWorkFirstTime)) {
                    workFirstTimeMap.put(status.getUserId(), currentWorkTime);
                }
            }
            // 处理最早工作时间
            Date currentWorkDownTime = status.getWorkDownTime();
            if (currentWorkDownTime != null) {
                // 处理最晚工作时间
                Date existingWorkEndDownTime = workEndDownTimeMap.get(status.getUserId());
                if (existingWorkEndDownTime == null || currentWorkDownTime.after(existingWorkEndDownTime)) {
                    workEndDownTimeMap.put(status.getUserId(), currentWorkDownTime);
                }
            }
            if (!statusFromTypeMap.containsKey(status.getUserId())) {
                statusFromTypeMap.put(status.getUserId(), statusFromType);
            }
        });
        statusFromTypeMap.forEach((userId, statusFromType) -> {
            Integer eatCount = eatCountMap.getOrDefault(userId, 0);
            Integer wcCount = wcCountMap.getOrDefault(userId, 0);
            Integer smokingCount = smokingCountMap.getOrDefault(userId, 0);
            Integer otherCount = otherCountMap.getOrDefault(userId, 0);
            statusFromType.setEat(eatCount);
            statusFromType.setToilet(wcCount);
            statusFromType.setSmoking(smokingCount);
            statusFromType.setOther(otherCount);
            AtomicLong eat = eatTimeMap.getOrDefault(userId, new AtomicLong(0L));
            AtomicLong wc = wcTimeMap.getOrDefault(userId, new AtomicLong(0L));
            AtomicLong smoking = smokingTimeMap.getOrDefault(userId, new AtomicLong(0L));
            AtomicLong other = otherTimeMap.getOrDefault(userId, new AtomicLong(0L));
            AtomicLong pureWork = pureWorkTimeMap.getOrDefault(userId, new AtomicLong(0L));
            AtomicLong huoDong = huoDongTimeMap.getOrDefault(userId, new AtomicLong(0L));
            statusFromType.setEatTime(dateUtils.formatDuration(eat.get()));
            statusFromType.setToiletTime(dateUtils.formatDuration(wc.get()));
            statusFromType.setSmokingTime(dateUtils.formatDuration(smoking.get()));
            statusFromType.setOtherTime(dateUtils.formatDuration(other.get()));
            statusFromType.setPureWorkTimeString(dateUtils.formatDuration(pureWork.get()-huoDong.get()));
            statusFromType.setHuoDongTime(dateUtils.formatDuration(huoDong.get()));
            statusFromType.setShangbanTime(dateUtils.formatDuration(pureWork.get()));
            statusFromType.setWorkFirstTime(workFirstTimeMap.get(userId));
            statusFromType.setWorkEndDownTime(workEndDownTimeMap.get(userId));
            fromTypeList.add(statusFromType);
        });
        return fromTypeList;
    }

    public void assemblerOperationList(String userId, List<UserOperation> operations, Map<String, Integer> eatCountMap, Map<String, Integer> wcMap,
                                       Map<String, Integer> smokingMap, Map<String, Integer> otherMap, Map<String, AtomicLong > eatTimeMap,
                                       Map<String, AtomicLong> wcTimeMap, Map<String, AtomicLong > smokingTimeMap, Map<String, AtomicLong> otherTimeMap,
                                        Map<String, AtomicLong > huoDongTimeMap){
        operations.stream().filter(Objects::nonNull).forEach(operation->{
            //"吃饭","上厕所","抽烟","其它"
            if (operation.getOperation().equals("吃饭")){
                int eatCount = eatCountMap.getOrDefault(userId, 0) + 1;
                eatCountMap.put(userId, eatCount);
                if (operation.getEndTime()!=null){
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    AtomicLong wcTemp = eatTimeMap.getOrDefault(userId, new AtomicLong(0L));
                    wcTemp.addAndGet(between.getSeconds());
                    eatTimeMap.put(userId, wcTemp);
                }
            }else if (operation.getOperation().equals("上厕所")) {
                int eatCount = wcMap.getOrDefault(userId, 0) + 1;
                wcMap.put(userId, eatCount);
                if (operation.getEndTime()!=null){
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    AtomicLong wcTemp = wcTimeMap.getOrDefault(userId, new AtomicLong(0L));
                    wcTemp.addAndGet(between.getSeconds());
                    wcTimeMap.put(userId, wcTemp);
                }
            }else if (operation.getOperation().equals("抽烟")) {
                int eatCount = smokingMap.getOrDefault(userId, 0) + 1;
                smokingMap.put(userId, eatCount);
                if (operation.getEndTime()!=null){
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    AtomicLong wcTemp = smokingTimeMap.getOrDefault(userId, new AtomicLong(0L));
                    wcTemp.addAndGet(between.getSeconds());
                    smokingTimeMap.put(userId, wcTemp);
                }

            }else if (operation.getOperation().equals("其它")) {
                int eatCount = otherMap.getOrDefault(userId, 0) + 1;
                otherMap.put(userId, eatCount);
                if (operation.getEndTime()!=null){
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    AtomicLong wcTemp = otherTimeMap.getOrDefault(userId, new AtomicLong(0L));
                    wcTemp.addAndGet(between.getSeconds());
                    otherTimeMap.put(userId, wcTemp);
                }
            }
            if (operation.getEndTime()!=null){
                Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                AtomicLong wcTemp = huoDongTimeMap.getOrDefault(userId, new AtomicLong(0L));
                wcTemp.addAndGet(between.getSeconds());
                huoDongTimeMap.put(userId,wcTemp);
            }
        });
    }


    public List<ReturnClockFromType> findClockList1(QueryType queryType) {
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
                        +": "+sdf.format(operation.getStartTime())+"至"+sdf.format(operation.getEndTime())+"\n"));
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
