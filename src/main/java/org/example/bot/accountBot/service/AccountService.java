package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.pojo.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Service
@Slf4j
public class AccountService {

    @Autowired
    AccountMapper mapper;


    public List<Account> selectAccountDataStatus0(String groupId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("data_status",0);
        queryWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        queryWrapper.orderByDesc("add_time");
        return mapper.selectList(queryWrapper);
    }

    public void insertAccount(Account updateAccount) {
        mapper.insert(updateAccount);
    }

    public void updateDataStatus(String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        updateWrapper.set("data_status",1);
        mapper.update(null,updateWrapper);
    }

    public void updateSetTime(Date setTime,String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("set_time",setTime);
        updateWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        mapper.update(null,updateWrapper);
    }
    public void deleteHistoryData(String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);//如果groupId为空是否查的到呢
        updateWrapper.eq("data_status",0);
        mapper.delete(updateWrapper);
    }
    public void deleteTodayData(String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id",groupId);
        updateWrapper.eq("data_status",0);
        mapper.delete(updateWrapper);
    }


    public void deleteInData(Date addTime,String groupId) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("add_time",addTime);
        updateWrapper.eq("group_id",groupId);
        mapper.delete(updateWrapper);
    }

    public void updateDown(BigDecimal add,String groupId) {
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
