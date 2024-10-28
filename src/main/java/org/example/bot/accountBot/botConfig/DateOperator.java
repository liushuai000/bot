package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
            Integer hours = Integer.parseInt(message.getText().substring(4, message.getText().length()));
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(hours).withMinute(59)
                    .withSecond(59).withNano(0);
            Date OverDue = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            status.setRiqie(true);//是否开启日切 是
            status.setSetTime(OverDue);//设置日切时间
            statusService.update(status);
            //需要更新Rate init设置 已过日切的使用旧的Rate之前群组设置的  没过日切的使用新Rate TODO
            accountBot.sendMessage(sendMessage,"设置成功 有效期:"+tomorrow.getYear()+"年"+tomorrow.getMonthValue()+ "月"+
                    tomorrow.getDayOfMonth()+"日"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
        }
    }

    // 操作人跟最高权限人都可以删除。 删除今日数据/关闭日切 到时间后账单数据自动保存为历史数据，软件界面内数据全部自动清空，操作员权限保留。
    public void deleteTodayData(Message message, SendMessage sendMessage,String groupId,Status status) {
        String text = message.getText();
        if (text.length()>=4){
            //删除今日账单关键词： 清理今天数据 删除今天数据 清理今天账单 删除今天账单 是否判断操作员权限？
            if (text.equals("清理今天数据")||text.equals("删除今天数据")||text.equals("清理今天账单")
                    ||text.equals("清理今日账单")||text.equals("删除今日账单")||text.equals("清理今天帐单")
                    ||text.equals("删除今天账单")||text.equals("删除账单") ||text.equals("删除今天帐单")||text.equals("删除帐单")
                    ||text.equals("清除账单")||text.equals("删除账单")||text.equals("清除帐单")||text.equals("删除帐单")){
                //如果设置了日切 没有日切就是今天的 就删除日切的  删除是改状态datastatus 为0不可见还是 真删除
                accountService.deleteTodayData(status.getSetTime(),groupId);
                issueService.deleteTodayIssueData(status.getSetTime(),groupId);
                accountBot.sendMessage(sendMessage,"操作成功");
            }else if (text.equals("删除全部账单")||text.equals("清除全部账单")){
                accountService.deleteHistoryData(groupId);
                issueService.deleteHistoryIssueData(groupId);
                accountBot.sendMessage(sendMessage,"操作成功");
            }else if (text.equals("关闭日切")){
                status.setRiqie(false);//是否开启日切 是
                status.setSetTime(new Date());//设置日切时间
                statusService.update(status);
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


    //如果日切时间超时 我需要删除开启日切的账单并且需要关闭日切
    public List<Account> selectAccountIsRiqie(SendMessage sendMessage,Status status, String groupId) {
        //搜索出历史账单/判断是否过期
        List<Account> accountList=accountService.selectAccountRiqie(status.isRiqie(),groupId);
        //当前时间小于日切时间
        if (status.isRiqie() && new Date().compareTo(status.getSetTime())<0){
            status.setRiqie(false);
            statusService.update(status);
            accountList.stream().filter(Objects::nonNull).filter(account -> account.isRiqie())
                    .forEach(a->accountService.deleteById(a.getId()));
            accountList = accountList.stream().filter(Objects::nonNull).filter(account -> !account.isRiqie()).collect(Collectors.toList());
            accountBot.sendMessage(sendMessage,"日切时间已到期!已关闭日切"+status.getSetTime());
        }
        return accountList;
    }


}
