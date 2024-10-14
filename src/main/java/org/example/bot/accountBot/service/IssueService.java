package org.example.bot.accountBot.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.IssueMapper;
import org.example.bot.accountBot.pojo.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@Service
@Slf4j
@Transactional("")
public class IssueService {

    @Autowired
    IssueMapper mapper;

    public List<Issue> selectIssue() {
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("data_status", 0);
        return mapper.selectList(queryWrapper);
    }

    public void updateIssueDataStatus() {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("data_status", 1);
        mapper.update(null,updateWrapper);
    }

    public void updateIssueSetTime(Date setTime) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("set_time", setTime);
        mapper.update(null,updateWrapper);
    }

    public void insertIssue(Issue issue) {
        mapper.insert(issue);
    }
    //添加 减少 有添加或者减少的 down
    public void updateIssueDown(BigDecimal down) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("down", down);
        mapper.update(null,updateWrapper);
    }
    public void deleteTodayIssueData() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        QueryWrapper<Issue> wrapper = new QueryWrapper<>();
        wrapper.eq("data_status", 0);
        wrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));
        mapper.delete(wrapper);
    }
    public void deleteHistoryIssueData() {
        QueryWrapper<Issue> wrapper = new QueryWrapper<>();
        wrapper.eq("data_status", 0);
        mapper.delete(wrapper);
    }

    public void deleteNewestIssue(Date addTime) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("add_time", addTime);
        mapper.delete(updateWrapper);
    }

}
