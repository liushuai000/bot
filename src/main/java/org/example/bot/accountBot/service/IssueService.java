package org.example.bot.accountBot.service;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.IssueMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Status;
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
@Transactional
public class IssueService {

    @Autowired
    IssueMapper mapper;

    public List<Issue> selectIssueRiqie(boolean riqie,String groupId) {
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("riqie", riqie);
        queryWrapper.orderByDesc("add_time");
        return mapper.selectList(queryWrapper);
    }

    public void insertIssue(Issue issue) {
        mapper.insert(issue);
    }
    //添加 减少 有添加或者减少的 down
    public void updateIssueDown(BigDecimal down, String groupId) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id", groupId);
        updateWrapper.set("down", down);
        mapper.update(null,updateWrapper);
    }
    public void deleteTodayIssueData(Status status, String groupId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        QueryWrapper<Issue> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        if (!status.isRiqie()){
            wrapper.eq("riqie", false);
        }else {
            wrapper.eq("riqie", true);
        }
        wrapper.ge("add_time", Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant()))
                .le("add_time", Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant()));

        mapper.delete(wrapper);

    }
    public void deleteHistoryIssueData(String groupId) {
        QueryWrapper<Issue> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        mapper.delete(wrapper);
    }

    public void deleteNewestIssue(String id, String groupId) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.eq("group_id", groupId);
        mapper.delete(updateWrapper);
    }

    public void updateLastUpdateRiqie(int id,boolean riqie,Date updateTime) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("update_time",updateTime);
        updateWrapper.set("riqie",riqie);
        mapper.update(null,updateWrapper);
    }
    public void updateRiqie(int id, boolean riqie) {
        UpdateWrapper<Issue> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        updateWrapper.set("riqie",riqie);
        mapper.update(null,updateWrapper);
    }
}
