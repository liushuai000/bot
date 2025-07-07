package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.example.bot.accountBot.mapper.ConfigEditButtonMapper;
import org.example.bot.accountBot.mapper.ConfigEditMapper;
import org.example.bot.accountBot.mapper.UserMapper;
import org.example.bot.accountBot.pojo.ConfigEdit;
import org.example.bot.accountBot.pojo.ConfigEditButton;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ConfigEditHandler {
    @Autowired
    private AccountBot accountBot;
    @Autowired
    private ConfigEditMapper configEditMapper;
    @Autowired
    private ConfigEditButtonMapper configEditButtonMapper;
    @Autowired
    private MediaInfoConfig mediaInfoConfig;
    @Autowired
    private UserMapper userMapper;//监听完收款成功应该充值有效时长
    @Autowired
    private UserService userService;
    // 放在类顶部或单独封装成服务
    private static final Map<String, PaymentTask> paymentTasks = new ConcurrentHashMap<>();
    private static final Set<String> processedTransactions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    // 定时清理缓存（每小时一次）
    @Scheduled(fixedRate = 3600000)
    public void cleanUpProcessedTransactions() {
        processedTransactions.clear();
    }
    @Autowired
    private RestTemplateConfig restTemplateConfig;
    @Value("${tranHistoryUrl}")
    private String tranHistoryUrl;

    public static class PaymentTask {
        private String userId;
        private String address;
        private BigDecimal amount;
        private LocalDateTime startTime;
        private SendMessage confirmMessage;
        private Double month; // 新增字段
        public PaymentTask(String userId, String address, BigDecimal amount, SendMessage confirmMessage,Double month) {
            this.userId = userId;
            this.address = address;
            this.amount = amount;
            this.confirmMessage = confirmMessage;
            this.startTime = LocalDateTime.now();
            this.month = month;

        }

        public boolean isExpired() {
            return Duration.between(startTime, LocalDateTime.now()).toMinutes() > 10;
        }
    }

    public void sendButtonMessage(String buttonId, long userId, int messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        ConfigEdit configEdit = configEditMapper.selectOne(null);
        if (configEdit == null) {
            sendMessage.setText("未配置收款信息！请后台配置");
            accountBot.sendMessage(sendMessage);
            return;
        }

        ConfigEditButton configEditButton = configEditButtonMapper.selectOne(new QueryWrapper<ConfigEditButton>().eq("id", buttonId));
        StringBuilder stringBuilder = new StringBuilder();
        BigDecimal originalAmount = new BigDecimal(configEditButton.getLink());
        Random random = new Random();
        BigDecimal randomDecimal = new BigDecimal(random.nextInt(9999) + 1).divide(new BigDecimal(10000), 4, RoundingMode.HALF_UP);
        BigDecimal amount = originalAmount.add(randomDecimal);

        stringBuilder.append("收款地址：(点击地址可自动复制)\n" +
                "<code>" + configEdit.getPayText() + "</code>\n" +
                "\n" +
                "订单金额：" + amount + " USDT\n" +
                "续费时间：" + configEditButton.getMonth() + " 个月\n" +
                "\n" +
                "1．注意：请务必按指定金额转账，10分钟内支付有效。\n" +
                "2．转账成功10秒钟左右即可自动续费成功。\n" +
                "3．如遇到问题，请联系记账机器人售后客服: @" + configEdit.getAdminUserName());

        sendMessage.setText(stringBuilder.toString());
        sendMessage.disableWebPagePreview();

        // 保存支付任务
        String address = configEdit.getPayText();
        Double month = Double.valueOf(configEditButton.getMonth()); // 获取配置的月份

        paymentTasks.put(userId + ":" + address, new PaymentTask(String.valueOf(userId), address, amount, sendMessage,month));
        // 发送收款消息
        if (configEdit != null && configEdit.getPayImage() != null && StringUtils.isNotBlank(configEdit.getPayImage())) {
            mediaInfoConfig.sendSingleMediaFileIsDelete(String.valueOf(userId), sendMessage, configEdit.getPayImage(), "image/");
        } else {
            accountBot.sendMessage(sendMessage);
        }
    }

    @Scheduled(fixedRate = 30000) // 每 30 秒检查一次
    public void checkPayments() {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, PaymentTask> entry : paymentTasks.entrySet()) {
            String key = entry.getKey();
            PaymentTask task = entry.getValue();
            if (task.isExpired()) {
                toRemove.add(key);
                continue;
            }
            try {
                // 查询地址最新交易
                String url = tranHistoryUrl + task.address;
                List<TronHistoryDTO> historyTrading = restTemplateConfig.getForObjectHistoryTrading3(url, Map.class);
                if (historyTrading != null && !historyTrading.isEmpty()) {
                    for (TronHistoryDTO t : historyTrading) {
                        long blockTime = t.getBlock_ts();
                        String transactionKey = t.getTransaction_id() + task.address;
                        // 忽略旧交易（只看最近 60 秒内的）
                        if (System.currentTimeMillis() - blockTime > TimeUnit.SECONDS.toMillis(60)) {
                            continue;
                        }
                        // 判断是否是入账（收款地址为目标地址）
                        if (!t.getTo_address().equals(task.address)) {
                            continue;
                        }
                        if (processedTransactions.contains(transactionKey)) {
                            continue;
                        }
                        processedTransactions.add(transactionKey);
                        BigDecimal balance = new BigDecimal(t.getQuant());
                        BigDecimal tokenDecimal = BigDecimal.TEN.pow(t.getTokenInfo().getTokenDecimal());
                        BigDecimal receivedAmount = balance.divide(tokenDecimal, 8, RoundingMode.HALF_UP);
                        // 判断金额是否匹配（允许小额误差）
                        if (receivedAmount.compareTo(task.amount) == 0) {
                            // 执行支付成功逻辑
                            handlePaymentSuccess(task);
                            // 清除任务
                            toRemove.add(key);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("检查支付失败，地址: {}", task.address, e);
            }
        }
        // 清理过期任务
        toRemove.forEach(paymentTasks::remove);
    }
    private void handlePaymentSuccess(PaymentTask task) {
        log.info("检测到支付成功，用户ID: {}, 地址: {}", task.userId, task.address);
        User user = userService.findByUserId(task.userId);
        if (user != null) {
            LocalDateTime now = LocalDateTime.now();
            // ✅ 根据 month 判断是加天数还是月数
            LocalDateTime renewalTime;
            if (task.month == 0.5) {
                renewalTime = now.plusDays(15); // 半个月
            } else {
                renewalTime = now.plusMonths(task.month.longValue()); // 整数月
            }
            user.setValidTime(Date.from(renewalTime.atZone(ZoneId.systemDefault()).toInstant()));
            userService.updateUser(user);
        }
        // 发送支付成功消息
        SendMessage successMessage = new SendMessage();
        successMessage.setChatId(Long.parseLong(task.userId));
        String renewalText;
        if (task.month == 0.5) {
            renewalText = "✅ 支付成功！您的账户已续费 15 天。";
        } else {
            renewalText = String.format("✅ 支付成功！您的账户已续费 "+task.month+" 个月。");
        }
        successMessage.setText(renewalText);
        accountBot.sendMessage(successMessage);
    }


}
