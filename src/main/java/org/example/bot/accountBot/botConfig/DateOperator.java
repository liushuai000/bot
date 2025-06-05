package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.StatusService;
import org.example.bot.accountBot.utils.ConstantMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    Map<String, String> constantMap = ConstantMap.COMMAND_MAP_ENGLISH;
    //判断是否过期 groupId text  查询是否过期
    public void isOver24HourCheck(Message message, SendMessage sendMessage, UserDTO userDTO, Status status,List<Account> accountList,List<Issue> issueList) {
        String lowerCase = userDTO.getText().toLowerCase();
        if ((userDTO.getText().length()>=4&&userDTO.getText().substring(0,4).equals("设置日切")
                ||userDTO.getText().length()>=4&&userDTO.getText().substring(0,4).equals("开启日切"))
        ||(userDTO.getText().length()>=4&&userDTO.getText().substring(0,4).equals(constantMap.get("设置日切"))
                ||userDTO.getText().length()>=4&&userDTO.getText().substring(0,4).equals(constantMap.get("开启日切"))) ){
            this.dataInfo(lowerCase,status,sendMessage,4,lowerCase.length());
        }else if (lowerCase.startsWith("set daily switch")){
            this.dataInfo(lowerCase,status,sendMessage,"set daily switch".length(),lowerCase.length());
        }else if (lowerCase.startsWith("enable daily switch")){
            this.dataInfo(lowerCase,status,sendMessage,"enable daily switch".length(),lowerCase.length());
        }
    }
    public void dataInfo(String lowerCase,Status status,SendMessage sendMessage,Integer length,Integer endLength){
        LocalDateTime tomorrow;
        if (StringUtils.isBlank(lowerCase.substring(length, endLength))){
            //如果当前时间大于12点，则设置明天的12点为日切时间
            if (12>new Date().getHours()){
                tomorrow=LocalDateTime.now().plusDays(0).withHour(12).withMinute(0).withSecond(0).withNano(1);
            }else {
                tomorrow = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(1);
            }
        }else {
            Integer hours = Integer.parseInt(lowerCase.substring(length, endLength));
            //16 >15
            if (hours>new Date().getHours()){
                tomorrow = LocalDateTime.now().plusDays(0).withHour(hours).withMinute(0).withSecond(0).withNano(1);
            }else {
                tomorrow = LocalDateTime.now().plusDays(1).withHour(hours).withMinute(0).withSecond(0).withNano(1);
            }
        }
        Date OverDue = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
        status.setRiqie(true);//是否开启日切 是
        status.setSetTime(OverDue);//设置日切时间
        // ✅ 修改：设置日切开始时间为当前时间减去24小时
        status.setSetStartTime(Date.from(Instant.now().minus(24, ChronoUnit.HOURS)));
        statusService.update(status);//accountList 更新账单日切时间
        // 计算两个日期之间的毫秒差
        long differenceInMillis = OverDue.getTime()-new Date().getTime();
        // 转换为小时、分钟和秒
        long hours = differenceInMillis / (1000 * 60 * 60);
        long minutes = (differenceInMillis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (differenceInMillis % (1000 * 60)) / 1000;
        //机器人进群  首次进群 如果有账单就查所有 没有就默认
//            accountList.stream().filter(Objects::nonNull).forEach(a->accountService.updateSetTime(a.getId()+"",OverDue));
//        issueList.stream().filter(Objects::nonNull).forEach(a->issueService.updateLastUpdateRiqie(a.getId(),OverDue));
        accountBot.sendMessage(sendMessage,"设置成功 日切时间为每天:"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒!\n" +
                "距离日切时间结束还有:"+ hours+"小时"+minutes+"分钟"+seconds+"秒");
    }



    // 操作人跟最高权限人都可以删除。 删除今日数据/关闭日切 到时间后账单数据自动保存为历史数据，软件界面内数据全部自动清空，操作员权限保留。
    public void deleteTodayData(Message message, SendMessage sendMessage,String groupId,Status status,List<Account> accountList,List<Issue> issueList) {
        String text = message.getText().toLowerCase();
        if (text.length()>=4){
            //删除今日账单关键词： 清理今天数据 删除今天数据 清理今天账单 删除今天账单 是否判断操作员权限？
            if ((text.equals("清理今天数据")||text.equals("删除今天数据")||text.equals("清理今天账单")
                    ||text.equals("清理今日账单")||text.equals("删除今日账单")||text.equals("清理今天帐单")
                    ||text.equals("删除今天账单")||text.equals("删除账单") ||text.equals("删除今天帐单")||text.equals("删除帐单")
                    ||text.equals("清除账单")||text.equals("删除账单")||text.equals("清除帐单")||text.equals("删除帐单"))
                    ||  (text.equals(constantMap.get("清理今天数据"))||text.equals(constantMap.get("删除今天数据"))||text.equals(constantMap.get("清理今天账单"))
                    ||text.equals(constantMap.get("清理今日账单"))||text.equals("删除今日账单")||text.equals("清理今天帐单")
                    ||text.equals(constantMap.get("删除今天账单"))||text.equals(constantMap.get("删除账单")) ||text.equals(constantMap.get("删除今天帐单"))||text.equals(constantMap.get("删除帐单"))
                    ||text.equals(constantMap.get("清除账单"))||text.equals(constantMap.get("删除账单"))||text.equals(constantMap.get("清除帐单"))||text.equals(constantMap.get("删除帐单")))){
                accountService.deleteTodayData(status,groupId);
                issueService.deleteTodayIssueData(status,groupId);
                accountBot.sendMessage(sendMessage,"操作成功 ，今日账单已删除");
            }else if (text.equals("删除全部账单")||text.equals("清除全部账单")||text.equals(constantMap.get("删除全部账单")) || text.equals(constantMap.get("清除全部账单"))
            ||text.equals("delete all bills")||text.equals("delete all records")|| text.equals("clear all bills")){
                accountService.deleteHistoryData(groupId);
                issueService.deleteHistoryIssueData(groupId);
                accountBot.sendMessage(sendMessage,"操作成功 ，全部账单已删除。");
            }else if (text.equals("关闭日切")||text.equals(constantMap.get("关闭日切"))|| text.equals("disable daily switch")){
                status.setRiqie(false);//是否开启日切 是
                Date date = new Date();
                status.setSetTime(date);//设置日切时间
                statusService.update(status);
                accountBot.sendMessage(sendMessage,"操作成功,关闭日切");
            }
        }
    }
    public List<Issue> selectIsIssueRiqie(SendMessage sendMessage, Status status, String groupId) {
        List<Issue> issueList=issueService.selectIssueRiqie(status.isRiqie(),status.getSetTime(),groupId);
        // 如果未开启日切，直接返回所有账单
        if (!status.isRiqie()) {
            return issueList;
        }
        // 获取日切开始时间和结束时间
        Date setStartTime = status.getSetStartTime();
        Date setTime = status.getSetTime();
        return issueList.stream().filter(Objects::nonNull).filter(account -> {
            Date addTime = account.getAddTime();
            return addTime != null &&
                    !addTime.before(setStartTime) && !addTime.after(setTime);
        }).collect(Collectors.toList());
    }
    public List<Account> selectIsRiqie(SendMessage sendMessage, Status status, String groupId) {
        // 获取原始账单数据
        List<Account> accountList = accountService.selectAccountRiqie(status.isRiqie(), status.getSetTime(), groupId);
        // 如果未开启日切，直接返回所有账单
        if (!status.isRiqie()) {
            return accountList;
        }
        // 获取日切开始时间和结束时间
        Date setStartTime = status.getSetStartTime();
        Date setTime = status.getSetTime();
        // 筛选在 [setStartTime, setTime] 时间段内的账单
        return accountList.stream().filter(Objects::nonNull).filter(account -> {
                    Date addTime = account.getAddTime();
                    return addTime != null &&
                            !addTime.before(setStartTime) && !addTime.after(setTime);
                }).collect(Collectors.toList());
    }

    public void checkRiqie(SendMessage sendMessage,Status status) {
        //当前时间小于日切时间
        if (status.isRiqie() ){
            if (status.getSetTime().compareTo(new Date())<=0){
                Date setTime = status.getSetTime();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date()); // 使用当前时间
                // 获取 setTime 的时分秒
                Calendar setTimeCalendar = Calendar.getInstance();
                setTimeCalendar.setTime(setTime);
                // 设置当前日期，但保留 setTime 的时分秒
                calendar.set(Calendar.HOUR_OF_DAY, setTimeCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, setTimeCalendar.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, setTimeCalendar.get(Calendar.SECOND));
                calendar.set(Calendar.MILLISECOND, setTimeCalendar.get(Calendar.MILLISECOND));
                calendar.add(Calendar.HOUR_OF_DAY, 24);
                setTime = calendar.getTime(); // 获取更新后的时间
                status.setSetTime(setTime);
                status.setSetStartTime(new Date());
                statusService.update(status);
                //日切时间已更新，当前日切时间为 ：每天:11时59分59秒
                accountBot.sendMessage(sendMessage,"日切时间已更新，当前日切时间为 ：每天:"+status.getSetTime().getHours()+"时"+
                        status.getSetTime().getMinutes()+"分"+status.getSetTime().getSeconds()+"秒");
            }
        }
    }

}
