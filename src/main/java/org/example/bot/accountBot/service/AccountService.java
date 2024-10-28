package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
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

@Component
@Service
@Slf4j
public class AccountService {

    @Autowired
    AccountMapper mapper;

    public List<Account> findAccountByGroupId(String groupId) {
        return mapper.selectList(new QueryWrapper<Account>().eq("group_id", groupId));
    }














    //查询没有开启日切的
    public List<Account> selectAccountRiqie(boolean riqie,String groupId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("riqie",riqie);//查询没有开启日切的
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
    public void deleteTodayData(Date setTime,String groupId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
//        wrapper.eq("riqie", 0); 有没有设置日切 都删除
        wrapper.eq("group_id", groupId);
        //如果日切时间小于当前时间
        if (new Date().compareTo(setTime)<0){
            wrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                    .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
        }else {
            wrapper.le("add_time",setTime);
        }
        mapper.delete(wrapper);
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
