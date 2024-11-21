package com.clock.bot.config;

import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;

import java.util.HashMap;
import java.util.Map;

public class OperationReminder {

    // 定义操作和对应的超时时间
    private static final Map<String, int[]> operationTimeouts = new HashMap<>();

    static {
        operationTimeouts.put("吃饭", new int[]{15, 25, 30});
        operationTimeouts.put("上厕所", new int[]{10, 15, 20});
        operationTimeouts.put("抽烟", new int[]{5, 10, 15});
        operationTimeouts.put("其它", new int[]{10, 20, 30});
    }

    public static String generateReminderMessage(UserOperation userOperation, UserStatus userStatus, long timeDifference) {
        String operation = userOperation.getOperation();
        int[] timeouts = operationTimeouts.get(operation);
        if (timeouts == null) {
            return "";
        }
        int reminderCount = userOperation.getReminderCount();
        for (int i = 0; i < timeouts.length; i++) {
            if (timeDifference > timeouts[i] * 60) { // 将分钟转换为秒
                if (reminderCount < i + 1) {
                    userOperation.setReminderCount(i + 1); // 更新提醒次数
                    return "用户 @" + userStatus.getUsername() + " 的" +
                            userOperation.getOperation() + "操作已超时 " + timeouts[i] + " 分钟, 如已完成请及时回座：/back 已提醒:" + (i + 1) + "次";
                }
            }
        }
        return "";
    }

}

