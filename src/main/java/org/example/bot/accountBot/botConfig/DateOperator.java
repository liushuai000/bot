package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Rate;
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
public class DateOperator {
    boolean Over24Hour=false;//是否把accountBot 里的删除
    //判断是否过期
    private List<Account> isOver24Hour(Message message, SendMessage sendMessage) {

        List<Account> list=accService.selectAccount();
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
            accService.updateSetTime(setTime);
            //过期时间是一天
            accService.updateOverDue((long) (24 * 60 * 60 * 1000));
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
        if (accService.selectAccount().size()!=0){
            list=accService.selectAccount();
            //获取设置当天的时间
            long diff =list.get(list.size()-1).getAddTime().getTime()-list.get(list.size()-1).getSetTime().getTime();
            //boolean over24hour=diff > 24 * 60 * 60 * 1000;
            Rate rate=accService.selectRate();
            log.info("ratesssssssss:{}",rate);
            boolean over24hour=diff >  rate.getOverDue();
            setTime = list.get(list.size()-1).getSetTime();

            if (over24hour){
                Over24Hour=true;
                accService.updateDataStatus();
                Rate rate1=new Rate();
                accService.updateSetTime(setTime);
                log.info("setTime,,:{}",setTime);
                accService.updateRate(String.valueOf(rate1.getRate()));
                accService.updateExchange(rate1.getExchange());
                log.info("over24hour---------:{}",over24hour);
                list=accService.selectAccount();
                log.info("listover24:{}",list);
            }
        }
        log.info("Over24Hour,,:{}",Over24Hour);

        setOver24Hour(Over24Hour);

        return list;
    }



    //转换时间
    private  Date timeExchange(String timeStr) {
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
