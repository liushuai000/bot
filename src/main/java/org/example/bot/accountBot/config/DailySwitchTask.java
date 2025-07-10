package org.example.bot.accountBot.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.botConfig.AccountBot;
import org.example.bot.accountBot.botConfig.DateOperator;
import org.example.bot.accountBot.mapper.*;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DailySwitchTask {

    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private DateOperator dateOperator;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private IssueMapper issueMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountBot accountBot;
    DateUtils dateUtils=new DateUtils();
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;

    /**
     * 每天中午 12:00 执行
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void checkExpiringUsersAtNoon() {
        log.info("开始执行中午 12:00 的定时任务：检查有效期快到期的用户");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningTime = now.plusHours(24); // 提前24小时提醒
        List<User> expiringUsers = this.findExpiringUsersWithin24Hours(now, warningTime);

        if (expiringUsers.isEmpty()) {
            log.info("没有即将到期的用户");
        } else {
            log.info("找到 {} 个即将到期的用户", expiringUsers.size());
            sendExpirationWarning(expiringUsers, "中午提醒");
            // 2. 再为每个用户注册到期提醒定时器
            registerExpirationWarnings(expiringUsers);
        }
    }
    // 缓存已注册的用户ID，防止重复提醒
    private final Set<String> registeredUserIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void registerExpirationWarnings(List<User> users) {
        LocalDateTime now = LocalDateTime.now();
        for (User user : users) {
            String userId = user.getUserId();
            // 如果该用户已有提醒注册，跳过
            if (registeredUserIds.contains(userId)) {
                log.warn("用户 {} 已存在注册提醒，跳过", userId);
                continue;
            }
            Date validTime = user.getValidTime();
            if (validTime == null) continue;
            LocalDateTime expireTime = validTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            // 如果当前时间已经过了有效期，则不提醒
            if (now.isAfter(expireTime)) {
                log.warn("用户 {} 的有效期已过，跳过提醒", userId);
                continue;
            }
            long delayMillis = Duration.between(now, expireTime).toMillis();
            // 标记为已注册提醒
            registeredUserIds.add(userId);
            // 提交延迟任务
            taskScheduler.schedule(() -> {
                try {
                    User latestUser = userMapper.selectById(user.getId());
                    // 用户不存在或有效期已更新，不再提醒
                    if (latestUser == null || latestUser.getValidTime() == null ||
                            !latestUser.getValidTime().equals(validTime)) {
                        log.warn("用户 {} 的有效期已变更，跳过提醒", userId);
                        return;
                    }
                    // 发送“您的使用权限已到期”提醒消息
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(Long.parseLong(userId));
                    GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userId));
                    if (groupInfoSetting.getEnglish()){
                        sendMessage.setText(String.format("⚠️ 您的权限已到期！请续费以继续使用此功能！"));
                    }else {
                        sendMessage.setText(String.format("⚠️ Your usage permission has expired! Please renew to continue using this function!"));
                    }
                    accountBot.sendMessage(sendMessage);
                    log.info("已发送【使用权限已到期】提醒给用户：{}", userId);
                } catch (Exception e) {
                    log.error("延迟发送到期提醒失败，用户ID: {}", userId, e);
                } finally {
                    // 移除缓存中的任务
                    registeredUserIds.remove(userId);
                }
            }, Date.from(expireTime.atZone(ZoneId.systemDefault()).toInstant()));
        }
    }

    /**
     * 每天晚上 24:00 执行
     *
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiringUsersAtNight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningTime = now.plusHours(24);
        List<User> expiringUsers = this.findExpiringUsersWithin24Hours(now, warningTime);
        if (expiringUsers.isEmpty()) {
            log.info("没有即将到期的用户");
        } else {
            log.info("找到 {} 个即将到期的用户", expiringUsers.size());
            sendExpirationWarning(expiringUsers, "晚间提醒");
            // 2. 再为每个用户注册到期提醒定时器
            registerExpirationWarnings(expiringUsers);
        }
    }

    public List<User> findExpiringUsersWithin24Hours(LocalDateTime now, LocalDateTime warningTime) {
        return userMapper.selectList(new QueryWrapper<User>()
                .isNotNull("valid_time")
                .between("valid_time", Date.from(now.atZone(ZoneId.systemDefault()).toInstant()),
                        Date.from(warningTime.atZone(ZoneId.systemDefault()).toInstant())));
    }
    private void sendExpirationWarning(List<User> users, String triggerType) {
        for (User user : users) {
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(Long.parseLong(user.getUserId()));
                String formattedExpireTime = dateUtils.parseDate(user.getValidTime());
                GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", user.getUserId()));
                if (groupInfoSetting.getEnglish()){
                    sendMessage.setText(String.format("⚠️ 您的使用期限已不足24小时！\n到期时间：%s", formattedExpireTime));
                }else{
                    sendMessage.setText(String.format("⚠️ Your usage period is less than 24 hours! \nExpiration time: %s", formattedExpireTime));
                }
                accountBot.sendMessage(sendMessage);
                log.info("[{}] 已发送到期提醒给用户：{}", triggerType, user.getUserId());
            } catch (Exception e) {
                log.error("发送到期提醒失败，用户ID: {}", user.getUserId(), e);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 每天 00:00 执行
    public void executeDailyTasks() {
        log.info("开始执行每日定时任务：日切检查 + 数据清理");
        try {
            // 1. 执行日切检查
            List<Status> statuses = statusMapper.selectList(new QueryWrapper<Status>().eq("riqie", true));
            for (Status status : statuses) {
                try {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(status.getGroupId());
                    dateOperator.checkRiqie(sendMessage, status);
                } catch (Exception e) {
                    log.error("处理群组 {} 的日切失败: {}", status.getGroupId(), e.getMessage());
                }
            }
            // 2. 清理超过30天的 account 和 issue 数据
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            // 转换为 Date 类型用于数据库操作
            Date thresholdDate = Date.from(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant());
            // 删除 account 表中旧数据
            int deletedAccountCount =accountMapper.delete(new QueryWrapper<Account>().lt("add_time", thresholdDate));
            log.info("已删除 account 表中超过30天的数据，共 {} 条", deletedAccountCount);
            // 删除 issue 表中旧数据
            int deletedIssueCount = issueMapper.delete(new QueryWrapper<Issue>().lt("add_time", thresholdDate));
            log.info("已删除 issue 表中超过30天的数据，共 {} 条", deletedIssueCount);
        } catch (Exception e) {
            log.error("执行每日定时任务失败", e);
        }
    }




    /**
     * 每分钟检查一次所有开启了日切的群组
     */
    @Scheduled(cron = "0 * * * * ?") // 每小时执行一次
    public void checkAndExecuteDailySwitch() {
        log.info("开始执行每日日切检查任务");
        try {
            // 获取所有开启了日切的群组状态信息
            List<Status> statuses = statusMapper.selectList(new QueryWrapper<Status>().eq("riqie", true));
            for (Status status : statuses) {
                try {
                    // 构造一个空的 SendMessage 对象用于发送提示消息（可选）
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(status.getGroupId());
                    // 执行日切检查并触发更新
                    dateOperator.checkRiqie(sendMessage, status);
                } catch (Exception e) {
                    log.error("处理群组 {} 的日切失败: {}", status.getGroupId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("获取开启日切的群组失败", e);
        }
    }


}
