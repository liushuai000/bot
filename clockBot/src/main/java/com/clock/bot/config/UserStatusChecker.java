package com.clock.bot.config;

import com.clock.bot.botConfig.ClockBot;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.UserOperationService;
import com.clock.bot.service.UserStatusService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
public class UserStatusChecker {
    @Autowired
    private ClockBot clockBot;
    @Autowired
    private UserStatusService userStatusService;
    @Autowired
    private UserOperationService userOperationService;
    @Value("${scheduler.fixed-rate}")
    private long fixedRate;
    @Scheduled(fixedRateString = "${scheduler.fixed-rate}")
    public void checkUserStatus() {
        // 调用方法检查用户状态
        checkAndSendMessageIfTimeout();
    }


    private void checkAndSendMessageIfTimeout() {
        // 查询所有 userStatus 中 userOperationId 不为空的记录
        List<UserStatus> userStatuses = userStatusService.findUserStatusWithUserOperationId();
        for (UserStatus userStatus : userStatuses) {
            // 根据 userOperationId 查询 userOperation 表
            UserOperation userOperation = userOperationService.findById(userStatus.getUserOperationId());
            if (userOperation != null && userOperation.getReminderCount() < 3) {
                if (userOperation.getEndTime()==null){//只有没有结束操作的人 才监听
                    // 计算 startTime 是否超过 30 分钟
                    long startTime = userOperation.getStartTime().getTime();
                    long currentTime = System.currentTimeMillis();
                    long timeDifference = (currentTime - startTime) / 1000; // 转换为秒
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(userStatus.getGroupId());
                    String message = OperationReminder.generateReminderMessage(userOperation, userStatus,timeDifference);
                    if (StringUtils.isNotBlank(message)) {
                        userOperationService.updateUserOperation(userOperation);
                        clockBot.sendMessage(sendMessage, message);
                    }
                }
            }
        }
    }

}

