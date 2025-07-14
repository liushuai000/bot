package org.example.bot.accountBot.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.assembler.AccountAssembler;
import org.example.bot.accountBot.botConfig.AccountBot;
import org.example.bot.accountBot.botConfig.MediaInfoConfig;
import org.example.bot.accountBot.dto.*;
import org.example.bot.accountBot.mapper.*;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.StatusService;
import org.example.bot.accountBot.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private LoginFromMapper loginFromMapper;
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;
    @Autowired
    private UserNormalMapper userNormalMapper;
    @Autowired
    private GroupInnerUserMapper groupInnerUserMapper;
    @Autowired
    private UserOperationMapper userOperationMapper;
    @Autowired
    private AccountBot accountBot;
    @Autowired
    private MediaInfoConfig mediaInfoConfig;
    @Autowired
    private GroupTagMapper groupTagMapper;
    @Autowired
    private ConfigEditMapper configEditMapper;
    @Autowired
    private ConfigEditButtonMapper configEditButtonMapper;
    @Autowired
    private UserOrderMapper userOrderMapper;
    @Autowired
    private AccountSettingMapper accountSettingMapper;
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
            returnFromType.setRateData(accountAssembler.rateToDTO(rate,accountDTOList,issueDTOList,status));
            List<CallbackUserDTO> callbackUserDTOList=this.getCallbackDTO(accountDTOList,issueDTOList);
            returnFromType.setCallbackData(callbackUserDTOList);
            List<OperationUserDTO> operationUserDTOList=this.getOperationUserDTO(accountDTOList,issueDTOList);
            returnFromType.setOperationData(operationUserDTOList);
            GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", groupId));
            returnFromType.setEnglish(groupInfoSetting.getEnglish());
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
                        String userId = accountDTO.getCallBackUserId();
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
                        String userId = issueDTO.getCallBackUserId();
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
        queryWrapper.orderByAsc("add_time");
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
        queryWrapper.orderByAsc("add_time");
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
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; //有效期 1 day
    private static final Date EXPIRATION_DATE = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
    @Override
    public JsonResult login(LoginFromDTO loginFromDTO) {
        LoginFrom loginFrom = loginFromMapper.selectOne(new QueryWrapper<LoginFrom>()
                .eq("username", loginFromDTO.getUsername()).eq("password", loginFromDTO.getPassword()));
        if (loginFrom==null){
            return new JsonResult("用户名或密码错误");
        }
        return new JsonResult(EXPIRATION_DATE);
    }
    @Override
    public JsonResult getUserList(QueryUserDTO queryDTO) {
        List<ReturnUserDTO> dtoList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        QueryWrapper<GroupInnerUser> wrapper = new QueryWrapper<>();
        if (queryDTO.getStartTime()!=null && StringUtils.isNotBlank(queryDTO.getStartTime())){
            LocalDate startDate = LocalDate.parse(queryDTO.getStartTime(), DateTimeFormatter.ISO_DATE);
            LocalDate endDate = LocalDate.parse(queryDTO.getEndTime(), DateTimeFormatter.ISO_DATE);
            // 补全时间为当天的 00:00:00 和 23:59:59
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            Date startDateValue = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Date endDateValue = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
            wrapper.between("last_time", startDateValue, endDateValue);
        }
        if (queryDTO.getNickname()!=null && StringUtils.isNotBlank(queryDTO.getNickname())){
            wrapper.or(w -> w.like("first_name", queryDTO.getNickname().trim())
                    .or().like("last_name", queryDTO.getNickname().trim())
                    .or().apply("CONCAT(first_name, ' ', last_name) = {0}", queryDTO.getNickname().trim())
                    .or().apply("CONCAT(first_name, last_name) = {0}", queryDTO.getNickname().trim()));
        }
        Boolean isRepeatUser = queryDTO.getIsRepeatUser();
        if (!isRepeatUser){
            wrapper.select("user_id", "MAX(last_time) as last_time")
                    .groupBy("user_id");
        }
        wrapper.orderByDesc("last_time");
        Page<GroupInnerUser> resultPage = groupInnerUserMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),wrapper);
        for (GroupInnerUser groupInnerUser : resultPage.getRecords()) {
            if (!isRepeatUser){
                groupInnerUser=groupInnerUserMapper.selectOne(new QueryWrapper<GroupInnerUser>()
                        .eq("user_id", groupInnerUser.getUserId()).eq("last_time", groupInnerUser.getLastTime()));
            }
            ReturnUserDTO returnUserDTO = new ReturnUserDTO();
            returnUserDTO.setUserId(groupInnerUser.getUserId());
            returnUserDTO.setGroupId(groupInnerUser.getGroupId());
            returnUserDTO.setUsername(groupInnerUser.getUsername());
            returnUserDTO.setNickname(groupInnerUser.getFirstLastName());
            returnUserDTO.setFromGroup(groupInnerUser.getType());
            returnUserDTO.setStatus(groupInnerUser.isStatus());
            returnUserDTO.setChatTime(groupInnerUser.getLastTime());
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", groupInnerUser.getUserId()));
            if (user!=null && user.getValidTime()!=null){
                returnUserDTO.setStatus(user.isValidFree());
                returnUserDTO.setExpireTime(user.getValidTime());
            }else {
                returnUserDTO.setStatus(false);
                returnUserDTO.setExpireTime(groupInnerUser.getCreateTime());
            }
            dtoList.add(returnUserDTO);
        }
        data.put("total", resultPage.getTotal());
        data.put("data", dtoList);
        return new JsonResult(data);
    }

    @Override
    public JsonResult findGroupList(QueryGroupDTO queryDTO) {
        List<ReturnGroupDTO> returnDTOList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        QueryWrapper<Status> wrapper = new QueryWrapper<>();
        if (queryDTO.getGroupId()!=null && StringUtils.isNotBlank(queryDTO.getGroupId())){
            wrapper.eq("group_id", queryDTO.getGroupId());
        }
        if (queryDTO.getGroupName()!=null&& StringUtils.isNotBlank(queryDTO.getGroupName())){
            wrapper.like("group_title", queryDTO.getGroupName().trim());
        }
        if (queryDTO.getInviterName()!=null && StringUtils.isNotBlank(queryDTO.getInviterName())){
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.or(w -> w.like("first_name", queryDTO.getInviterName().trim())
                    .or().like("last_name", queryDTO.getInviterName().trim())
                    .or().apply("CONCAT(first_name, ' ', last_name) = {0}", queryDTO.getInviterName().trim())
                    .or().apply("CONCAT(first_name, last_name) = {0}", queryDTO.getInviterName().trim()));
            List<User> users = userMapper.selectList(userQueryWrapper);
            List<String> userIds = users.stream().map(User::getUserId).collect(Collectors.toList());
            List<UserNormal> userNormals = userNormalMapper.selectList(new QueryWrapper<UserNormal>().in("user_id", userIds));
            List<String> groupIds = userNormals.stream().map(UserNormal::getGroupId).collect(Collectors.toList());
            wrapper.in("group_id", groupIds);
        }
        if (queryDTO.getStartTime()!=null && StringUtils.isNotBlank(queryDTO.getStartTime())){
            LocalDate startDate = LocalDate.parse(queryDTO.getStartTime(), DateTimeFormatter.ISO_DATE);
            LocalDate endDate = LocalDate.parse(queryDTO.getEndTime(), DateTimeFormatter.ISO_DATE);
            // 补全时间为当天的 00:00:00 和 23:59:59
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            // 转换为 Date 类型用于数据库查询
            Date startDateValue = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Date endDateValue = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
            wrapper.between("create_time", startDateValue, endDateValue);
        }
        wrapper.orderByDesc("create_time");
        Page<Status> resultPage = statusMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),wrapper);
        for (Status status : resultPage.getRecords()){
            ReturnGroupDTO dto = new ReturnGroupDTO();
            String groupId = status.getGroupId();
            UserNormal userNormal = userNormalMapper.selectOne(new QueryWrapper<UserNormal>().eq("group_id", groupId));
            if (userNormal==null){
                statusMapper.delete(new QueryWrapper<Status>().eq("group_id", groupId));
                continue;
            }
            dto.setGroupId(groupId);
            dto.setGroupName(status.getGroupTitle());
            dto.setInviterId(userNormal.getUserId());
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userNormal.getUserId()));
            if (user!=null){
                dto.setInviterName(user.getFirstLastName());
            }else{
                statusMapper.delete(new QueryWrapper<Status>().eq("group_id", groupId));
                userNormalMapper.delete(new QueryWrapper<UserNormal>().eq("group_id", groupId));
                continue;
            }
            List<Rate> rates = rateService.selectRateList(groupId);
            if (rates==null || rates.isEmpty()){
                dto.setExchangeRate(BigDecimal.ZERO);
                dto.setFeeRate(BigDecimal.ZERO);
            }else {
                dto.setExchangeRate(rates.get(0).getExchange());
                dto.setFeeRate(rates.get(0).getRate());
            }
            dto.setDailyCutTime(status.getSetTime().getHours()+"");
            List<UserOperation> userOperations = userOperationMapper.selectList(new QueryWrapper<UserOperation>()
                    .eq("group_id", groupId).eq("is_operation", true));
            String result = userOperations.stream().filter(userOp -> userOp.getUserId()==null || !isNotInviter(userOp, userNormal)).filter(Objects::nonNull)
                    .map(userOp -> "@" + userOp.getUsername()).collect(Collectors.joining(" ")); // 使用空格分隔
            dto.setOperator(result);
            dto.setBillCount(status.getShowFew());
            dto.setJoinTime(status.getCreateTime());
            dto.setIsAccountingEnabled(true);
            dto.setIsPinned(true);
            returnDTOList.add(dto);
        }
        returnDTOList.sort(Comparator.comparing(ReturnGroupDTO::getInviterName, Comparator.nullsFirst(String::compareTo)));
        data.put("total", resultPage.getTotal());
        data.put("data", returnDTOList);
        return new JsonResult(data);
    }
    @Override
    public JsonResult findGroupListTag(QueryGroupTagDTO queryDTO) {
        List<ReturnGroupDTO> returnDTOList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        QueryWrapper<GroupTag> groupTagWrapper = new QueryWrapper<>();
        if (queryDTO.getTagName()!=null){
            groupTagWrapper.like("tag_name", queryDTO.getTagName().trim());
        }
        if (queryDTO.getGroupId()!=null && StringUtils.isNotBlank(queryDTO.getGroupId())){
            groupTagWrapper.eq("group_id", queryDTO.getGroupId());
        }
        groupTagWrapper.orderByDesc("tag_name");
        List<GroupTag> groupTags = groupTagMapper.selectList(groupTagWrapper);
        QueryWrapper<Status> wrapper = new QueryWrapper<>();
        if (groupTags.isEmpty()){
            data.put("total", 0);
            data.put("data", returnDTOList);
            return new JsonResult(data);
        }
        if (queryDTO.getGroupName()!=null&& StringUtils.isNotBlank(queryDTO.getGroupName())){
            wrapper.like("group_title", queryDTO.getGroupName().trim());
        }
        if (queryDTO.getStartTime()!=null && StringUtils.isNotBlank(queryDTO.getStartTime())){
            LocalDate startDate = LocalDate.parse(queryDTO.getStartTime(), DateTimeFormatter.ISO_DATE);
            LocalDate endDate = LocalDate.parse(queryDTO.getEndTime(), DateTimeFormatter.ISO_DATE);
            // 补全时间为当天的 00:00:00 和 23:59:59
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            // 转换为 Date 类型用于数据库查询
            Date startDateValue = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Date endDateValue = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
            wrapper.between("create_time", startDateValue, endDateValue);
        }
        wrapper.in("group_id", groupTags.stream().map(GroupTag::getGroupId).collect(Collectors.toList()));
        wrapper.orderByDesc("create_time");
        Page<Status> resultPage = statusMapper.selectPage(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()),wrapper);
        for (Status status : resultPage.getRecords()){
            ReturnGroupDTO dto = new ReturnGroupDTO();
            String groupId = status.getGroupId();
            UserNormal userNormal = userNormalMapper.selectOne(new QueryWrapper<UserNormal>().eq("group_id", groupId));
            if (userNormal==null){
                statusMapper.delete(new QueryWrapper<Status>().eq("group_id", groupId));
                continue;
            }
            dto.setGroupId(groupId);
            dto.setGroupName(status.getGroupTitle());
            dto.setInviterId(userNormal.getUserId());
            dto.setInviterName(userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userNormal.getUserId())).getFirstLastName());
            Rate rate = rateMapper.selectOne(new QueryWrapper<Rate>().eq("group_id", groupId).last("LIMIT 1"));
            if (rate==null){
                dto.setExchangeRate(BigDecimal.ZERO);
                dto.setFeeRate(BigDecimal.ZERO);
            }else {
                dto.setExchangeRate(rate.getExchange());
                dto.setFeeRate(rate.getRate());
            }
            dto.setDailyCutTime(status.getSetTime().getHours()+"");
            List<UserOperation> userOperations = userOperationMapper.selectList(new QueryWrapper<UserOperation>()
                    .eq("group_id", groupId).eq("is_operation", true));
            String result = userOperations.stream().filter(userOp -> userOp.getUserId()==null || !isNotInviter(userOp, userNormal)).filter(Objects::nonNull)
                    .map(userOp -> "@" + userOp.getUsername()).collect(Collectors.joining(" ")); // 使用空格分隔
            dto.setOperator(result);
            dto.setBillCount(status.getShowFew());
            dto.setJoinTime(status.getCreateTime());
            dto.setIsAccountingEnabled(true);
            dto.setIsPinned(true);
            List<GroupTag> groupTags1 = groupTagMapper.selectList(new QueryWrapper<GroupTag>().eq("group_id", groupId));
            dto.setTags(groupTags1.stream().map(GroupTag::getTagName).collect(Collectors.toList()));
            returnDTOList.add(dto);
        }
        returnDTOList.sort(Comparator.comparing((ReturnGroupDTO dto) -> dto.getTags().isEmpty() ? "" : dto.getTags().get(0))
                        .thenComparing(ReturnGroupDTO::getJoinTime, Comparator.nullsLast(Comparator.reverseOrder())));
        data.put("total", resultPage.getTotal());
        data.put("data", returnDTOList);
        return new JsonResult(data);
    }

    @Override
    public JsonResult sendAllMessage(ManagerGroupMessageDTO dto) {
        for (String groupId:dto.getGroupIds()) this.send(groupId, dto.getFileList(), dto.getMessage());
        return new JsonResult();
    }

    @Override
    public JsonResult sendGroupMessage(GroupMessageDTO dto) {
        if (dto.getGroupId()==null || StringUtils.isBlank(dto.getGroupId())){
            List<Status> groupInfos = statusMapper.selectList(new QueryWrapper<>());
            for (Status groupInfo : groupInfos) {
                this.send(groupInfo.getGroupId(), dto.getFileList(), dto.getContent());//sendAllMessage中 这个content和message一样这里不小心起了个别名
            }
        }else if (dto.getGroupId()!=null){
            this.send(dto.getGroupId(), dto.getFileList(), dto.getContent());
        }
        return new JsonResult();
    }

    @Override
    public JsonResult setTagGroup(String groupId, String tag) {
        GroupTag groupTag = groupTagMapper.selectOne(new QueryWrapper<GroupTag>().eq("group_id", groupId).eq("tag_name", tag));
        if (groupTag== null){
            groupTag =new GroupTag();
            groupTag.setGroupId(groupId);
            groupTag.setTagName(tag);
            groupTagMapper.insert(groupTag);
        }else if (groupTag!=null){
            return new JsonResult("此群已有当前标签!");
        }
        return new JsonResult();
    }

    @Override
    public JsonResult getTagAll(Integer pageNum, Integer pageSize,String groupId) {
        try {
            // 构建查询条件：仅查询 tagName 并去重
            QueryWrapper<GroupTag> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("DISTINCT tag_name");
            if (groupId!=null && StringUtils.isNotBlank(groupId)){
                queryWrapper.eq("group_id", groupId);
            }
            // 执行分页查询
            Page<GroupTag> resultPage = groupTagMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
            // 提取不重复的标签名称
            List<String> uniqueTags = resultPage.getRecords().stream()
                    .map(GroupTag::getTagName).collect(Collectors.toList());
            // 构造返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("total", resultPage.getTotal());
            data.put("data", uniqueTags);
            return new JsonResult(data);
        } catch (Exception e) {
            log.error("Failed to fetch unique tags with pagination", e);
            return new JsonResult("获取标签失败");
        }
    }

    @Override
    public JsonResult deleteTagGroup(String groupId, String tag) {
        groupTagMapper.delete(new QueryWrapper<GroupTag>().eq("group_id", groupId).eq("tag_name", tag));
        return new JsonResult();
    }

    @Override
    public JsonResult updateExpireTime(String userId, String expireTime) {
        userMapper.update(null,new UpdateWrapper<User>().set("valid_time", expireTime).eq("user_id", userId));
        return new JsonResult();
    }

    @Override
    public JsonResult sendUserMessage(UserMessageDTO dto) {
        for (String userId : dto.getUserIds())  this.send(userId, dto.getFileList(), dto.getContent());
        return new JsonResult();
    }

    @Override
    public JsonResult sendGroupMessageTag(GroupMessageDTO dto) {
        if (dto.getGroupId()==null || StringUtils.isBlank(dto.getGroupId())){
            List<GroupTag> groupTags = groupTagMapper.selectList(new QueryWrapper<>());
            for (GroupTag groupInfo : groupTags) {
                this.send(groupInfo.getGroupId(), dto.getFileList(), dto.getContent());
            }
        }else if (dto.getGroupId()!=null){
            this.send(dto.getGroupId(), dto.getFileList(), dto.getContent());
        }
        return new JsonResult();
    }

    @Override
    public JsonResult saveCustomerConfig(ConfigDTO dto) {
        ConfigEdit configEdit = configEditMapper.selectOne(new QueryWrapper<>());
        if (configEdit==null){
            configEdit = new ConfigEdit();
            configEdit.setPayText(dto.getPayText());
            configEdit.setAdminUserName(dto.getAdminUserName());
            configEdit.setPayImage(dto.getPayImage());
            configEdit.setShowRenewal(dto.getShowRenewal());
            configEditMapper.insert(configEdit);
            if (dto.getButtonRows()!=null&& !dto.getButtonRows().isEmpty()){
                for (List<AdTimeButtonDTO> row : dto.getButtonRows()) {
                    for (AdTimeButtonDTO buttonDTO : row) {
                        // 新增按钮
                        ConfigEditButton newButton = new ConfigEditButton();
                        newButton.setConfigEditId(String.valueOf(configEdit.getId()));
                        newButton.setRowIndex(buttonDTO.getRowIndex());
                        newButton.setButtonIndex(buttonDTO.getButtonIndex());
                        newButton.setText(buttonDTO.getText());
                        newButton.setMonth(buttonDTO.getAmount());
                        newButton.setLink(buttonDTO.getLink());
                        // 设置其他字段
                        configEditButtonMapper.insert(newButton);
                    }
                }
            }
        }else {
            configEdit.setPayText(dto.getPayText());
            configEdit.setAdminUserName(dto.getAdminUserName());
            configEdit.setPayImage(dto.getPayImage());
            configEdit.setShowRenewal(dto.getShowRenewal());
            configEditMapper.updateById(configEdit);
            List<List<AdTimeButtonDTO>> buttonRows = dto.getButtonRows();
            configEditButtonMapper.delete(new QueryWrapper<ConfigEditButton>().eq("config_edit_id", configEdit.getId()));
            if (buttonRows != null && !buttonRows.isEmpty()){
                for (List<AdTimeButtonDTO> row : buttonRows) {
                    for (AdTimeButtonDTO buttonDTO : row) {
                        // 新增按钮
                        ConfigEditButton newButton = new ConfigEditButton();
                        newButton.setConfigEditId(String.valueOf(configEdit.getId()));
                        newButton.setRowIndex(buttonDTO.getRowIndex());
                        newButton.setButtonIndex(buttonDTO.getButtonIndex());
                        newButton.setText(buttonDTO.getText());
                        newButton.setMonth(buttonDTO.getAmount());
                        newButton.setLink(buttonDTO.getLink());
                        // 设置其他字段
                        configEditButtonMapper.insert(newButton);
                    }
                }
            }
        }
        return new JsonResult();
    }

    @Override
    public JsonResult findConfig() {
        ConfigDTO configDTO=new ConfigDTO();
        ConfigEdit configEdit = configEditMapper.selectOne(null);
        if (configEdit!=null){
            List<ConfigEditButton> chatNumButtons = configEditButtonMapper.selectList(new QueryWrapper<ConfigEditButton>().eq("config_edit_id",configEdit.getId()));
            configDTO.setPayImage(configEdit.getPayImage());
            configDTO.setAdminUserName(configEdit.getAdminUserName());
            configDTO.setPayText(configEdit.getPayText());
            configDTO.setShowRenewal(configEdit.getShowRenewal());
            if (chatNumButtons != null) {
                // 使用 Map 按 rowIndex 分组
                Map<Integer, List<ConfigEditButton>> rowMap = new HashMap<>();
                for (ConfigEditButton button : chatNumButtons) {
                    rowMap.computeIfAbsent(button.getRowIndex(), k -> new ArrayList<>()).add(button);
                }
                // 创建二维列表
                List<List<AdTimeButtonDTO>> buttonRows = new ArrayList<>();
                for (int rowIndex = 0; rowIndex < rowMap.size(); rowIndex++) {
                    List<ConfigEditButton> rowButtons = rowMap.get(rowIndex);
                    if (rowButtons != null) {
                        // 按 buttonIndex 排序
                        rowButtons.sort(Comparator.comparingInt(ConfigEditButton::getButtonIndex));
                        List<AdTimeButtonDTO> buttonRowDTOs = new ArrayList<>();
                        for (ConfigEditButton button : rowButtons) {
                            AdTimeButtonDTO buttonDTO = new AdTimeButtonDTO();
                            buttonDTO.setText(button.getText());
                            buttonDTO.setLink(button.getLink());
                            buttonDTO.setAmount(button.getMonth());//这写错名字了 就用这个了
                            buttonDTO.setButtonIndex(button.getButtonIndex());
                            buttonDTO.setRowIndex(button.getRowIndex());
                            // 设置其他字段
                            buttonRowDTOs.add(buttonDTO);
                        }
                        buttonRows.add(buttonRowDTOs);
                    }
                }
                configDTO.setButtonRows(buttonRows);
            }
        }
        return new JsonResult(configDTO);
    }

    @Override
    public JsonResult accountRegister(LoginFromDTO dto) {
        LoginFrom loginFrom = loginFromMapper.selectOne(new QueryWrapper<LoginFrom>().eq("username",dto.getUsername()));
        if (loginFrom!=null){
            return new JsonResult("此用户名已被注册!");
        }
        if (!dto.getToken().equals("liu332331")){
            return new JsonResult("令牌错误!");
        }
        loginFrom=new LoginFrom();
        loginFrom.setUsername(dto.getUsername());
        loginFrom.setPassword(dto.getPassword());
        loginFromMapper.insert(loginFrom);
        return new JsonResult();
    }

    @Override
    public JsonResult getAccountSetting() {
        AccountSetting accountSetting = accountSettingMapper.selectOne(new QueryWrapper<>());
        return new JsonResult(accountSetting);
    }

    @Override
    public JsonResult saveAccountSetting(AccountSettingDTO dto) {
        AccountSetting accountSetting = accountSettingMapper.selectOne(new QueryWrapper<>());
        if (accountSetting==null){
            accountSetting=new AccountSetting();
            accountSetting.setExpireNotice(dto.getExpireNotice());
            accountSetting.setAdminExpireNotice(dto.getAdminExpireNotice());
            accountSetting.setEnglishExpireNotice(dto.getEnglishExpireNotice());
            accountSetting.setEnglishAdminExpireNotice(dto.getEnglishAdminExpireNotice());
            accountSetting.setNoneNotice(dto.getNoneNotice());
            accountSetting.setAdminNotice(dto.getAdminNotice());
            accountSetting.setEnglishNotGroupAdminNotice(dto.getEnglishNotGroupAdminNotice());
            accountSetting.setNotGroupAdminNotice(dto.getNotGroupAdminNotice());
            accountSetting.setNotGroupAdminNoticeHtml(dto.getNotGroupAdminNoticeHtml());
            accountSetting.setGroupLanguage(dto.getGroupLanguage().equals("zh"));
            accountSetting.setStartMessageNotice(dto.getStartMessageNotice());
            accountSetting.setStartMessageNoticeSwitch(dto.getStartMessageNoticeSwitch());
            accountSetting.setEnglishStartMessageNotice(dto.getEnglishStartMessageNotice());
            accountSetting.setPrivateMessageLanguage(dto.getPrivateMessageLanguage().equals("zh"));
            accountSetting.setTrialDurationHours(dto.getTrialDurationHours());
            accountSettingMapper.insert(accountSetting);
        }else{
            accountSetting.setExpireNotice(dto.getExpireNotice());
            accountSetting.setAdminExpireNotice(dto.getAdminExpireNotice());
            accountSetting.setNoneNotice(dto.getNoneNotice());
            accountSetting.setAdminNotice(dto.getAdminNotice());
            accountSetting.setEnglishExpireNotice(dto.getEnglishExpireNotice());
            accountSetting.setEnglishAdminExpireNotice(dto.getEnglishAdminExpireNotice());
            accountSetting.setGroupLanguage(dto.getGroupLanguage().equals("zh"));
            accountSetting.setEnglishNotGroupAdminNotice(dto.getEnglishNotGroupAdminNotice());
            accountSetting.setNotGroupAdminNotice(dto.getNotGroupAdminNotice());
            accountSetting.setNotGroupAdminNoticeHtml(dto.getNotGroupAdminNoticeHtml());
            accountSetting.setStartMessageNotice(dto.getStartMessageNotice());
            accountSetting.setStartMessageNoticeSwitch(dto.getStartMessageNoticeSwitch());
            accountSetting.setEnglishStartMessageNotice(dto.getEnglishStartMessageNotice());
            accountSetting.setPrivateMessageLanguage(dto.getPrivateMessageLanguage().equals("zh"));
            accountSetting.setTrialDurationHours(dto.getTrialDurationHours());
            accountSettingMapper.updateById(accountSetting);
        }
        return new JsonResult();
    }

    @Override
    public JsonResult findAccountUser(Integer page, Integer size, String keyword) {
        List<AccountUserAdminDTO> dtoList = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword!=null && StringUtils.isNotBlank(keyword)){
            wrapper.or(w -> w.like("first_name", keyword.trim())
                    .or().like("last_name", keyword.trim())
                    .or().like("user_id", keyword.trim())
                    .or().apply("CONCAT(first_name, ' ', last_name) = {0}", keyword.trim())
                    .or().apply("CONCAT(first_name, last_name) = {0}", keyword.trim()));
        }
        wrapper.orderByDesc("cjgl").orderByDesc("create_time"); // 先按 cjgl 字段降序（true 在前）
        Page<User> resultPage = userMapper.selectPage(new Page<>(page, size),wrapper);
        for (User groupInnerUser : resultPage.getRecords()) {
            AccountUserAdminDTO returnUserDTO = new AccountUserAdminDTO();
            returnUserDTO.setUserId(groupInnerUser.getUserId());
            returnUserDTO.setCreateTime(groupInnerUser.getCreateTime());
            returnUserDTO.setCjgl(groupInnerUser.isCjgl());
            returnUserDTO.setUsername(groupInnerUser.getUsername());
            returnUserDTO.setNickname(groupInnerUser.getFirstLastName());
            dtoList.add(returnUserDTO);
        }
        data.put("total", resultPage.getTotal());
        data.put("data", dtoList);
        return new JsonResult(data);
    }

    @Override
    public JsonResult quxiaoAccountSuperAdminUser(String userId) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id",userId));
        user.setCjgl(false);
        userMapper.updateById(user);
        return new JsonResult();
    }


    @Override
    public JsonResult findAccountUserOrder(Integer page, Integer size, String keyword, String startTime, String endTime, String selectedType) {
        QueryWrapper<UserOrder> userOrderQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank( keyword)){
            userOrderQueryWrapper.like("user_id",keyword);
        }
        if (startTime!=null && StringUtils.isNotBlank(startTime)){
            userOrderQueryWrapper.between("create_time", startTime, endTime);
        }
        if (StringUtils.isNotBlank(selectedType)){
            userOrderQueryWrapper.eq("type",selectedType);
        }
        Page<UserOrder> userOrderPage = userOrderMapper.selectPage(new Page<>(page, size), userOrderQueryWrapper);
        List<UserOrderDTO> dtoList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (UserOrder userOrder : userOrderPage.getRecords()) {
            UserOrderDTO userOrderDTO = new UserOrderDTO();
            userOrderDTO.setUserId(userOrder.getUserId());
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userOrder.getUserId()));
            if (user != null){
                userOrderDTO.setUsername(user.getUsername());
                userOrderDTO.setNickname(user.getFirstLastName());
            }
            userOrderDTO.setAmount(userOrder.getAmount());
            userOrderDTO.setType(userOrder.getType());
            userOrderDTO.setConfigEditButtonName(userOrder.getConfigEditButtonName());
            userOrderDTO.setMonth(userOrder.getMonth()+"天");
            userOrderDTO.setOrderNumber(userOrder.getOrderNumber());
            userOrderDTO.setCreateTime(userOrder.getCreateTime());
            userOrderDTO.setEndTime(userOrder.getEndTime());
            dtoList.add(userOrderDTO);
        }
        map.put("total", userOrderPage.getTotal());
        map.put("data", dtoList);
        return new JsonResult(map);
    }

    @Override
    public JsonResult accountChangePassword(String username,String newPassword, String secretKey) {
        if (!secretKey.equals("liu332331")){
            return new JsonResult("密钥错误!");
        }
        LoginFrom loginFrom = loginFromMapper.selectOne(new QueryWrapper<LoginFrom>().eq("username", username));
        if (loginFrom != null){
            loginFrom.setPassword(newPassword);
            loginFromMapper.updateById(loginFrom);
            return new JsonResult();
        }else {
            return new JsonResult("用户名或原密码错误!");
        }
    }

    @Override
    public JsonResult setAccountSuperAdminUser(String userId) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id",userId));
        user.setCjgl(true);
        userMapper.updateById(user);
        return new JsonResult();
    }

    private static boolean isNotInviter(UserOperation userOp, UserNormal userNormal) {
        return  userOp.getUserId().equals(userNormal.getUserId());
    }

    @Override
    public JsonResult setStatus() {
        // 获取所有群组
        List<UserNormal> statuses = userNormalMapper.selectList(new QueryWrapper<UserNormal>());
        for (UserNormal status : statuses) {
            String groupId = status.getGroupId();
            try {
                // 调用 Telegram API 检查 Bot 是否在群组中
                ChatMember chatMember = accountBot.findStatus(groupId);
                Thread.sleep(500);
                if (chatMember ==null || chatMember.getStatus().equals("left")|| chatMember.getStatus().equals("kicked")){
                    statusMapper.delete(new UpdateWrapper<Status>().eq("group_id", groupId));
                    userNormalMapper.delete(new QueryWrapper<UserNormal>().eq("group_id", groupId));
                    userOperationMapper.delete(new QueryWrapper<UserOperation>().eq("group_id", groupId));
                }
            } catch (Exception e) {
                log.error("Failed to check bot presence in group: {}", groupId, e);
            }
        }
        return new JsonResult("状态已更新");
    }

    @Override
    public JsonResult leaveGroup(String groupId) {
        accountBot.leaveChat(groupId);
        statusMapper.delete(new QueryWrapper<Status>().eq("group_id", groupId));
        groupInfoSettingMapper.delete(new QueryWrapper<GroupInfoSetting>().eq("group_id", groupId));
        return new JsonResult();
    }

    @Override
    public JsonResult allLeaveGroup(List<String> groupIds) {
        for (String groupId : groupIds){
            try {
                accountBot.leaveChat(groupId);
            }catch (Exception e){
                log.error("退群失败：{}", e.getMessage());
            }
            statusMapper.delete(new QueryWrapper<Status>().eq("group_id", groupId));
            groupInfoSettingMapper.delete(new QueryWrapper<GroupInfoSetting>().eq("group_id", groupId));
        }//批量退群 处理
        return new JsonResult();
    }

    public void send(String chatId, List<FileItemDTO> fileItemDTOS,String  text){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String string = text;
        sendMessage.setText(string);
        sendMessage.disableWebPagePreview();
        sendMessage.setParseMode("HTML");
        List<FileItemGuangbo> fileItems;
        if (fileItemDTOS != null && !fileItemDTOS.isEmpty()){
            fileItems = fileItemDTOS.stream().filter(Objects::nonNull).map(input -> {
                FileItemGuangbo file = new FileItemGuangbo();
                file.setName(input.getName());
                file.setUrl(input.getUrl());
                file.setSize(input.getSize());
                file.setType(input.getType());
                return file;
            }).collect(Collectors.toList());
            mediaInfoConfig.sendCombinedMessageGeneric(chatId, sendMessage, fileItems);
        } else {
            try {
                accountBot.oneBroadcast(sendMessage);
            }catch (Exception e){
                System.err.println(e.getMessage());
                statusMapper.delete(new QueryWrapper<Status>().eq("group_id", chatId));
            }
        }
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
        queryWrapper.orderByAsc("add_time");
        return accountMapper.selectList(queryWrapper);
    }


    public void insertAccount(Account account) {
        accountMapper.insert(account);
    }
    public void deleteHistoryData(String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
//        updateWrapper.eq("riqie",0); 过期 或没过期的都删除
        accountMapper.delete(updateWrapper);
    }

    public void deleteTodayData(Status status, String groupId) {
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        wrapper.ge("add_time", status.getSetStartTime())
                .le("add_time", status.getSetTime());
        wrapper.eq("riqie", status.isRiqie());
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
