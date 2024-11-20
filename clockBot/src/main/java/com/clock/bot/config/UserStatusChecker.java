package com.clock.bot.config;

import com.clock.bot.botConfig.ClockBot;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.UserOperationService;
import com.clock.bot.service.UserStatusService;
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
    @Value("${eatOutTime}")
    private Integer eatOutTime; //#吃饭超时时间 单位秒
    @Value("${wcOutTime}")
    private Integer wcOutTime;
    @Value("${smokingOutTime}")
    private Integer smokingOutTime;
    @Value("${otherOutTime}")
    private Integer otherOutTime;

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
                    // 计算 startTime 是否超过 30 秒
                    long startTime = userOperation.getStartTime().getTime();
                    long currentTime = System.currentTimeMillis();
                    long timeDifference = (currentTime - startTime) / 1000; // 转换为秒
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(userStatus.getGroupId());
                    userOperation.setReminderCount(userOperation.getReminderCount()+1);
                    userOperationService.updateUserOperation(userOperation);
                    if (userOperation.getOperation().equals("吃饭")){
                        if (timeDifference>eatOutTime)  sendMessage.setText("用户 @" + userStatus.getUsername() + " 的"+
                                    userOperation.getOperation()+"操作已超时 "+eatOutTime+" 秒。已提醒:"+userOperation.getReminderCount()+"次");
                    }else if (userOperation.getOperation().equals("上厕所")){
                        if (timeDifference>wcOutTime)  sendMessage.setText("用户 @" + userStatus.getUsername() + " 的"+
                                userOperation.getOperation()+"操作已超时 "+wcOutTime+" 秒。已提醒:"+userOperation.getReminderCount()+"次");
                    } else if (userOperation.getOperation().equals("抽烟")) {
                        if (timeDifference>smokingOutTime)  sendMessage.setText("用户 @" + userStatus.getUsername() + " 的"+
                                userOperation.getOperation()+"操作已超时 "+smokingOutTime+" 秒。已提醒:"+userOperation.getReminderCount()+"次");
                    } else if (userOperation.getOperation().equals("其他")) {
                        if (timeDifference>otherOutTime)  sendMessage.setText("用户 @" + userStatus.getUsername() + " 的"+
                                userOperation.getOperation()+"操作已超时 "+otherOutTime+" 秒。已提醒:"+userOperation.getReminderCount()+"次");
                    }
                    // 超过 30 秒，发送消息
                    clockBot.sendMessage(sendMessage, sendMessage.getText());
                }
            }
        }
    }

}

