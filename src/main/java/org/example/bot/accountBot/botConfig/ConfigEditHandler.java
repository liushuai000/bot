package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.example.bot.accountBot.mapper.*;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.UserService;
import org.example.bot.accountBot.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    private UserService userService;
    @Autowired
    private UserOrderMapper userOrderMapper;
    DateUtils dateUtils=new DateUtils();
    // 放在类顶部或单独封装成服务
    private static final Map<String, PaymentTask> paymentTasks = new ConcurrentHashMap<>();
    private static final Set<String> processedTransactions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @Autowired
    private ButtonList buttonList;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);

    // 定时清理缓存（每小时一次）
    @Scheduled(fixedRate = 3600000)
    public void cleanUpProcessedTransactions() {
        processedTransactions.clear();
    }
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;
    @Autowired
    private RestTemplateConfig restTemplateConfig;
    @Value("${tranHistoryUrl}")
    private String tranHistoryUrl;
    private String lastUsedAddress = ""; // 保存上次使用的地址

    public void checkAndUpdatePaymentAddress(String newAddress) {
        if (!newAddress.equals(lastUsedAddress)) {
            log.info("检测到收款地址变更，旧地址: {}, 新地址: {}", lastUsedAddress, newAddress);
            // 1. 清理当前所有未完成的 paymentTasks
            clearAllUnprocessedPayments();
            // 2. 将旧地址加入白名单继续监听一段时间（可选）
            addOldAddressToWatchList(lastUsedAddress);
            // 3. 更新全局地址
            lastUsedAddress = newAddress;
            // 4. 记录日志，便于后续排查
            log.info("已更新收款地址并清理旧缓存");
        }
    }
    private void clearAllUnprocessedPayments() {
        log.info("正在清理所有未完成的支付任务...");
        paymentTasks.clear();
    }
    private Set<String> oldAddressWhitelist = new HashSet<>();

    private void addOldAddressToWatchList(String address) {
        if (address != null && !address.isEmpty()) {
            oldAddressWhitelist.add(address);
            log.info("已将旧地址 {} 加入临时监听白名单", address);
            scheduler.schedule(() -> {
                oldAddressWhitelist.remove(address);
                log.info("旧地址 {} 已从监听白名单中移除", address);
            }, 10, TimeUnit.MINUTES);
        }
    }

    public static class PaymentTask {
        private String userId;
        private String address;
        private BigDecimal amount;
        private LocalDateTime startTime;
        private SendMessage confirmMessage;
        private Integer month; // 新增字段
        public PaymentTask(String userId, String address, BigDecimal amount, SendMessage confirmMessage,Integer month) {
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

    public void sendButtonMessage(String buttonId, long userId, GroupInfoSetting groupInfoSetting) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(userId);
        ConfigEdit configEdit = configEditMapper.selectOne(null);
        if (configEdit == null) {
            if (groupInfoSetting.getEnglish()){
                sendMessage.setText("未配置收款信息！请后台配置");
            }else {
                sendMessage.setText("Payment information is not configured! Please configure it in the background");
            }
            accountBot.sendMessage(sendMessage);
            return;
        }
        // ✅ 新增：检测地址是否变更
        String currentAddress = configEdit.getPayText();
        checkAndUpdatePaymentAddress(currentAddress); // 👈 触发地址变更检测逻辑
        ConfigEditButton configEditButton = configEditButtonMapper.selectOne(new QueryWrapper<ConfigEditButton>().eq("id", buttonId));
        StringBuilder stringBuilder = new StringBuilder();
        BigDecimal originalAmount = new BigDecimal(configEditButton.getLink());
        Random random = new Random();
        BigDecimal randomDecimal = new BigDecimal(random.nextInt(9999) + 1).divide(new BigDecimal(10000), 4, RoundingMode.HALF_UP);
        BigDecimal amount = originalAmount.add(randomDecimal);
        // 调用 createNewOrder 创建订单
        boolean newOrder = createNewOrder(String.valueOf(userId), amount, configEditButton.getMonth(), configEditButton.getText());
        if (newOrder) {
            return;
        }
        if (groupInfoSetting.getEnglish()){
            stringBuilder.append("收款地址：(点击地址可自动复制)\n" +
                    "<code>" + configEdit.getPayText() + "</code>\n" +
                    "\n" +
                    "订单金额：" + amount + " USDT\n" +
                    "续费时间：" + configEditButton.getMonth() + " 天\n" +
                    "\n" +
                    "1．注意：请务必按指定金额转账，10分钟内支付有效。\n" +
                    "2．转账成功10秒钟左右即可自动续费成功。\n" +
                    "3．如遇到问题，请联系记账机器人售后客服: @" + configEdit.getAdminUserName());
        }else{
            stringBuilder.append("Payment address: (Click the address to copy it automatically)\n" +
                    "<code>" + configEdit.getPayText() + "</code>\n" +
                    "\n" +
                    "Order amount：" + amount + " USDT\n" +
                    "Renewal time：" + configEditButton.getMonth() + " day\n" +
                    "\n" +
                    "1．Note: Please make sure to transfer the specified amount. Payment will be valid within 10 minutes。\n" +
                    "2．The transfer will be automatically renewed in about 10 seconds.。\n" +
                    "3．If you encounter any problems, please contact the accounting robot after-sales customer service: @" + configEdit.getAdminUserName());
        }
        sendMessage.setText(stringBuilder.toString());
        sendMessage.disableWebPagePreview();
        Map<String, String> map = new HashMap<>();
        map.put("取消订单(Cancellation of order)", "cancelOrder"+userId);
        buttonList.sendButton(sendMessage,String.valueOf(userId),map);
        // 保存支付任务
        String address = configEdit.getPayText();
        Integer month = Integer.valueOf(configEditButton.getMonth());
        paymentTasks.put(userId + ":" + address, new PaymentTask(String.valueOf(userId), address, amount, sendMessage, month));
        // 发送收款消息
        if (configEdit != null && configEdit.getPayImage() != null && StringUtils.isNotBlank(configEdit.getPayImage())) {
            mediaInfoConfig.sendSingleMediaFileIsDelete(String.valueOf(userId), sendMessage, configEdit.getPayImage(), "image/");
        } else {
            accountBot.sendMessage(sendMessage);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void checkPayments() {
        List<String> toRemove = new ArrayList<>();
        ConfigEdit configEdit = configEditMapper.selectOne(null);
        if (configEdit == null || configEdit.getPayText() == null) return;
        Set<String> addressesToCheck = new HashSet<>();
        addressesToCheck.add(configEdit.getPayText());
        addressesToCheck.addAll(oldAddressWhitelist);
        log.info("正在监听以下地址: {}", addressesToCheck);
        for (Map.Entry<String, PaymentTask> entry : paymentTasks.entrySet()) {
            String key = entry.getKey();
            PaymentTask task = entry.getValue();
            if (task.isExpired()) {
                toRemove.add(key);
                continue;
            }
            try {
                for (String address : addressesToCheck) {
                    String url = tranHistoryUrl + address;
                    List<TronHistoryDTO> historyTrading = restTemplateConfig.getForObjectHistoryTrading3(url, Map.class);
                    if (historyTrading != null && !historyTrading.isEmpty()) {
                        for (TronHistoryDTO t : historyTrading) {
                            long blockTime = t.getBlock_ts();
                            String transactionKey = t.getTransaction_id() + t.getTo_address();
                            if (System.currentTimeMillis() - blockTime > TimeUnit.SECONDS.toMillis(60)) continue;
                            if (!t.getTo_address().equals(configEdit.getPayText()) && !oldAddressWhitelist.contains(t.getTo_address())) {
                                continue;
                            }
                            if (processedTransactions.contains(transactionKey)) continue;
                            processedTransactions.add(transactionKey);
                            BigDecimal balance = new BigDecimal(t.getQuant());
                            BigDecimal tokenDecimal = BigDecimal.TEN.pow(t.getTokenInfo().getTokenDecimal());
                            BigDecimal receivedAmount = balance.divide(tokenDecimal, 8, RoundingMode.HALF_UP);
                            if (receivedAmount.compareTo(task.amount) == 0 &&
                                    t.getTo_address().equals(task.address)) {

                                handlePaymentSuccess(task);
                                toRemove.add(key);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("检查支付失败，地址: {}", task.address, e);
            }
        }
        toRemove.forEach(paymentTasks::remove);
    }

    private void handlePaymentSuccess(PaymentTask task) {
        log.info("检测到支付成功，用户ID: {}, 地址: {}", task.userId, task.address);
        User user = userService.findByUserId(task.userId);
        if (user != null) {
            user.setValidTime(dateUtils.calculateRenewalDate(user.getValidTime(), task.month.longValue(), ZoneId.systemDefault()));
            userService.updateUser(user);
        }
        // 更新订单状态为“已购买”
        QueryWrapper<UserOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", task.userId)
                .eq("type", UserOrder.STATUS_WGM)
                .orderByDesc("create_time")
                .last("LIMIT 1");
        UserOrder unfinishedOrder = userOrderMapper.selectOne(queryWrapper);
        if (unfinishedOrder != null) {
            unfinishedOrder.setType(UserOrder.STATUS_YIGOUMAI);
            userOrderMapper.updateById(unfinishedOrder);
        }
        // 发送支付成功消息
        SendMessage successMessage = new SendMessage();
        successMessage.setChatId(Long.parseLong(task.userId));
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id",task.userId));
        if (groupInfoSetting.getEnglish()){
            successMessage.setText(String.format("✅ 支付成功！您的账户已续费 "+task.month+" 天。\n到期时间为:<b>"+dateUtils.parseDate(user.getValidTime()) )+"</b>");
        }else{
            successMessage.setText(String.format("✅ Payment successful! Your account has been renewed "+task.month+" day。\nExpiration time is:<b>"+dateUtils.parseDate(user.getValidTime()))+"</b>");
        }
        accountBot.sendMessage(successMessage);
    }
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanUpExpiredOrders() {
        List<UserOrder> expiredOrders = userOrderMapper.selectList(new QueryWrapper<UserOrder>()
                .eq("type", UserOrder.STATUS_WGM)
                .lt("create_time", new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10))));
        for (UserOrder order : expiredOrders) {
            order.setType(UserOrder.STATUS_YGP);
            userOrderMapper.updateById(order);
        }
    }
    @Transactional
    public void cancelOrder(String userId) {
        log.info("用户 {} 请求取消订单", userId);
        // 查询用户当前未完成订单（status = "未购买"）
        QueryWrapper<UserOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("type", UserOrder.STATUS_WGM)
                .orderByDesc("create_time")
                .last("LIMIT 1");
        UserOrder order = userOrderMapper.selectOne(queryWrapper);
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id",userId));
        if (order != null) {
            // 更新订单状态为“已取消”
            order.setType(UserOrder.STATUS_YQX);
            userOrderMapper.updateById(order);
            // 删除支付任务
            String address = getCurrentAddressByUserId(userId); // 获取地址，见下文
            paymentTasks.remove(userId + ":" + address);
            // 发送取消成功消息
            SendMessage message = new SendMessage();
            message.setChatId(Long.parseLong(userId));
            if (groupInfoSetting.getEnglish()){
                message.setText("✅ 订单已取消!");
            }else{
                message.setText("✅ Order Cancelled!");
            }
            accountBot.sendMessage(message);
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(Long.parseLong(userId));
            if (groupInfoSetting.getEnglish()){
                message.setText("⚠️ 当前没有可取消的订单。");
            }else{
                message.setText("⚠️ There are currently no orders to cancel。");
            }
            accountBot.sendMessage(message);
        }
    }
    private String getCurrentAddressByUserId(String userId) {
        for (Map.Entry<String, PaymentTask> entry : paymentTasks.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                return entry.getValue().address;
            }
        }
        return null;
    }

    public boolean createNewOrder(String userId, BigDecimal amount, String month, String buttonName) {
        QueryWrapper<UserOrder> userOrderQueryWrapper = new QueryWrapper<>();
        userOrderQueryWrapper.eq("user_id", userId)
                .eq("type", UserOrder.STATUS_WGM)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        UserOrder existingOrder = userOrderMapper.selectOne(userOrderQueryWrapper);
        if (existingOrder != null) {
            LocalDateTime createTime = existingOrder.getCreateTime().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id",userId));
            if (Duration.between(createTime, LocalDateTime.now()).toMinutes() < 10) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(Long.parseLong(userId));
                if (groupInfoSetting.getEnglish()){
                    // 订单仍为“未购买”，说明未支付
                    sendMessage.setText( "❌你已经有未完成的订单，请先完成或取消。");
                }else {
                    sendMessage.setText( "❌You already have an unfinished order, please complete or cancel it first。");
                }
                accountBot.sendMessage(sendMessage);
                return true;
            } else {
                existingOrder.setType("已过期");
                userOrderMapper.updateById(existingOrder);
            }
        }

        String orderNumber = dateUtils.generateOrderNumber();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(Double.valueOf(month).longValue());
        UserOrder newOrder = new UserOrder()
                .setUserId(userId)
                .setAmount(amount)
                .setType(UserOrder.STATUS_WGM)
                .setConfigEditButtonName(buttonName)
                .setMonth(month)
                .setOrderNumber(orderNumber)
                .setCreateTime(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setEndTime(Date.from(endTime.atZone(ZoneId.systemDefault()).plusMinutes(10).toInstant()));
        userOrderMapper.insert(newOrder);

        // 添加定时任务：10分钟后检查是否已支付
        scheduleOrderTimeoutCheck(newOrder);

        return false;
    }
    private void scheduleOrderTimeoutCheck(UserOrder order) {
        String orderNumber = order.getOrderNumber();
        String userId = order.getUserId();
        scheduler.schedule(() -> checkOrderPaymentStatus(orderNumber, userId), 10, TimeUnit.MINUTES);
    }

    private void checkOrderPaymentStatus(String orderNumber, String userId) {
        UserOrder order = userOrderMapper.selectOne(new QueryWrapper<UserOrder>().eq("order_number", orderNumber));
        if (order != null && UserOrder.STATUS_WGM.equals(order.getType())) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(Long.parseLong(userId));
            GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id",userId));
            if (groupInfoSetting.getEnglish()){
                // 订单仍为“未购买”，说明未支付
                sendMessage.setText( "❌续费超时，如有需要请重新生成订单。");
            }else{
                // 订单仍为“未购买”，说明未支付
                sendMessage.setText( "❌Renewal timeout, please regenerate order if necessary。");
            }
            accountBot.sendMessage(sendMessage);
            // 可选：更新订单状态为“已过期”
            order.setType("已过期");
            userOrderMapper.updateById(order);
        }
    }


}
