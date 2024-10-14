package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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

    boolean Over24Hour=false;//是否把accountBot 里的删除
    public Date oldSetTime;//历史账单的过期时间
    void setOver24Hour(boolean over24Hour) {
        this.Over24Hour = over24Hour;
    }
    //判断是否过期
    public List<Account> isOver24Hour(Message message, SendMessage sendMessage) {
        List<Account> accountList=accountService.selectAccountDataStatus0();
        Date setTime=new Date();
        if (accountList.size()>0){
            oldSetTime=accountList.get(accountList.size()-1).getSetTime();
            setTime=accountList.get(accountList.size()-1).getSetTime();
        }
        log.info("setTime;;;{}",setTime);
        if (message.getText().length()>=4&&message.getText().substring(0,4).equals("设置日切")){
            accountService.updateSetTime(setTime);
            Integer hours = Integer.parseInt(message.getText().substring(4, message.getText().length()));
            Date tempDate = new Date();
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(hours).withMinute(tempDate.getMinutes()).withSecond(tempDate.getSeconds()).withNano(0);
            Date OverDue = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            //过期时间是一天
            rateService.updateOverDue(OverDue);
            accountBot.sendMessage(sendMessage,"设置成功 有效期:"+tomorrow.getYear()+"年"+tomorrow.getMonthValue()+ "月"+
                    tomorrow.getDayOfMonth()+"日"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
            return accountList;
        }
        List<Account> accounts = accountService.selectAccountDataStatus0();
        if (accounts.size()!=0){
            accountList=accounts;
            Rate rate=rateService.selectRate();
            setTime = accountList.get(accountList.size()-1).getSetTime();
            //true 已过期  -1表示已过期
            if (-1==accountList.get(accountList.size()-1).getAddTime().compareTo(rate.getOverDue())){
                Over24Hour=true;
                accountService.updateDataStatus();
                Rate rate1=new Rate();
                accountService.updateSetTime(setTime);
                rateService.updateRate(String.valueOf(rate1.getRate()));
                rateService.updateExchange(rate1.getExchange());
                accountList=accountService.selectAccountDataStatus0();
                log.info("已过期:{}  listOver24:{}",rate.getOverDue(),accountList);
            }
        }
        log.info("Over24Hour,,:{}",Over24Hour);
        setOver24Hour(Over24Hour);
        return accountList;
    }
    //获取并判断下发订单是否过期
    public List<Issue> issueIsOver24Hour(Message message, SendMessage sendMessage) {
        List<Issue> list=issueService.selectIssue();
        if (!list.isEmpty()){
            oldSetTime=list.get(list.size()-1).getSetTime();
            Rate rate=rateService.selectRate();
            Date setTime = list.get(list.size()-1).getSetTime();
            //如果当天的时间大于设置的逾期时间
            if (-1==list.get(list.size()-1).getAddTime().compareTo(rate.getOverDue())){
                Over24Hour=true;
                issueService.updateIssueDataStatus();
                Rate rate1=new Rate();
                issueService.updateIssueSetTime(setTime);
                log.info("issueSetTime,,:{}",setTime);
                rateService.updateRate(String.valueOf(rate1.getRate()));
                rateService.updateExchange(rate1.getExchange());
                list=issueService.selectIssue();
                log.info("listOver24:{}",list);
            }
        }
        log.info("Over24Hour,:{}",Over24Hour);
        setOver24Hour(Over24Hour);
        return list;
    }

    // 操作人跟最高权限人都可以删除。 删除今日数据/关闭日切 到时间后账单数据自动保存为历史数据，软件界面内数据全部自动清空，操作员权限保留。
    public void deleteTodayData(Message message, SendMessage sendMessage, List<Account> list, String replyToText) {
        String text = message.getText();
        if (text.length()>=4){
            //删除今日账单关键词： 清理今天数据 删除今天数据 清理今天账单 删除今天账单 是否判断操作员权限？
            if (text.equals("清理今天数据")||text.equals("删除今天数据")||text.equals("清理今天账单")
                    ||text.equals("清理今日账单")||text.equals("删除今日账单")||text.equals("清理今天帐单")
                    ||text.equals("删除今天账单")||text.equals("删除账单") ||text.equals("删除今天帐单")||text.equals("删除帐单")
                    ||text.equals("清除账单")||text.equals("删除账单")||text.equals("清除帐单")||text.equals("删除帐单")){
                accountService.deleteTodayData();
                issueService.deleteTodayIssueData();
                accountBot.sendMessage(sendMessage,"操作成功");
            }else if (text.equals("删除全部账单")||text.equals("清除全部账单")){
                accountService.deleteHistoryData();
                issueService.deleteHistoryIssueData();
                accountBot.sendMessage(sendMessage,"操作成功");
            }else if (text.equals("关闭日切")){
                Date overdue=new Date();
                rateService.updateOverDue(overdue);
                accountBot.sendMessage(sendMessage,"操作成功,关闭日切");
            }
        }
    }

    //转换时间
    public  Date timeExchange(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // 将字符串转换为 LocalTime 类型
        LocalTime time = LocalTime.parse(timeStr, formatter);
        // 获取当前日期
        LocalDateTime localDateTime = LocalDateTime.now().with(time);
        // 转换为指定时区的日期时间
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("GMT+8"));
        // 转换为 Date 类型
        Date addTime = Date.from(zonedDateTime.toInstant());
        // 输出转换后的日期
        log.info("Converted date: " + addTime);
        return addTime;
    }
}
