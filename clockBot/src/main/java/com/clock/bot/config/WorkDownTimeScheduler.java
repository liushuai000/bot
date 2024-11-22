package com.clock.bot.config;

import com.clock.bot.botConfig.ClockBot;
import com.clock.bot.pojo.Status;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.StatusService;
import com.clock.bot.service.UserOperationService;
import com.clock.bot.service.UserStatusService;
import com.clock.bot.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class WorkDownTimeScheduler {
    @Autowired
    private StatusService statusService;

    @Autowired
    private UserStatusService userStatusService;

    @Autowired
    private UserOperationService userOperationService;

    @Autowired
    private ClockBot clockBot;
    DateUtils dateUtils=new DateUtils();
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void updateWorkDownTime() {
        List<Status> statuses = statusService.selectStatusList();
        LocalDateTime now = LocalDateTime.now();

        for (Status status : statuses) {
            Date currentCutOffTime = status.getCurrentCutOffTime();
            LocalDateTime cutOffTime = currentCutOffTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            long minutesUntilCutOff = ChronoUnit.MINUTES.between(now, cutOffTime);
            if (minutesUntilCutOff == 60) {
                // 提前一个小时提醒
                sendReminder(status, "下班提醒❌：距离强制下班日切时间还有1小时，到时间将被强制下班。");
            } else if (minutesUntilCutOff == 30) {
                // 提前半小时提醒
                sendReminder(status, "提前半小时提醒：请尽快打卡下班。");
            } else if (minutesUntilCutOff == 5) {
                // 提前5分钟强制下班
                forceWorkDown(status);
            }
        }
    }

    private void sendReminder(Status status, String message) {
        List<UserStatus> userStatuses = userStatusService.findByGroupId(status.getGroupId());
        for (UserStatus userStatus : userStatuses) {
            if (StringUtils.isNotBlank(userStatus.getUserOperationId())) {
                UserOperation userOperation = userOperationService.findById(userStatus.getUserOperationId());
                if (userOperation.getEndTime() == null) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(status.getGroupId());
                    sendMessage.setText(message);
                    clockBot.sendMessage(sendMessage, message);
                }
            }
        }
    }

    private void forceWorkDown(Status status) {
        List<UserStatus> userStatuses = userStatusService.findByGroupId(status.getGroupId());
        for (UserStatus userStatus : userStatuses) {
            if (StringUtils.isNotBlank(userStatus.getUserOperationId())) {
                UserOperation userOperation = userOperationService.findById(userStatus.getUserOperationId());
                if (userOperation.getEndTime() == null) {
                    userOperation.setEndTime(new Date()); // 设置结束时间为当前时间
                    userOperationService.updateUserOperation(userOperation);
                }
            }
            Date date = new Date();
            userStatus.setStatus(false); // 更新下班状态
            userStatus.setReturnHome(false); // 更新回家状态
            userStatus.setWorkDownTime(date); // 更新工作结束时间
            userStatusService.updateUserStatus(userStatus);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(status.getGroupId());
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int month = localDate.getMonthValue();
            int day = localDate.getDayOfMonth();
            String dateFromat =month+"/"+day+" "+date.getHours()+":"+date.getMinutes()+"\n";
            String message=getMessage(userStatus.getUsername(),userStatus.getUserId(),userStatus.getGroupId(),dateFromat);
            sendMessage.setText("@"+userStatus.getUsername()+"您已强制下班");
            clockBot.sendMessage(sendMessage, "您已强制下班\n"+message);
        }
    }


    public String getMessage(String username,String userId,String groupId,String dateFromat){
        List<UserStatus> userStatuses=userStatusService.selectTodayUserStatus(userId,groupId);
        // 计算总工作时间
        Duration totalDuration = userStatuses.stream().filter(Objects::nonNull).map(UserStatus::getDuration).reduce(Duration.ZERO, Duration::plus);
        List<Integer> ids = userStatuses.stream().filter(Objects::nonNull).map(UserStatus::getId).collect(Collectors.toList());
        List<UserOperation> operations=userOperationService.findByUserStatusIds(ids);
        AtomicLong seconds = new AtomicLong();
        AtomicReference<Integer> eat = new AtomicReference<>(0);
        AtomicReference<Integer> wc = new AtomicReference<>(0);
        AtomicReference<Integer> smoking = new AtomicReference<>(0);
        AtomicReference<Integer> other = new AtomicReference<>(0);
        AtomicLong eatSeconds = new AtomicLong();
        AtomicLong wcSeconds = new AtomicLong();
        AtomicLong smokingSeconds = new AtomicLong();
        AtomicLong otherSeconds = new AtomicLong();
        operations.stream().filter(Objects::nonNull).forEach(operation->{
            //"吃饭","上厕所","抽烟","其它"
            if (operation.getOperation().equals("吃饭")){
                eat.updateAndGet(v -> v + 1);
                Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                eatSeconds.addAndGet(between.getSeconds());
            }else if (operation.getOperation().equals("上厕所")) {
                wc.updateAndGet(v -> v + 1);
                Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                wcSeconds.addAndGet(between.getSeconds());
            }else if (operation.getOperation().equals("抽烟")) {
                smoking.updateAndGet(v -> v + 1);
                Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                smokingSeconds.addAndGet(between.getSeconds());
            }else if (operation.getOperation().equals("其它")) {
                other.updateAndGet(v -> v + 1);
                Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                otherSeconds.addAndGet(between.getSeconds());
            }
            Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
            seconds.addAndGet(between.getSeconds());
        });
        String eatText=eat.get()==0?"":"本日吃饭："+eat.get()+" 次\n";
        String wcText=wc.get()==0?"":"本日上厕所："+wc.get()+" 次\n";
        String smokingText=smoking.get()==0?"":"本日抽烟："+smoking.get()+" 次\n";
        String otherText=other.get()==0?"":"本日其它："+other.get()+" 次\n";
        String o1 = eatSeconds.get() == 0 ? "" : "今日累计吃饭时间："+dateUtils.formatDuration(eatSeconds.get())+"\n";
        String o2 = wcSeconds.get() == 0 ? "" : "今日累计上厕所时间："+dateUtils.formatDuration(wcSeconds.get())+"\n";
        String o3 = smokingSeconds.get() == 0 ? "" : "今日累计抽烟时间："+dateUtils.formatDuration(smokingSeconds.get())+"\n";
        String o4 = otherSeconds.get() == 0 ? "" : "今日累计其它时间："+dateUtils.formatDuration(otherSeconds.get())+"\n";
        String text1;
        if (eatSeconds.get()==0&&wcSeconds.get()==0&&smokingSeconds.get()==0&&otherSeconds.get()==0){
            text1="";
        }else {
            text1=o1+o2+o3+o4+"------------------------\n" ;
        }
        String text;
        if (eat.get()==0&&wc.get()==0&&smoking.get()==0&&other.get()==0){
            text="";
        }else {
            text=eatText+wcText+smokingText+otherText+"------------------------\n" ;
        }
        long pureWorkTimeSeconds = totalDuration.getSeconds() - seconds.get(); // 纯工作时间
        String pureWorkTimeString = dateUtils.formatDuration(pureWorkTimeSeconds);
        String todayWorkTime =dateUtils.formatDuration(totalDuration.getSeconds());
        String huodong=seconds.get()==0?"":"今日累计活动总时间：" +dateUtils.formatDuration(seconds.get())+"\n";
        return  "用户：" + username + "\n" +
                "用户标识：<code>" + userId + "</code>\n" +
                "✅ 打卡成功：下班 - <code>" + dateFromat + "</code>\n" +
                "提示：本日工作时间已结算\n" +
                "今日工作总计：" +todayWorkTime+ "\n" +
                "纯工作时间：" + pureWorkTimeString + "\n" +
                "------------------------\n" +
                huodong+
                text1 + text ;
    }


}


