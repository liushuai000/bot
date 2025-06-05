package org.example.bot.accountBot.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.botConfig.DateOperator;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@Slf4j
public class DailySwitchTask {

    @Autowired
    private StatusMapper statusMapper;

    @Autowired
    private DateOperator dateOperator;

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
