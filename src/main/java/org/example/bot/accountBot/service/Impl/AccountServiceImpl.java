package org.example.bot.accountBot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.assembler.AccountAssembler;
import org.example.bot.accountBot.dto.*;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.mapper.IssueMapper;
import org.example.bot.accountBot.mapper.RateMapper;
import org.example.bot.accountBot.mapper.UserMapper;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Autowired
    AccountMapper mapper;

    @Autowired
    RateMapper rateMapper;
    @Autowired
    UserMapper userMapper;
    AccountAssembler accountAssembler = new AccountAssembler();
    @Autowired
    private IssueMapper issueMapper;
    @Autowired
    private RateService rateService;

    public ReturnFromType findAccountByGroupId(QueryType queryType) {
        Date addTime = queryType.getAddTime();
        String username = queryType.getUsername();
        String groupId = queryType.getGroupId();
        boolean findAll = queryType.isFindAll();
        boolean operation = queryType.isOperation();
        ReturnFromType returnFromType = new ReturnFromType();
        if (StringUtils.isBlank(queryType.getGroupId())){
            return null;
        }
        try {
            List<AccountDTO> accountDTOList=this.getAccountDTO(addTime,username,groupId,findAll,operation);
            returnFromType.setAccountData(accountDTOList);
            List<IssueDTO> issueDTOList=this.getIssueDTO(addTime,username,groupId,findAll,operation);
            returnFromType.setIssueData(issueDTOList);
            List<CallbackUserDTO> callbackUserDTOList=this.getCallbackDTO(addTime,username,groupId,findAll,operation);
            returnFromType.setCallbackData(callbackUserDTOList);

            Rate rate = rateService.selectRateList(groupId).get(0);
            returnFromType.setRateData(accountAssembler.rateToDTO(rate));

            return returnFromType;
        } catch (Exception e) {
            // 记录日志或返回空列表
            e.printStackTrace();
            return null;
        }
    }

    private List<CallbackUserDTO> getCallbackDTO(Date addTime, String username, String groupId, boolean findAll, boolean operation) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        return null;
    }

    private List<IssueDTO> getIssueDTO(Date addTime, String username, String groupId, boolean findAll, boolean operation) {
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        if (!findAll) {
            queryWrapper.lt("add_time", addTime);
        }
        queryWrapper.orderByDesc("add_time");
        List<Issue> issues = issueMapper.selectList(queryWrapper);
        List<Issue> issueList;
        if (StringUtils.isNotBlank(username)) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            if (user == null) {
                throw new NoSuchElementException("User not found for username: " + username);
            }
            String userId = user.getUserId();
            issueList = issues.stream().filter(Objects::nonNull)
                    .filter(operation ? account -> account.getUserId().equals(userId) : account -> account.getCallBackUserId().equals(userId))
                    .collect(Collectors.toList());
        }else {
            issueList=issues;
        }
        List<Integer> rateIds = issueList.stream().filter(Objects::nonNull).map(Issue::getRateId).distinct().collect(Collectors.toList());
        List<String> userIds = issueList.stream().filter(Objects::nonNull).map(Issue::getUserId).distinct().collect(Collectors.toList());
        List<String> callBackUserIds = issueList.stream().filter(Objects::nonNull).map(Issue::getCallBackUserId).distinct().collect(Collectors.toList());
        Map<Integer, Rate> rateMap = rateMapper.selectBatchIds(rateIds).stream().collect(Collectors.toMap(Rate::getId, rate -> rate));
        Map<String, User> userIdMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        Map<String, User> callBackUserIdsMap = userMapper.selectBatchIds(callBackUserIds).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        return issueList.stream().filter(Objects::nonNull).map(account -> accountAssembler.issueToDTO(account,
                rateMap.get(account.getRateId()),userIdMap.get(account.getUserId()),callBackUserIdsMap.get(account.getCallBackUserId()))
        ).collect(Collectors.toList());
    }

    public List<AccountDTO> getAccountDTO(Date addTime, String username, String groupId, boolean findAll, boolean operation) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        if (!findAll) {
            queryWrapper.lt("add_time", addTime);
        }
        queryWrapper.orderByDesc("add_time");
        List<Account> accounts = mapper.selectList(queryWrapper);
        List<Account> accountList;
        if (StringUtils.isNotBlank(username)) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", username);
            User user = userMapper.selectOne(userQueryWrapper);
            if (user == null) {
                throw new NoSuchElementException("User not found for username: " + username);
            }
            String userId = user.getUserId();
            accountList = accounts.stream().filter(Objects::nonNull)
                    .filter(operation ? account -> account.getUserId().equals(userId) : account -> account.getCallBackUserId().equals(userId))
                    .collect(Collectors.toList());
        }else {
            accountList=accounts;
        }
        List<Integer> rateIds = accountList.stream().filter(Objects::nonNull).map(Account::getRateId).distinct().collect(Collectors.toList());
        List<String> userIds = accountList.stream().filter(Objects::nonNull).map(Account::getUserId).distinct().collect(Collectors.toList());
        List<String> callBackUserIds = accountList.stream().filter(Objects::nonNull).map(Account::getCallBackUserId).distinct().collect(Collectors.toList());
        Map<Integer, Rate> rateMap = rateMapper.selectBatchIds(rateIds).stream().collect(Collectors.toMap(Rate::getId, rate -> rate));
        Map<String, User> userIdMap = userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        Map<String, User> callBackUserIdsMap = userMapper.selectBatchIds(callBackUserIds).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        return accountList.stream().filter(Objects::nonNull).map(account -> accountAssembler.accountToDTO(account,
                        rateMap.get(account.getRateId()),userIdMap.get(account.getUserId()),callBackUserIdsMap.get(account.getCallBackUserId()))
        ).collect(Collectors.toList());
    }







    /****************************  以下是针对数据的操作 **********************************/
    //查询没有开启日切的
    public List<Account> selectAccountRiqie(boolean riqie,Date setTime, String groupId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("riqie",riqie);//查询没有开启日切的
        queryWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
//        queryWrapper.ge("add_time",setTime);//查询大于等于日切时间的账单
        queryWrapper.orderByDesc("add_time");
        return mapper.selectList(queryWrapper);
    }
    public List<Account> selectAccounts( String groupId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        queryWrapper.orderByDesc("add_time");
        return mapper.selectList(queryWrapper);
    }


    public void insertAccount(Account account) {
        mapper.insert(account);
    }
    public void deleteById(int id) {
        mapper.deleteById(id);
    }
    public void deleteHistoryData(String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
//        updateWrapper.eq("riqie",0); 过期 或没过期的都删除
        mapper.delete(updateWrapper);
    }

    @Override
    public void updateRiqie(int id,boolean riqie) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("riqie",riqie);
        mapper.update(null,updateWrapper);
    }

    @Override
    public void updateLastUpdateRiqie(int id,boolean riqie,Date updateTime) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("update_time",updateTime);
        updateWrapper.set("riqie",riqie);
        mapper.update(null,updateWrapper);
    }


    public void deleteTodayData(Status status, String groupId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        wrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
        if (!status.isRiqie()){
            wrapper.eq("riqie", false);
        }else {
            wrapper.eq("riqie", true);
        }
        mapper.delete(wrapper);
    }

    public void deleteInData(String id,String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.eq("group_id",groupId);
        mapper.delete(updateWrapper);
    }

    public void updateDown(BigDecimal add, String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);
        updateWrapper.set("down",add);
        mapper.update(null,updateWrapper);
    }

    public void updateNewestData(BigDecimal down,String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);
        updateWrapper.set("down",down);
        mapper.update(null,updateWrapper);
    }

}
