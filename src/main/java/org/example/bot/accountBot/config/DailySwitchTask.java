package org.example.bot.accountBot.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.botConfig.DateOperator;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.mapper.IssueMapper;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
