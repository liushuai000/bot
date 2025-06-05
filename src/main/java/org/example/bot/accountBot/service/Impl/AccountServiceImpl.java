package org.example.bot.accountBot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import org.example.bot.accountBot.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Resource
    @Qualifier("accountMapper1")
    AccountMapper accountMapper;

    @Autowired
    RateMapper rateMapper;
    @Autowired
    UserMapper userMapper;
    AccountAssembler accountAssembler = new AccountAssembler();
    @Autowired
    private IssueMapper issueMapper;
    @Autowired
    private RateService rateService;
    @Autowired
    private StatusService statusService;

    public ReturnFromType findAccountByGroupId(QueryType queryType) {
        Date addTime = queryType.getAddTime();
        Date addEndTime = queryType.getAddEndTime();
        String username = queryType.getUsername();
        String groupId = queryType.getGroupId();
        boolean findAll = queryType.isFindAll();
        Boolean operation = queryType.getOperation();
        ReturnFromType returnFromType = new ReturnFromType();
        if (StringUtils.isBlank(queryType.getGroupId())){
            return null;
        }
        try {
            Status status = statusService.selectGroupId(groupId);
            if (status!=null){
                if (!status.isRiqie()){//不是日切
                    LocalDateTime tomorrow = LocalDateTime.now().plusDays(-1);
                    Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
                    returnFromType.setStartTime(validTime);
                    returnFromType.setStartEndTime(new Date());
                }else {
                    Date originalSetTime = status.getSetTime();
                    // 将 Date 转换为 LocalDateTime
                    LocalDateTime localDateTime = originalSetTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    // 减去一天
                    LocalDateTime newLocalDateTime = localDateTime.minusDays(1);
                    // 将 LocalDateTime 转换回 Date
                    Date newSetTime = Date.from(newLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    returnFromType.setStartTime(newSetTime);//日切开始时间
                    returnFromType.setStartEndTime(status.getSetTime());
                }
            }
            List<AccountDTO> accountDTOList=this.getAccountDTO(returnFromType,addTime,addEndTime,username,groupId,findAll,operation,status);
            returnFromType.setAccountData(accountDTOList);
            List<IssueDTO> issueDTOList=this.getIssueDTO(returnFromType,addTime,addEndTime,username,groupId,findAll,operation,status);
            returnFromType.setIssueData(issueDTOList);
            if (accountDTOList==null && issueDTOList==null)return returnFromType;
            Rate rate = rateService.selectRateList(groupId).get(0);
            returnFromType.setRateData(accountAssembler.rateToDTO(rate,accountDTOList,issueDTOList));
            List<CallbackUserDTO> callbackUserDTOList=this.getCallbackDTO(accountDTOList,issueDTOList);
            returnFromType.setCallbackData(callbackUserDTOList);
            List<OperationUserDTO> operationUserDTOList=this.getOperationUserDTO(accountDTOList,issueDTOList);
            returnFromType.setOperationData(operationUserDTOList);
            return returnFromType;
        } catch (Exception e) {
            // 记录日志或返回空列表
            e.printStackTrace();
            return null;
        }
    }
    private boolean isCallBackUserIdNull(AccountDTO accountDTO) {
        return accountDTO != null && accountDTO.getCallBackUserId() == null;
    }
    private boolean isCallBackUserIdNoNull(AccountDTO accountDTO) {
        return accountDTO != null && accountDTO.getCallBackUserId() != null;
    }
    private boolean isCallBackUserIdNullIssue(IssueDTO issueDTO) {
        return issueDTO != null && issueDTO.getCallBackUserId() == null;
    }
    private boolean isCallBackUserIdNoNullIssue(IssueDTO issueDTO) {
        return issueDTO != null && issueDTO.getCallBackUserId() != null;
    }
    //accountSummary.setExchange(exchange);//如果需要在前端显示  直接在后端计算好USDT在前端显示
    private List<OperationUserDTO> getOperationUserDTO(List<AccountDTO> accountDTOList,List<IssueDTO> issueDTOList) {
        Map<String, OperationUserDTO> summaryMap = new ConcurrentHashMap<>();
        if (accountDTOList!=null){
            accountDTOList.stream().filter(Objects::nonNull)
                    .forEach(accountDTO -> {
                        String userId = accountDTO.getUserId();
                        BigDecimal total = accountDTO.getTotal();
                        BigDecimal downing = accountDTO.getDowning();
                        BigDecimal exchange = accountDTO.getExchange();
                        BigDecimal rate = accountDTO.getRate();
                        OperationUserDTO accountSummary = summaryMap.get(userId);
                        if (accountSummary==null){
                            accountSummary = new OperationUserDTO();
                            accountSummary.addTotal(total);
                            BigDecimal totalUSDT = total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP);
                            accountSummary.addTotalUSDT(totalUSDT);
                            accountSummary.addIssueDowning(downing);
                            accountSummary.addIssueDowningUSDT(downing.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                            accountSummary.incrementCount();
                            summaryMap.put(userId,accountSummary);
                        }else {
                            accountSummary.addIssueDowning(downing);
                            accountSummary.addIssueDowningUSDT(downing.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                            accountSummary.incrementCount();
                            accountSummary.addTotal(total);
                            BigDecimal totalUSDT = total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP);
                            accountSummary.addTotalUSDT(totalUSDT);
                        }
                        accountSummary.setRate(rate.multiply(BigDecimal.valueOf(0.01)));
                        accountSummary.setExchange(exchange);
                        accountSummary.setGroupId(accountDTO.getGroupId());
                        accountSummary.setOperationName(accountDTO.getUsername());
                        accountSummary.setOperationFirstName(accountDTO.getFirstName());
                    });
        }
       if (issueDTOList!=null){
        issueDTOList.stream().filter(Objects::nonNull)
                .forEach(issueDTO -> {
                    String userId = issueDTO.getUserId();
                    BigDecimal total = issueDTO.getDowned();
                    BigDecimal down = issueDTO.getDown();//未下发
                    BigDecimal exchange = issueDTO.getExchange();
//                    BigDecimal rate = issueDTO.getRate();
                    OperationUserDTO accountSummary = summaryMap.get(userId);
                    if (accountSummary==null){
                        accountSummary = new OperationUserDTO();
                        accountSummary.addIssueTotal(total);
                        accountSummary.addIssueTotalUSDT(total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                        accountSummary.IssueIncrementCount();
                        summaryMap.put(userId,accountSummary);
                    }else {
                        accountSummary.IssueIncrementCount();
                        accountSummary.addIssueTotal(total);
                        accountSummary.addIssueTotalUSDT(total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                    }
                    accountSummary.setDown(down);
                    accountSummary.setExchange(exchange);
                    accountSummary.setGroupId(issueDTO.getGroupId());
                    accountSummary.setOperationName(issueDTO.getUsername());
                    accountSummary.setOperationFirstName(issueDTO.getFirstName());
                });
       }
        List<OperationUserDTO> result = new ArrayList<>(summaryMap.values());
        result.stream().filter(Objects::nonNull).forEach(OperationUserDTO::calcDown);
        return result;
    }

    private List<CallbackUserDTO> getCallbackDTO(List<AccountDTO> accountDTOList,List<IssueDTO> issueDTOList) {
        Map<String, CallbackUserDTO> summaryMap = new ConcurrentHashMap<>();
        if (accountDTOList!=null){
            accountDTOList.stream().filter(Objects::nonNull).filter(this::isCallBackUserIdNoNull)
                    .forEach(accountDTO -> {
                        String userId = accountDTO.getUserId();
                        BigDecimal total = accountDTO.getTotal();
                        BigDecimal downing = accountDTO.getDowning();
                        BigDecimal exchange = accountDTO.getExchange();
                        BigDecimal rate = accountDTO.getRate();
                        CallbackUserDTO accountSummary = summaryMap.get(userId);
                        if (accountSummary==null){
                            accountSummary = new CallbackUserDTO();
                            accountSummary.addTotal(total);
                            BigDecimal totalUSDT = total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP);
                            accountSummary.addTotalUSDT(totalUSDT);
                            accountSummary.addIssueDowning(downing);
                            accountSummary.addIssueDowningUSDT(downing.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                            accountSummary.incrementCount();
                            summaryMap.put(userId,accountSummary);
                        }else {
                            accountSummary.incrementCount();
                            accountSummary.addTotal(total);
                            BigDecimal totalUSDT = total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP);
                            accountSummary.addTotalUSDT(totalUSDT);
                            accountSummary.addIssueDowning(downing);
                            accountSummary.addIssueDowningUSDT(downing.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                        }
                        accountSummary.setExchange(exchange);
                        accountSummary.setRate(rate.multiply(BigDecimal.valueOf(0.01)));
                        accountSummary.setGroupId(accountDTO.getGroupId());
                        accountSummary.setCallBackName(accountDTO.getCallBackName());
                        accountSummary.setCallBackFirstName(accountDTO.getCallBackFirstName());
                    });
        }
        if (issueDTOList!=null){
            issueDTOList.stream().filter(Objects::nonNull).filter(this::isCallBackUserIdNoNullIssue)
                    .forEach(issueDTO -> {
                        String userId = issueDTO.getUserId();
                        BigDecimal total = issueDTO.getDowned();
                        BigDecimal down = issueDTO.getDown();//未下发
//                        BigDecimal downing = issueDTO.getDowning();
                        BigDecimal exchange = issueDTO.getExchange();
//                        BigDecimal rate = issueDTO.getRate();// 出账没有费率
                        CallbackUserDTO accountSummary = summaryMap.get(userId);
                        if (accountSummary==null){
                            accountSummary = new CallbackUserDTO();
                            accountSummary.addIssueTotal(total);
                            accountSummary.addIssueTotalUSDT(total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
//                            accountSummary.addIssueDowning(downing);
                            accountSummary.IssueIncrementCount();
                            summaryMap.put(userId,accountSummary);
                        }else {
                            accountSummary.IssueIncrementCount();
//                            accountSummary.addIssueDowning(downing);
                            accountSummary.addIssueTotalUSDT(total.divide(exchange, 2, BigDecimal.ROUND_HALF_UP));
                            accountSummary.addIssueTotal(total);
                        }
                        accountSummary.setExchange(exchange);
                        accountSummary.setDown(down);
                        accountSummary.setGroupId(issueDTO.getGroupId());
                        accountSummary.setCallBackName(issueDTO.getCallBackName());
                        accountSummary.setCallBackFirstName(issueDTO.getCallBackFirstName());
                    });
        }
        List<CallbackUserDTO> result = new ArrayList<>(summaryMap.values());
        result.stream().filter(Objects::nonNull).forEach(CallbackUserDTO::calcDown);
        return result;

    }

    private List<IssueDTO> getIssueDTO(ReturnFromType returnFromType,Date addTime,Date addEndTime, String username, String groupId, boolean findAll, Boolean operation,Status status) {
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        if (!findAll) {//不查询全部数据
            if (status.isRiqie()){
                if (addTime==null){
                    queryWrapper.ge("add_time", returnFromType.getStartTime()).le("add_time", returnFromType.getStartEndTime());
                }else{
                    queryWrapper.ge("add_time", addTime).le("add_time", addEndTime);
                }
            }else {
                LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
                queryWrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                        .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
            }
        }else {
            if (status.isRiqie()){
                queryWrapper.eq("riqie", status.isRiqie());
            }
        }
        queryWrapper.orderByDesc("add_time");
        List<Issue> issues = issueMapper.selectList(queryWrapper);
        List<Issue> issueList=issues;
        if (!findAll) {
            if (StringUtils.isNotBlank(username)) {
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.like("username", username.trim());
                User user = userMapper.selectOne(userQueryWrapper);
                if (user == null) {
                    throw new NoSuchElementException("User not found for username: " + username);
                }
                String userId = user.getUserId();
                issueList = issues.stream()
                        .filter(Objects::nonNull)
                        .filter(account -> {
                            if (operation==null) {
                                return account.getUserId().equals(userId)||userId.equals(account.getCallBackUserId());
                            } else if (operation){
                                return account.getUserId().equals(userId);
                            }else {
                                return userId.equals(account.getCallBackUserId());
                            }
                        }).collect(Collectors.toList());
            }
        }
        List<Integer> rateIds = issueList.stream().filter(Objects::nonNull).map(Issue::getRateId).distinct().collect(Collectors.toList());
        List<String> userIds = issueList.stream().filter(Objects::nonNull).map(Issue::getUserId).distinct().collect(Collectors.toList());
        List<String> callBackUserIds = issueList.stream().filter(Objects::nonNull).map(Issue::getCallBackUserId).distinct().collect(Collectors.toList());
        Map<Integer, Rate> rateMap;
        if(!rateIds.isEmpty()){
            rateMap=rateMapper.selectBatchIds(rateIds).stream().collect(Collectors.toMap(Rate::getId, rate -> rate));
        } else {
            rateMap = new HashMap<>();
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        QueryWrapper<User> callBackWrapper = new QueryWrapper<>();
        callBackWrapper.in("user_id", callBackUserIds);
        Map<String, User> userIdMap ;
        if (!userIds.isEmpty()){
            userIdMap=userMapper.selectList(wrapper).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        }else {
            userIdMap=new HashMap<>();
        }
        Map<String, User> callBackUserIdsMap ;
        if (!callBackUserIds.isEmpty()){
            callBackUserIdsMap=userMapper.selectList(callBackWrapper).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        }else {
            callBackUserIdsMap=new HashMap<>();
        }
        return issueList.stream().filter(Objects::nonNull).map(account -> accountAssembler.issueToDTO(account,
                rateMap.get(account.getRateId()),userIdMap.get(account.getUserId()),callBackUserIdsMap.get(account.getCallBackUserId()))
        ).collect(Collectors.toList());
    }

    public List<AccountDTO> getAccountDTO(ReturnFromType returnFromType,Date addTime,Date addEndTime, String username, String groupId, boolean findAll, Boolean operation,Status status) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        if (!findAll) {//不查询全部数据
            //第一次查询需要使用setStartTime
            if (status.isRiqie()){
                if (addTime==null){
                    queryWrapper.ge("add_time", returnFromType.getStartTime()).le("add_time", returnFromType.getStartEndTime());
                }else{
                    queryWrapper.ge("add_time", addTime).le("add_time", addEndTime);
                }
            }else {
                LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
                LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
                queryWrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                        .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
            }
        }else {
            if (status.isRiqie()){
                queryWrapper.eq("riqie", status.isRiqie());
            }
        }
        queryWrapper.orderByDesc("add_time");
        List<Account> accounts = accountMapper.selectList(queryWrapper);
        if (accounts.isEmpty())return null;
        List<Account> accountList = accounts;
        if (!findAll) {
            if (StringUtils.isNotBlank(username)) {
                QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                userQueryWrapper.like("username", username.trim());
                User user = userMapper.selectOne(userQueryWrapper);
                if (user == null) {
                    throw new NoSuchElementException("User not found for username: " + username);
                }
                String userId = user.getUserId();
                accountList = accounts.stream().filter(Objects::nonNull).filter(account -> {
                            if (operation==null) {
                                return account.getUserId().equals(userId)||userId.equals(account.getCallBackUserId());
                            } else if (operation){
                                return account.getUserId().equals(userId);
                            }else {
                                return userId.equals(account.getCallBackUserId());
                            }
                        }).collect(Collectors.toList());
            }
        }
        List<Integer> rateIds = accountList.stream().filter(Objects::nonNull).map(Account::getRateId).distinct().collect(Collectors.toList());
        List<String> userIds = accountList.stream().filter(Objects::nonNull).map(Account::getUserId).distinct().collect(Collectors.toList());
        List<String> callBackUserIds = accountList.stream().filter(Objects::nonNull).map(Account::getCallBackUserId).distinct().collect(Collectors.toList());
        Map<Integer, Rate> rateMap;
        if(!rateIds.isEmpty()){
            rateMap=rateMapper.selectBatchIds(rateIds).stream().collect(Collectors.toMap(Rate::getId, rate -> rate));
        } else {
            rateMap = new HashMap<>();
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.in("user_id", userIds);
        QueryWrapper<User> callBackWrapper = new QueryWrapper<>();
        callBackWrapper.in("user_id", callBackUserIds);
        Map<String, User> userIdMap ;
        if (!userIds.isEmpty()){
            userIdMap=userMapper.selectList(wrapper).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        }else {
            userIdMap=new HashMap<>();
        }
        Map<String, User> callBackUserIdsMap ;
        if (!callBackUserIds.isEmpty()){
            callBackUserIdsMap=userMapper.selectList(callBackWrapper).stream().collect(Collectors.toMap(User::getUserId, user -> user));
        }else {
            callBackUserIdsMap=new HashMap<>();
        }
        return accountList.stream().filter(Objects::nonNull).map(account -> accountAssembler.accountToDTO(account,
                rateMap.get(account.getRateId()),userIdMap.get(account.getUserId()),callBackUserIdsMap.get(account.getCallBackUserId()))
        ).collect(Collectors.toList());
    }







    /****************************  以下是针对数据的操作 **********************************/
    //查询没有开启日切的
    public List<Account> selectAccountRiqie(boolean riqie,Date setTime, String groupId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        if (riqie){//开启日切则查今日的日切数据
            queryWrapper.eq("riqie",riqie);//查询没有开启日切的
        }
//        else {//如果关闭了日切则查全部的
//            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
//            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
//            queryWrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
//                    .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
//        }
        queryWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        queryWrapper.orderByDesc("add_time");
        return accountMapper.selectList(queryWrapper);
    }
    public List<Account> selectAccounts( String groupId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        queryWrapper.orderByDesc("add_time");
        return accountMapper.selectList(queryWrapper);
    }


    public void insertAccount(Account account) {
        accountMapper.insert(account);
    }
    public void deleteById(int id) {
        accountMapper.deleteById(id);
    }
    public void deleteHistoryData(String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
//        updateWrapper.eq("riqie",0); 过期 或没过期的都删除
        accountMapper.delete(updateWrapper);
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
        accountMapper.delete(wrapper);
    }

    public void deleteInData(String id,String groupId) {
        accountMapper.delete(new QueryWrapper<Account>().eq("id",id).eq("group_id",groupId));
    }

    public void updateDown(BigDecimal add, String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);
        updateWrapper.set("down",add);
        accountMapper.update(null,updateWrapper);
    }

    public void updateNewestData(BigDecimal down,String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);
        updateWrapper.set("down",down);
        accountMapper.update(null,updateWrapper);
    }

}
