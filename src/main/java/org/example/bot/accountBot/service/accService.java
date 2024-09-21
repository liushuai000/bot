package org.example.bot.accountBot.service;

import org.example.bot.accountBot.mapper.mapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
@Component
public class accService {

    @Autowired
    mapper mapper;
    public List selectAll() {
        System.out.println("selectAll");
       return mapper.selectAll();
    }

    public List<Account> selectAccount() {
        return mapper.selectAccount();
    }

    public void insertUser(User user) {
        mapper.insertUser(user);
    }

    public void insertAccount(Account updateAccount) {
        mapper.insertAccount(updateAccount);
    }

    public void updateRate(String updateAccount) {
        mapper.updateRate(updateAccount);
    }

    public void updateExchange(BigDecimal updateAccount) {
        mapper.updateExchange(updateAccount);
    }

    public Rate selectRate() {
        return mapper.selectRate();
    }

    public void insertRate(Rate rates) {
        mapper.insertRate(rates);
    }

    public void deleteHandele(String deleteName) {
        mapper.deleteHandle(deleteName);
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


    public void updateOverDue(Long overdue) {
        mapper.updateOverDue(overdue);
    }

    public void deleteInData(Date addTime) {
        mapper.deleteInData(addTime);
    }

    public void deleteMatchTime(Date matchTime) {
        mapper.deleteMatchTime(matchTime);
    }

    public List<Issue> selectIssue() {
        return mapper.selectIssue();
    }

    public void updateIssueDataStatus() {
        mapper.updateIssueDataStatus();
    }

    public void updateIssueSetTime(Date setTime) {
        mapper.updateIssueSetTime(setTime);
    }

    public void insertIssue(Issue issue) {
        mapper.insertIssue(issue);
    }

    public void updateDown(BigDecimal add) {
        mapper.updateDown(add);
    }

    public void uodateIssueDown(BigDecimal add) {
        mapper.uodateIssueDown(add);
    }

    public void deleteTedayIusseData() {
        mapper.deleteTedayIusseData();
    }

    public void updateissueDown(BigDecimal down) {
        mapper.updateissueDown(down);
    }

    public void deleteNewestIssue(Date addTime) {
        mapper.deleteNewestIssue(addTime);
    }

    public void updateNewestData(BigDecimal down) {
        mapper.updateNewestData(down);
    }

    public List<String> inform(Date date) {
        return mapper.inform(date);
    }

    public void updateHandleStatus(int i) {
        mapper.updateHandleStatus(i);
    }

    public void updateCallBackStatus(int i) {
        mapper.updateCallBackStatus(i);
    }

    public void updateDatilStatus(int i) {
        mapper.updateDatilStatus(i);
    }
}
