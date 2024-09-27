package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Resource;
import javax.xml.ws.soap.Addressing;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 操作时间
 */

@Slf4j

public class DateOperator extends AccountBot{
    @Resource
    AccountService accountService;
    @Resource
    RateService rateService;
    @Resource
    IssueService issueService;
    boolean Over24Hour=false;//是否把accountBot 里的删除
    public Date oldSetTime;
    void setOver24Hour(boolean over24Hour) {
    }
    //判断是否过期
    public List<Account> isOver24Hour(Message message, SendMessage sendMessage) {

        List<Account> list=accountService.selectAccount();
        //默认日切是8点
        int i=8;
        Date setTime=new Date();
        if (list.size()>0){
            oldSetTime=list.get(list.size()-1).getSetTime();
            setTime=list.get(list.size()-1).getSetTime();
        }
        log.info("setTime;;;{}",setTime);

        if (message.getText().length()>=4&&message.getText().substring(0,4).equals("设置日切")){
            i = Integer.parseInt(message.getText().substring(4));
            log.info("i:{}",i);
            LocalDateTime fourAMToday = LocalDate.now().atTime(i, 0);
            setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
            log.info("setTime2:{}",setTime);
            accountService.updateSetTime(setTime);
            //过期时间是一天
            rateService.updateOverDue((long) (24 * 60 * 60 * 1000));
            //accService.updateOverDue((long) ( 60 * 1000));
            sendMessage.setText("设置成功");

            try {
                log.info("发送消息2");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
        if (accountService.selectAccount().size()!=0){
            list=accountService.selectAccount();
            //获取设置当天的时间
            long diff =list.get(list.size()-1).getAddTime().getTime()-list.get(list.size()-1).getSetTime().getTime();
            //boolean over24hour=diff > 24 * 60 * 60 * 1000;
            Rate rate=rateService.selectRate();
            log.info("ratesssssssss:{}",rate);
            boolean over24hour=diff >  rate.getOverDue();
            setTime = list.get(list.size()-1).getSetTime();

            if (over24hour){
                Over24Hour=true;
                accountService.updateDataStatus();
                Rate rate1=new Rate();
                accountService.updateSetTime(setTime);
                log.info("setTime,,:{}",setTime);
                rateService.updateRate(String.valueOf(rate1.getRate()));
                rateService.updateExchange(rate1.getExchange());
                log.info("over24hour---------:{}",over24hour);
                list=accountService.selectAccount();
                log.info("listover24:{}",list);
            }
        }
        log.info("Over24Hour,,:{}",Over24Hour);

        setOver24Hour(Over24Hour);

        return list;
    }
    //获取并判断下发订单是否过期
    public List<Issue> issueIsOver24Hour(Message message, SendMessage sendMessage) {
        List<Issue> list=issueService.selectIssue();
        if (!list.isEmpty()){
            oldSetTime=list.get(list.size()-1).getSetTime();
//            list=issueService.selectIssue();
            //获取设置当天的时间
            long diff =list.get(list.size()-1).getAddTime().getTime()-list.get(list.size()-1).getSetTime().getTime();
            //boolean over24hour=diff > 24 * 60 * 60 * 1000;
            Rate rate=rateService.selectRate();
            boolean over24hour=diff >  rate.getOverDue();//如果当天的时间大于设置的逾期时间
            Date setTime = list.get(list.size()-1).getSetTime();

            if (over24hour){
                Over24Hour=true;
                issueService.updateIssueDataStatus();
                Rate rate1=new Rate();
                issueService.updateIssueSetTime(setTime);
                log.info("setTime,,:{}",setTime);
                rateService.updateRate(String.valueOf(rate1.getRate()));
                rateService.updateExchange(rate1.getExchange());
                log.info("over24hour---------:{}",over24hour);
                list=issueService.selectIssue();
                log.info("listover24:{}",list);
            }
        }
        log.info("Over24Hour,,:{}",Over24Hour);

        setOver24Hour(Over24Hour);

        return list;

    }


    // 操作人跟最高权限人都可以删除。 删除今日数据/关闭日切 到时间后账单数据自动保存为历史数据，软件界面内数据全部自动清空，操作员权限保留。
    public void deleteTodayData(Message message, SendMessage sendMessage, List<Account> list, String replyToText) {
        String text = message.getText();
        if (text.length()>=4){
            //删除今日账单关键词： 清理今天数据 删除今天数据 清理今天账单 删除今天账单 是否判断操作员权限？ TODO
            if (text.equals("清理今天数据")||text.equals("删除今天数据")||text.equals("清理今天账单")||text.equals("删除今天账单")){
                accountService.deleteTodayData();
                issueService.deleteTodayIssueData();
                sendMessage.setText("操作成功");
                try {
                    log.info("发送消息3");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("deleteTedayData异常");
                }

            }else if (text.equals("关闭日切")){
                Long overdue=3153600000000l;
                rateService.updateOverDue(overdue);
                sendMessage.setText("操作成功,关闭日切");
                try {
                    log.info("发送消息3");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("deleteTedayData异常");
                }
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
