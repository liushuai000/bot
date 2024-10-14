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


    public List<Account> selectAccountDataStatus0() {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("data_status",0);
        return mapper.selectList(queryWrapper);
    }

    public void insertAccount(Account updateAccount) {
        mapper.insert(updateAccount);
    }

    public void updateDataStatus() {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("data_status",1);
        mapper.update(null,updateWrapper);
    }

    public void updateSetTime(Date setTime) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("set_time",setTime);
        mapper.update(null,updateWrapper);
    }
    public void deleteHistoryData() {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("data_status",0);
        mapper.delete(updateWrapper);
    }
    public void deleteTodayData() {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("data_status",0);
        mapper.delete(updateWrapper);
    }


    public void deleteInData(Date addTime) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("add_time",addTime);
        mapper.delete(updateWrapper);
    }

    public void updateDown(BigDecimal add) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("down",add);
        mapper.update(null,updateWrapper);
    }

    public void updateNewestData(BigDecimal down) {
        UpdateWrapper<Account> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("down",down);
        mapper.update(null,updateWrapper);
    }

    public List<String> inform(Date date) {
        // 计算 48 小时之前的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -48);
        Date dateThreshold = calendar.getTime();
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("handle").gt("add_time",dateThreshold);
        return mapper.selectObjs(queryWrapper).stream().map(Object::toString).collect(Collectors.toList());
    }

}
