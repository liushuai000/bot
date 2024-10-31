package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.AccountDTO;
import org.example.bot.accountBot.dto.QueryType;
import org.example.bot.accountBot.dto.ReturnFromType;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.mapper.RateMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


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
}
