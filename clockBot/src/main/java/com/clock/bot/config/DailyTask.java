package com.clock.bot.config;

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

@Component
public class DailyTask {

    @Autowired
    private StatusService statusService;

    @Autowired
    private UserStatusService userStatusService;
    @Autowired
    private UserOperationService userOperationService;

    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜执行
    public void updateWorkDownTime() {
        List<Status> statuses = statusService.selcectStatusList();
        for (Status status : statuses) {
            Date currentCutOffTime = status.getCurrentCutOffTime();
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(currentCutOffTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())) {
                // 获取该群组下的所有用户状态
                List<UserStatus> userStatuses = userStatusService.findByGroupId(status.getGroupId());
                for (UserStatus userStatus : userStatuses) {
                    //如果 userOperationId 不为空，说明该用户正在做某项操作，强制结束
                    if (StringUtils.isNotBlank(userStatus.getUserOperationId())){
                        UserOperation byId = userOperationService.findById(userStatus.getUserOperationId());
                        if (byId.getEndTime()==null){
                            byId.setEndTime(byId.getEndTime());
                            userOperationService.updateUserOperation(byId);
                        }
                    }
                    userStatus.setStatus(false);//更新下班状态 和活动结束
                    userStatus.setReturnHome(false);
                    // 更新 workDownTime 为当前时间
                    userStatus.setWorkDownTime(new Date());
                    userStatusService.updateUserStatus(userStatus);
                }
            }
        }
    }
}

