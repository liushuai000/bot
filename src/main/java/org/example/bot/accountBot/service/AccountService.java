package org.example.bot.accountBot.service;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.pojo.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
@Component
@Service
@Slf4j
public class AccountService {

    @Autowired
    AccountMapper mapper;


    public List<Account> selectAccount() {
        return mapper.selectAccount();
    }

    public void insertAccount(Account updateAccount) {
        mapper.insertAccount(updateAccount);
    }

    public void updateDataStatus() {
        mapper.updateDataStatus();
    }

    public void updateSetTime(Date setTime) {
        mapper.updateSetTime(setTime);
    }

    public void deleteTedayData() {
        mapper.deleteTedayData();
    }


    public void deleteInData(Date addTime) {
        mapper.deleteInData(addTime);
    }

    public void deleteMatchTime(Date matchTime) {
        mapper.deleteMatchTime(matchTime);
    }

    public void updateDown(BigDecimal add) {
        mapper.updateDown(add);
    }

    public void updateNewestData(BigDecimal down) {
        mapper.updateNewestData(down);
    }

    public List<String> inform(Date date) {
        return mapper.inform(date);
    }

}
