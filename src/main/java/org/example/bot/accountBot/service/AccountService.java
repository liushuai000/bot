package org.example.bot.accountBot.service;

import org.example.bot.accountBot.dto.QueryType;
import org.example.bot.accountBot.dto.ReturnFromType;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Status;

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

    List<Account> selectAccounts(String groupId);

    void deleteById(int id);

    void deleteHistoryData(String groupId);

    void updateRiqie(int id,boolean riqie);

    void updateLastUpdateRiqie(int id,boolean riqie, Date updateTime);

    void updateSetTime(String id, Date setTime);
}
