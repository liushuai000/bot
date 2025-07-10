package org.example.bot.accountBot.service;

import org.example.bot.accountBot.dto.*;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.utils.JsonResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface AccountService {


    ReturnFromType findAccountByGroupId(QueryType queryType);

    void deleteInData(String id, String groupId);

    void updateNewestData(BigDecimal down, String groupId);

    void insertAccount(Account updateAccount);

    void updateDown(BigDecimal subtract, String groupId);

    void deleteTodayData(Status status, String groupId);

    List<Account> selectAccountRiqie(boolean riqie,Date setTime, String groupId);

    void deleteHistoryData(String groupId);

    JsonResult login(LoginFromDTO loginFromDTO);

    JsonResult findGroupList(QueryGroupDTO queryDTO);

    JsonResult getUserList(QueryUserDTO queryDTO);

    JsonResult setStatus();

    JsonResult leaveGroup(String groupId);

    JsonResult allLeaveGroup(List<String> groupIds);

    JsonResult findGroupListTag(QueryGroupTagDTO dto);

    JsonResult sendAllMessage(ManagerGroupMessageDTO dto);

    JsonResult sendGroupMessage(GroupMessageDTO dto);

    JsonResult setTagGroup(String groupId, String tag);

    JsonResult getTagAll(Integer page, Integer size,String groupId);

    JsonResult deleteTagGroup(String groupId, String tag);

    JsonResult updateExpireTime(String userId, String expireTime);

    JsonResult sendUserMessage(UserMessageDTO dto);

    JsonResult sendGroupMessageTag(GroupMessageDTO dto);

    JsonResult saveCustomerConfig(ConfigDTO dto);

    JsonResult findConfig();

    JsonResult accountRegister(LoginFromDTO loginFromDTO);

    JsonResult getAccountSetting();

    JsonResult saveAccountSetting(AccountSettingDTO dto);

    JsonResult setAccountSuperAdminUser(String userId);

    JsonResult findAccountUser(Integer page, Integer size, String keyword);

    JsonResult quxiaoAccountSuperAdminUser(String userId);

    JsonResult findAccountUserOrder(Integer page, Integer size, String keyword, String startTime, String endTime, String selectedType);

    JsonResult accountChangePassword(String username, String newPassword, String secretKey);
}
