package com.clock.bot.config;

import com.clock.bot.botConfig.ClockBot;
import com.clock.bot.pojo.Status;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.StatusService;
import com.clock.bot.service.UserOperationService;
import com.clock.bot.service.UserStatusService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

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
                sendReminder(status, "提前一个小时提醒：请尽快打卡下班。");
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
            userStatus.setStatus(false); // 更新下班状态
            userStatus.setReturnHome(false); // 更新回家状态
            userStatus.setWorkDownTime(new Date()); // 更新工作结束时间
            userStatusService.updateUserStatus(userStatus);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(status.getGroupId());
            sendMessage.setText("您已强制下班");
            clockBot.sendMessage(sendMessage, "您已强制下班");
        }
    }
}


