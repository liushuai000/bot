package org.example.bot.accountBot.service;


import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.IssueMapper;
import org.example.bot.accountBot.pojo.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
@Service
@Slf4j
public class IssueService {

    @Autowired
    IssueMapper mapper;

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

}
