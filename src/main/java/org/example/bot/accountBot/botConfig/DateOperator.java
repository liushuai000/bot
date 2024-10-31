package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作时间
 */

@Slf4j
@Service
public class DateOperator{
    @Autowired
    AccountService accountService;
    @Autowired
    RateService rateService;
    @Autowired
    IssueService issueService;
    @Autowired
    AccountBot accountBot;
    @Autowired
    StatusService statusService;

    //判断是否过期 groupId text  查询是否过期
    public void isOver24HourCheck(Message message, SendMessage sendMessage, UserDTO userDTO, Status status) {
        if (userDTO.getText().length()>=4&&userDTO.getText().substring(0,4).equals("设置日切")
                ||userDTO.getText().length()>=4&&userDTO.getText().substring(0,4).equals("开启日切")){
            LocalDateTime tomorrow;
            if (StringUtils.isBlank(message.getText().substring(4, message.getText().length()))){
                tomorrow=LocalDateTime.now().plusDays(1).withHour(11).withMinute(59).withSecond(59).withNano(0);
            }else {
                Integer hours = Integer.parseInt(message.getText().substring(4, message.getText().length()));
                tomorrow = LocalDateTime.now().plusDays(1).withHour(hours-1).withMinute(59).withSecond(59).withNano(0);
            }
            Date OverDue = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            status.setRiqie(true);//是否开启日切 是
            status.setSetTime(OverDue);//设置日切时间
            statusService.update(status);
            //需要更新Rate init设置 已过日切的使用旧的Rate之前群组设置的  没过日切的使用新Rate TODO
            accountBot.sendMessage(sendMessage,"设置成功 日切时间为每天:"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
        }
    }

    // 操作人跟最高权限人都可以删除。 删除今日数据/关闭日切 到时间后账单数据自动保存为历史数据，软件界面内数据全部自动清空，操作员权限保留。
    public void deleteTodayData(Message message, SendMessage sendMessage,String groupId,Status status,List<Account> accountList,List<Issue> issueList) {
        String text = message.getText();
        if (text.length()>=4){
            //删除今日账单关键词： 清理今天数据 删除今天数据 清理今天账单 删除今天账单 是否判断操作员权限？
            if (text.equals("清理今天数据")||text.equals("删除今天数据")||text.equals("清理今天账单")
                    ||text.equals("清理今日账单")||text.equals("删除今日账单")||text.equals("清理今天帐单")
                    ||text.equals("删除今天账单")||text.equals("删除账单") ||text.equals("删除今天帐单")||text.equals("删除帐单")
                    ||text.equals("清除账单")||text.equals("删除账单")||text.equals("清除帐单")||text.equals("删除帐单")){
                accountService.deleteTodayData(status,groupId);
                issueService.deleteTodayIssueData(status,groupId);
                accountBot.sendMessage(sendMessage,"操作成功 ，今日账单已删除");
            }else if (text.equals("删除全部账单")||text.equals("清除全部账单")){
                accountService.deleteHistoryData(groupId);
                issueService.deleteHistoryIssueData(groupId);
                accountBot.sendMessage(sendMessage,"操作成功 ，全部账单已删除。");
            }else if (text.equals("关闭日切")){
                status.setRiqie(false);//是否开启日切 是
                Date date = new Date();
                status.setSetTime(date);//设置日切时间
                //如果是最后一次关闭日切的账单  需要在新账单体现 updateTime
                accountList.stream().filter(Objects::nonNull).forEach(account -> accountService.updateLastUpdateRiqie(account.getId(),false,date));
                issueList.stream().filter(Objects::nonNull).forEach(issue -> issueService.updateLastUpdateRiqie(issue.getId(),false,date));
                statusService.update(status);
                accountBot.sendMessage(sendMessage,"操作成功,关闭日切");
            }
        }
    }
    public List<Issue> selectIsIssueRiqie(SendMessage sendMessage, Status status, String groupId) {
        List<Issue> issueList=issueService.selectIssueRiqie(status.isRiqie(),groupId);
        List<Issue> issues = new ArrayList<>();
        issueList.stream().filter(Objects::nonNull).forEach(issue -> {
            //如果关闭了日切 updateTime大于等于日切时间
            if (!status.isRiqie()){
                if (issue.getUpdateTime()==null && issue.getAddTime().compareTo(status.getSetTime())>0){
                    issues.add(issue);
                }
                if (issue.getUpdateTime()!=null && status.getSetTime().compareTo(issue.getUpdateTime())==0){
                    issues.add(issue);
                }
            }else if (issue.getUpdateTime()==null && status.isRiqie()){
                issues.add(issue);
            }
        });
        return issues;
    }
    //如果日切时间超时
    public List<Account> selectIsRiqie(SendMessage sendMessage, Status status, String groupId) {
        List<Account> accountList=accountService.selectAccountRiqie(status.isRiqie(),status.getSetTime(),groupId);//status.getSetTime() 这个没有用
        List<Account> accounts = new ArrayList<>();
        accountList.stream().filter(Objects::nonNull).forEach(account -> {
            //如果关闭了日切 updateTime大于等于日切时间
            if (!status.isRiqie()){
                log.info("执行了setTime???st:{}",status.getSetTime().compareTo(account.getAddTime())>0);
                if (account.getUpdateTime()==null && account.getAddTime().compareTo(status.getSetTime())>0){
                    accounts.add(account);
                }
                if (account.getUpdateTime()!=null && status.getSetTime().compareTo(account.getUpdateTime())==0){
                    accounts.add(account);
                }
            }else if (account.getUpdateTime()==null && status.isRiqie()){
                accounts.add(account);
            }
        });
        return accounts;
    }
    //取今天的日切时间 +关闭日切后的账单 默认日切时间中午12点
    public void checkRiqie(SendMessage sendMessage,Status status, List<Account> accountList,List<Issue> issueList) {
        //当前时间小于日切时间
        if (status.isRiqie() && status.getSetTime().compareTo(new Date())<0){
            Date setTime = status.getSetTime();
            setTime.setHours(24);
            status.setSetTime(setTime);
            statusService.update(status);
            accountList.stream().filter(Objects::nonNull).filter(account -> account.isRiqie())
                    .forEach(a->accountService.updateRiqie(a.getId(),false));//修改为不是日切的账单
            issueList.stream().filter(Objects::nonNull).filter(issue -> issue.isRiqie())
                    .forEach(a->issueService.updateRiqie(a.getId(),false));//修改为不是日切
            //日切时间已更新，当前日切时间为 ：每天:11时59分59秒
            accountBot.sendMessage(sendMessage,"日切时间已更新，当前日切时间为 ：每天:"+status.getSetTime().getHours()+"时"+
                    status.getSetTime().getMinutes()+"分"+status.getSetTime().getSeconds()+"秒");
        }
    }

}
