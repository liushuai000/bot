package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.TronAccountDTO;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.*;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.UserNormalService;
import org.example.bot.accountBot.service.UserService;
import org.example.bot.accountBot.service.WalletListenerService;
import org.example.bot.accountBot.utils.DateUtils;
import org.example.bot.accountBot.utils.StyleText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 此类设置单人机器人聊天消息
 */
@Slf4j
@Service
public class PaperPlaneBotSinglePerson {
    @Resource
    AccountBot  accountBot;
    @Autowired
    UserService userService;
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    UserNormalService userNormalService;
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;
    @Autowired
    private ConfigEditMapper configEditMapper;
    @Autowired
    private ConfigEditButtonMapper configEditButtonMapper;
    @Autowired
    WalletListenerService walletListenerService;
    @Autowired
    private ButtonList buttonList;
    @Autowired
    private StatusMapper statusMapper;
    @Value("${tranAccountUrl}")
    protected String tranAccountUrl;//查询账户余额
    @Value("${tranHistoryUrl}")
    protected String tranHistoryUrl;//查询账户历史交易
    @Value("${vueUrl}")
    protected String vueUrl;
    @Resource
    RestTemplateConfig restTemplateConfig;
    // 创建 SimpleDateFormat 对象，指定日期格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 定时任务调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);
    @Qualifier("userOperationService")
    // 记录已处理过的交易 ID + 地址组合
    private final Set<String> processedTransactions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @Autowired
    private AccountSettingMapper accountSettingMapper;

    @SneakyThrows
    @PostConstruct
    public void init() {
        // 启动定时任务，首次立即执行，之后每隔30秒执行一次
        scheduler.scheduleAtFixedRate(this::fetchAndCacheData, 0, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::cleanUpCache, 0, 1, TimeUnit.HOURS);
    }
    private void cleanUpCache() {
        processedTransactions.clear(); // 清理缓存，释放内存
    }
    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
    }
    private void fetchAndCacheData() {
        List<WalletListener> walletListeners = walletListenerService.queryAll(); // 获取所有监听地址
        // 按地址分组：String 是地址，List<WalletListener> 是所有监听该地址的用户
        Map<String, List<WalletListener>> addressToListeners = walletListeners.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(WalletListener::getAddress));
        // 并行处理每个地址
        addressToListeners.forEach((address, listeners) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    // 控制请求频率
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String url = tranHistoryUrl + address;
                List<TronHistoryDTO> historyTrading = restTemplateConfig.getForObjectHistoryTrading2(url, Map.class);
                if (historyTrading == null || historyTrading.isEmpty()) {
                    return;
                }
                long now = System.currentTimeMillis();
                // 处理每笔交易
                historyTrading.stream()
                        .filter(Objects::nonNull)
                        .forEach(t -> {
                            long blockTime = t.getBlock_ts();
                            String transactionKey = t.getTransaction_id() + address;
                            // 判断是否是最近60秒内的新交易，并且未被处理过
                            if (now - blockTime <= TimeUnit.SECONDS.toMillis(60) && !processedTransactions.contains(transactionKey)) {
                                processedTransactions.add(transactionKey); // 标记为已处理
                                // 构建消息内容
                                String result = buildTransactionMessage(t, address);
                                String resultEnglish = buildTransactionMessageEnglish(t, address);
                                // 给所有监听这个地址的用户发送消息
                                listeners.forEach(listener -> {
                                    try {
                                        SendMessage sendMessage = new SendMessage();
                                        sendMessage.setChatId(listener.getUserId());
                                        sendMessage.disableWebPagePreview();
                                        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", listener.getUserId()));
                                        if (groupInfoSetting.getEnglish()){
                                            accountBot.sendMessage(sendMessage, result);
                                        }else{
                                            accountBot.sendMessage(sendMessage, resultEnglish);
                                        }
                                    } catch (Exception e) {
                                        log.error("发送消息失败给用户 {}: {}", listener.getUserId(), e.getMessage());
                                        // 可选：删除异常用户监听器
                                        // walletListenerService.deleteWalletListener(listener);
                                    }
                                });
                            }
                        });
            }, scheduler); // 使用线程池执行
        });
    }

    private String buildTransactionMessage(TronHistoryDTO t, String address) {
        BigDecimal balance = new BigDecimal(t.getQuant());
        BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(t.getTokenInfo().getTokenDecimal()));
        String type = t.getTo_address().equals(address) ? "入账" : "出账";

        StringBuilder sb = new StringBuilder();
        sb.append("交易金额： ").append(bigDecimal).append(t.getTokenInfo().getTokenAbbr()).append(" 已确认 #").append(type).append("\n")
                .append("交易币种： ❇\uFE0F #").append(t.getTokenInfo().getTokenAbbr()).append("\n")
                .append("收款地址： <code>").append(t.getTo_address()).append("</code>\n")
                .append("支付地址： <code>").append(t.getFrom_address()).append("</code>\n")
                .append("交易哈希： ").append(t.getTransaction_id())
                .append(" (https://tronscan.org/#/transaction/").append(t.getTransaction_id()).append(")\n")
                .append("转账时间：").append(sdf.format(new Date(t.getBlock_ts()))).append("\n")
                .append("\uD83D\uDCE3 监控地址 (").append(address).append(")");

        return sb.toString();
    }
    private String buildTransactionMessageEnglish(TronHistoryDTO t, String address) {
        BigDecimal balance = new BigDecimal(t.getQuant());
        BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(t.getTokenInfo().getTokenDecimal()));
        String type = t.getTo_address().equals(address) ? "Entry" : "Out";
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction amount： ").append(bigDecimal).append(t.getTokenInfo().getTokenAbbr()).append(" Confirmed #").append(type).append("\n")
                .append("Trading Currency： ❇\uFE0F #").append(t.getTokenInfo().getTokenAbbr()).append("\n")
                .append("Collection address： <code>").append(t.getTo_address()).append("</code>\n")
                .append("Payment address: <code>").append(t.getFrom_address()).append("</code>\n")
                .append("Transaction hash： ").append(t.getTransaction_id())
                .append(" (https://tronscan.org/#/transaction/").append(t.getTransaction_id()).append(")\n")
                .append("Transfer time：").append(sdf.format(new Date(t.getBlock_ts()))).append("\n")
                .append("\uD83D\uDCE3 monitoring address (").append(address).append(")");

        return sb.toString();
    }
    public void switchEn(String text,Long chatId,GroupInfoSetting groupInfoSetting){
        if (text.equals("切换中文") || text.equals("切换英文") ||  text.equals("switch to chinese")||  text.equals("switch to english")){
            if (text.equals("切换中文") || text.equals("switch to chinese")){
                groupInfoSetting.setEnglish(true);
            } else if (text.equals("切换英文") || text.equals("switch to english")) {
                groupInfoSetting.setEnglish(false);
            }else {
                groupInfoSetting.setEnglish(true);
            }
            groupInfoSettingMapper.updateById(groupInfoSetting);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("切换成功"+("Switching successful"));
            accountBot.sendMessage(sendMessage);
        }
    }
    //获取个人账户信息
    protected void handleTronAccountMessage(SendMessage sendMessage, Update update,UserDTO userDTO){
        String text = userDTO.getText();
        if (text==null){
            return;
        }
        AccountSetting accountSetting = accountSettingMapper.selectOne(new QueryWrapper<>());
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getUserId()));
        if (groupInfoSetting==null){
            groupInfoSetting=new GroupInfoSetting();
            groupInfoSetting.setGroupId(Long.valueOf(userDTO.getUserId()));
            groupInfoSetting.setEnglish(accountSetting.getPrivateMessageLanguage());
            groupInfoSettingMapper.insert(groupInfoSetting);
        }else {
            groupInfoSetting.setEnglish(accountSetting.getPrivateMessageLanguage());
            groupInfoSettingMapper.updateById(groupInfoSetting);
        }
        if (userDTO.getText().equals("切换中文") || userDTO.getText().equals("切换英文")
                ||  userDTO.getText().toLowerCase().equals("switch to chinese")||  userDTO.getText().toLowerCase().equals("switch to english")){
            this.switchEn(userDTO.getText().toLowerCase(), Long.valueOf(userDTO.getUserId()),groupInfoSetting);
            return;
        }
        // 检查是否符合标准
        if (text.length() != 34) {
            return;
        }
        String userId = userDTO.getUserId();
        String url = tranAccountUrl+text;
        TronAccountDTO tronAccount = restTemplateConfig.getForObjectTronAccount(url);
        String htmlText ;
        String trxText;
        if (groupInfoSetting.getEnglish()){
            htmlText = "[USDT余额](https://tronscan.org/#/address/"+ text + "/transfers)";
            trxText = "[TRX余额](https://tronscan.org/#/address/"+ text + "/transfers)";
        }else{
            htmlText = "[USDT Balance](https://tronscan.org/#/address/"+ text + "/transfers)";
            trxText = "[TRX Balance](https://tronscan.org/#/address/"+ text + "/transfers)";
        }
        AtomicReference<BigDecimal> bigDecimal= new AtomicReference<>();
        AtomicReference<String> trxBigDecimal= new AtomicReference<>();
        if (tronAccount.getWithPriceTokens()!=null)
            tronAccount.getWithPriceTokens().stream().filter(Objects::nonNull).forEach(t -> {
                if (t.getTokenAbbr().equals("USDT")){
                    BigDecimal balance = new BigDecimal(tronAccount.getWithPriceTokens().get(1).getBalance());
                    // 计算移动小数点后的 balance
                    bigDecimal.set(balance.divide(BigDecimal.TEN.pow(tronAccount.getWithPriceTokens().get(1).getTokenDecimal())));
                }else if (t.getTokenAbbr().equals("trx")){
                    trxBigDecimal.set(t.getAmount());
                }
            });
        String usdt="";
        String trx="";
        if (bigDecimal.get()!=null){
            usdt="\uD83D\uDCB0 "+htmlText+":"+bigDecimal+" U\n";
        }else {
            usdt="\uD83D\uDCB0 "+htmlText+": 0 U\n";
        }
        if (trxBigDecimal.get()!=null){
            trx="\uD83D\uDCB0 "+trxText+":"+trxBigDecimal+" TRX\n";
        }else {
            trx ="\uD83D\uDCB0 "+trxText+": 0 TRX\n";
        }
        String result;
        if (groupInfoSetting.getEnglish()){
            result="✅✅✅✅\n" +
                    text+"\n" +
                    "——————————\n" + usdt+ trx+
                    "——————————\n" +
                    "交易次数：" +tronAccount.getTransactions()+"\n"+
                    "   -出："+tronAccount.getTransactions_out()+"\n"+
                    "   -入："+tronAccount.getTransactions_in()+"\n"+
                    "地址创建时间："+sdf.format(new Date(tronAccount.getDate_created()))+"\n"+
                    "最新交易时间："+sdf.format(new Date(tronAccount.getLatest_operation_time()))+"\n";
        }else{
            result="✅✅✅✅\n" +
                    text+"\n" +
                    "——————————\n" + usdt+ trx+
                    "——————————\n" +
                    "Number of transactions：" +tronAccount.getTransactions()+"\n"+
                    "   -Out："+tronAccount.getTransactions_out()+"\n"+
                    "   -Enter："+tronAccount.getTransactions_in()+"\n"+
                    "Address creation time："+sdf.format(new Date(tronAccount.getDate_created()))+"\n"+
                    "Latest trading hours："+sdf.format(new Date(tronAccount.getLatest_operation_time()))+"\n";
        }
        ButtonList buttonList = new ButtonList();
        Map<String,String> buttonTextMap=new HashMap<>();
        if (groupInfoSetting.getEnglish()){
            buttonTextMap.put("监听该地址","监听该地址");
            buttonTextMap.put("查询交易记录","查询交易记录");
        }else{
            buttonTextMap.put("Listen to this address","监听该地址");
            buttonTextMap.put("Query transaction records","查询交易记录");
        }
        buttonList.sendButton(sendMessage, String.valueOf(userId),buttonTextMap);
        accountBot.tronAccountMessageText(sendMessage,userId,result);
    }
    public static final String STATE_BROADCAST = "broadcast";
    public static final String ADMIN_STATE_BROADCAST = "admin_broadcast";
    public static final String STATE_NORMAL = "normal";
    // 广播状态
    private static final Map<String, String> userStates = new ConcurrentHashMap<>();
    @Autowired
    private UserNormalMapper userNormalMapper;
    //设置机器人在群组内的有效时间 默认免费使用日期6小时. 机器人底部按钮 获取个人信息 获取最新用户名 获取个人id 使用日期;
    protected void handleNonGroupMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        if (userDTO.getText()==null){
            return;
        }
        String text = userDTO.getText().toLowerCase();
        //授权-123456789-30  用户id -30
        PaperPlaneBotButton buttonList = new PaperPlaneBotButton();
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getUserId()));
        ConfigEdit configEdit = configEditMapper.selectOne(new QueryWrapper<>());
        User byUserId = userService.findByUserId(userDTO.getUserId());
        ReplyKeyboardMarkup replyKeyboardMarkup = buttonList.sendReplyKeyboard(configEdit,byUserId);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);//是否在onUpdateReceived设置
        String regex = "^授权-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
        String regexEn = "^authorization-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
        String regexDelete = "^删除授权-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";    //删除授权-123456789-30
        String regexEnDelete = "^authorization-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";//只支持中文就可以
        String[] split3 = text.split("-");
        if (text.equals("获取个人信息（personal information）")){
            this.getUserInfoMessage(message,sendMessage,userDTO);
            return;
        }else if (text.equals("监听列表（listening address）")){
            this.getListening(message,sendMessage,userDTO);
            return;
        }else if (text.equals("使用说明（illustrate）")){
            this.useInfo(message,sendMessage,userDTO);
            return;
        }else if (text.equals("群发广播（group broadcast）")){
            if (byUserId.getValidTime()==null || byUserId.getValidTime().getTime() <= System.currentTimeMillis()){
                if (groupInfoSetting.getEnglish()){
                    sendMessage.setText("你的使用权限已到期! 请续费使用此功能!");
                }else{
                    sendMessage.setText("Your usage rights have expired! Please renew to use this feature!");
                }
                accountBot.sendMessage(sendMessage);
                return;
            }
            userStates.put(userDTO.getUserId(), STATE_BROADCAST);
            if (groupInfoSetting.getEnglish()){
                sendMessage.setText("\uD83D\uDCE1 群发广播：\n" +
                        "\n" +
                        "仅发送自己邀请的群组，如需发送所有群，请在后台操作。\n" +
                        "支持纯文本、图片+文字、视频+文字。\n" +
                        "\n" +
                        "请输入您要广播的内容:");
            }else {
                sendMessage.setText("\uD83D\uDCE1 Group Broadcast：\n" +
                        "\n" +
                        "Only send the group you invite, if you need to send all groups, please operate in the background.\n" +
                        "Support pure text, image + text, video + text.\n" +
                        "\n" +
                        "Please enter the content you want to broadcast:");
            }
            accountBot.sendMessage(sendMessage);
            return;
        }else if (text.equals("超级管理员广播（super management broadcast）")){
            if (!byUserId.isCjgl()){
                return;
            }
            userStates.put(userDTO.getUserId(), ADMIN_STATE_BROADCAST);
            if (groupInfoSetting.getEnglish()){
                sendMessage.setText("\uD83D\uDCE1 管理群发广播：\n" +
                        "\n" +
                        "仅发送自己邀请的群组，如需发送所有群，请在后台操作。\n" +
                        "支持纯文本、图片+文字、视频+文字。\n" +
                        "\n" +
                        "请输入您要广播的内容:");
            }else {
                sendMessage.setText("\uD83D\uDCE1 Manage Group Broadcast：\n" +
                        "\n" +
                        "Only send the group you invite, if you need to send all groups, please operate in the background.\n" +
                        "Support pure text, image + text, video + text.\n" +
                        "\n" +
                        "Please enter the content you want to broadcast:");
            }
            accountBot.sendMessage(sendMessage);
            return;
        }else if (text.equals("自助续费（self-service renewal）")){
            this.renewal(message,sendMessage,userDTO);
            return;
        }else if (text.contains("##") &&text.length()>=37){
            // 使用 split 方法分割字符串
            String[] parts = text.split("##");
            String messageResult="";
            if (parts.length == 2) {
                String address = parts[0];
                String nickName = parts[1];
                //只有长度大于37才进行修改
                walletListenerService.updateWalletListener(userDTO.getUserId() + "", address, nickName);
                if (groupInfoSetting.getEnglish()){
                    messageResult="修改成功";
                }else{
                    messageResult="Modification successful";
                }
            } else {
                if (groupInfoSetting.getEnglish()){
                    messageResult="字符串格式不正确，没有找到 ## 或者 ## 数量不对";
                }else{
                    messageResult="The string format is incorrect, no ## is found or the number of ## is incorrect";
                }

            }
            accountBot.sendMessage(sendMessage,messageResult);
            return;
        }
        if (text.startsWith("授权")){
            if (text.startsWith("授权-") && !byUserId.isCjgl()){
                accountBot.sendMessage(sendMessage,"您不是超级管理!无权限设置管理员!");
                return;
            }
            boolean matches = text.matches(regex);
            String validTimeText = "";
            String userId=split3[1];
            User user=userService.findByUserId(userId);
            if (matches) {
                validTimeText=split3[2];
            }else {
                accountBot.sendMessage(sendMessage,"格式不匹配!");
                return;
            }
            DateUtils dateUtils = new DateUtils();
            Date validTime = dateUtils.calculateRenewalDate(user.getValidTime(), Long.parseLong(validTimeText), ZoneId.systemDefault());
            if (user == null) {
                user = new User();
                user.setUserId(userId);
                user.setUsername(userDTO.getUsername());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setCreateTime(new Date());
                user.setSuperAdmin(true);//是管理员
                user.setValidFree(true);
                user.setValidTime(validTime);
                userService.insertUser(user);
            }else {
                user.setSuperAdmin(true);//默认操作权限管理员
                user.setValidTime(validTime);
                user.setValidFree(true);
                userService.updateUserValidTime(user,validTime);
            }
            accountBot.sendMessage(sendMessage,"用户ID: "+userId+" 有效期: <b>"+dateUtils.parseDate(validTime)+"</b>");
            return;
        } else if (text.startsWith("authorization")) {
            if (text.startsWith("authorization-")&& !byUserId.isCjgl()){
                accountBot.sendMessage(sendMessage,"You are not a super administrator! You do not have the authority to set up an administrator!");
                return;
            }
            boolean matches = text.matches(regexEn);
            String validTimeText = "";
            String userId=split3[1];
            User user=userService.findByUserId(userId);
            if (matches) {
                validTimeText=split3[2];
            }else {
                accountBot.sendMessage(sendMessage,"Format does not match!");
                return;
            }
            DateUtils dateUtils = new DateUtils();
            Date validTime = dateUtils.calculateRenewalDate(user.getValidTime(), Long.parseLong(validTimeText), ZoneId.systemDefault());
            if (user == null) {
                user = new User();
                user.setUserId(userId);
                user.setUsername(userDTO.getUsername());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setCreateTime(new Date());
                user.setSuperAdmin(true);//是管理员
                user.setValidFree(true);
                user.setValidTime(validTime);
                userService.insertUser(user);
            }else {
                user.setSuperAdmin(true);//默认操作权限管理员
                user.setValidTime(validTime);
                user.setValidFree(true);
                userService.updateUserValidTime(user,validTime);
            }
            accountBot.sendMessage(sendMessage,"User ID: "+userId+" Validity:<b>"+dateUtils.parseDate(validTime)+"</b>");
            return;
        }else if (text.startsWith("删除授权")) {
            if (text.startsWith("删除授权-") && !byUserId.isCjgl()) {
                accountBot.sendMessage(sendMessage, "您不是超级管理!无权限设置管理员!");
                return;
            }
            boolean matches = text.matches(regexDelete);
            String deductDaysText = "";
            String userId = split3[1];
            if (matches) {
                deductDaysText = split3[2];
            } else {
                accountBot.sendMessage(sendMessage, "格式不匹配!");
                return;
            }
            int deductDays = Integer.parseInt(deductDaysText);
            User user = userService.findByUserId(userId);
            if (user == null) {
                accountBot.sendMessage(sendMessage, "用户不存在!");
                return;
            }
            // 获取用户原有的有效期时间
            LocalDateTime originalValidTime = user.getValidTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            // 从原有有效时间减去指定天数
            LocalDateTime newValidTime = originalValidTime.minusDays(deductDays);
            // 如果计算后的时间小于当前时间，则设置为当前时间（不允许负值）
            if (newValidTime.isBefore(LocalDateTime.now())) {
                newValidTime = LocalDateTime.now();
            }
            Date newValidDate = Date.from(newValidTime.atZone(ZoneId.systemDefault()).toInstant());
            // 更新用户的有效期
            user.setValidTime(newValidDate);
            // 统一格式的日期时间字符串
            String formattedDateTime = newValidTime.getYear() + "年" +
                    newValidTime.getMonthValue() + "月" +
                    newValidTime.getDayOfMonth() + "日" +
                    newValidTime.getHour() + "时" +
                    newValidTime.getMinute() + "分" +
                    newValidTime.getSecond() + "秒";
            // 只是减少有效期但仍有授权
            userService.updateUserValidTime(user, newValidDate);
            // 无论是否过期都使用相同的返回格式
            accountBot.sendMessage(sendMessage, "用户ID: " + userId + " 有效期:" + formattedDateTime);
            return;
        }
        if (text.equals("/start")) {
            StyleText styleText=new StyleText();
            AccountSetting accountSetting = accountSettingMapper.selectOne(new QueryWrapper<>());
            if (groupInfoSetting.getEnglish()){
                if (accountSetting!=null && accountSetting.getStartMessageNoticeSwitch()){
                    accountBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),styleText.cleanHtmlExceptSpecificTags(accountSetting.getStartMessageNotice()));
                }
            }else{
                if (accountSetting!=null && accountSetting.getStartMessageNoticeSwitch()){
                    accountBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),styleText.cleanHtmlExceptSpecificTags(accountSetting.getEnglishStartMessageNotice()));
                }
            }
        }
    }

    private void renewal(Message message, SendMessage sendMessage, UserDTO userDTO) {
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getUserId()));
        User user = userService.findByUserId(userDTO.getUserId());
        String msg;
        if (groupInfoSetting.getEnglish()){
            msg="自助续费机器人\n" +
                    "\n" +
                    "记账机器人使用有效期截至：";
        }else{
            msg="Self-service renewal robot\n" +
                    "\n" +
                    "The validity period of the accounting robot ends：";
        }
        if (user==null || user.getValidTime()==null){
            msg+= new DateUtils().parseDate(user.getCreateTime());
        } else if (user.getValidTime()!=null) {
            msg+= new DateUtils().parseDate(user.getValidTime());
        }
        sendMessage.setText( msg);
        ConfigEdit configEdit = configEditMapper.selectOne(new QueryWrapper<>());
        if (configEdit!=null){
            List<ConfigEditButton> configEditButtonList = configEditButtonMapper.selectList(new QueryWrapper<ConfigEditButton>().eq("config_edit_id", configEdit.getId()));
            if (configEditButtonList!=null && !configEditButtonList.isEmpty()){
                buttonList.moneyButton(sendMessage,configEditButtonList);
            }
        }
        accountBot.sendMessage(sendMessage);
    }

    //使用说明
    private void useInfo(Message message, SendMessage sendMessage, UserDTO userDTO) {
        String msg="1\uFE0F⃣增加机器人进群。群右上角--Add member-输入 @"+this.username+"\n" +
                " Add robots to the group. In the upper right corner of the group--Add member-enter @"+this.username+"\n" +
                "\n" +
                " 2\uFE0F⃣ 设置操作人 @AAA\n" +
                " Set operator @AAA\n" +
                "删除操作人 @AAA\n" +
                "Delete operator @AAA\n" +
                "先打空格在打@，会弹出选择更方便\n" +
                "Type space first and then @, a pop-up will appear for easier selection\n" +
                "显示操作人（Show operator）\n" +
                "\n" +
                "  如果对方没有设置用户名，可以回复对方信息并发送“设置操作员”或“删除操作员”来新增或删除操作员\n" +
                "If the other party has not set a user name, you can reply to the other party's message and send \"Set operator\" or \"Delete operator\" to add or delete an operator.\n" +
                "\n" +
                "3\uFE0F⃣输入”设置费率X.X%“\n" +
                "Enter \"set rate X.X%\"\n" +
                "输入”设置汇率X.X%“\n" +
                "Enter \"Set exchange rate  X.X%\"\n" +
                "\n" +
                " 4\uFE0F⃣ 入款指令；+     修正；D-\n" +
                "Deposit instruction; + correction; D-\n" +
                " 下发指令；-       修正；T-\n" +
                "Withdrawal instruction;- Amendment;T-\n" +
                "\n" +
                " 如果输入错误，可以用 入款-XXX  或 下发-XXX，来修正\n" +
                "If you make an input error, you can use Deposit-XXX or Issue-XXX to correct it.\n" +
                "“撤销入款”：可撤销最近一条入款记录\n" +
                "“Cancel deposit”: You can cancel the most recent deposit record\n" +
                "“撤销下发”：可撤销最近一条下发记录\n" +
                "“Undo delivery”: You can revoke the most recent delivery record.\n" +
                "\n" +
                " 5\uFE0F⃣“OTC”, “币价”，“Z0”,即可查询OKex官方最新OTC交易价格,底部可查询微信支付宝和银行卡价格.\n" +
                "\"OTC\", \"Coin Price\" and \"Z0\" can view the latest OTC transaction prices on OKex. At the bottom, you can view the prices of WeChat, Alipay and bank cards.\n" +
                " 6\uFE0F⃣账单（bill）\n" +
                "删除账单（删除今日账单）\n" +
                "Delete bill (delete today's bill)\n" +
                "删除全部帐单（删除历史账单谨慎使用）\n" +
                "Delete all bills (be careful when deleting historical bills)\n" +
                "\n" +
                " 7\uFE0F⃣计算器 （calculator）\n" +
                "100+100            1000*5\n" +
                "1000/5               1000-5\n" +
                "8\uFE0F⃣TRC20地址信息查询（TRC20 address information query）\n" +
                "查询TEtYFNQCaDWZXsjoMwxxJ9tymhXx*****\n" +
                "QueryTEtYFNQCaDWZXsjoMwxxJ9tymhXx*****\n" +
                "\n" +
                "9\uFE0F⃣“设置日切12”：设置每日截止时间+（0~23之间的整数）（可修改各组每日截止时间，默认为中国北京时间12:00）\n" +
                "\"Set daily cut-off 12\": Set the daily cut-off time + (an integer between 0 and 23) (the daily cut-off time of each group can be modified, the default is 12:00 Beijing time, China)\n" +
                "\uD83D\uDD1F其他功能（Other functions）\n" +
                "通知（notify）\n" +
                "显示操作人名称（Show operator name）\n" +
                "隐藏操作人名称（Hide operator name）\n" +
                "显示回复人名称（Show replyer name）\n" +
                "隐藏回复人名称（Hide reply name）\n" +
                "隐藏名称（Hide name）\n" +
                "切换中文（Switch to Chinese）\n" +
                "切换英文（Switch to English）\n" +
                "显示分类（Show categories）\n" +
                "隐藏分类（Hide categories）\n" +
                "设置手续费10（Setup fee 10）0为关闭（0 is off）\n" +
                "隐藏手续费（Hidden fees）\n" +
                "显示手续费（Show handling fee）\n" +
                "显示1条（Show 1 item）\n" +
                "显示3条（Show 3 item）\n" +
                "显示5条（Show 5 item）\n" +
                "显示金额（Display amount）\n" +
                "显示usdt（show usdt）\n" +
                "显示全部（Show all）\n" +
                "显示明细（Show details）\n" +
                "隐藏明细（hide details）\n" +
                "“设置下发地址“：设置下发地+下发地址信息\n" +
                "“查看下发地址“ ： 群内输入关键字    下发地址\n" +
                "P余额功能+指令，P100{增加余额，只显示在独立分类}\n" +
                "After-sales customer service: @vipkefu @yaokevipBot\n" +
                "售后客服： @vipkefu @yaokevipBot";
        sendMessage.setText(msg);
        accountBot.sendMessage(sendMessage);
    }

    private void getListening(Message message, SendMessage sendMessage, UserDTO userDTO) {
        List<WalletListener> walletListeners = walletListenerService.queryAll(userDTO.getUserId());
        Map<String, String> map = new HashMap<>();
        List<String> address = walletListeners.stream().filter(Objects::nonNull).map(walletListener -> {
            map.put(walletListener.getNickname(),walletListener.getAddress());
            return walletListener.getAddress();
        }).collect(Collectors.toList());
        StringBuilder stringBuilder = new StringBuilder();
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", message.getChatId().toString()));
        map.entrySet().forEach(entry -> stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
        String result;
        if (groupInfoSetting.getEnglish()){
            result="\uD83D\uDCB0 您已设置 "+map.entrySet().size()+"个通知：\n" +
                    "⁉\uFE0F 点击下列钱包对应标识按钮，查看钱包详情\n" +
                    "➖➖➖➖➖➖➖➖➖➖➖\n"+stringBuilder;
        }else{
            result="\uD83D\uDCB0 You have set "+map.entrySet().size()+"Notifications：\n" +
                    "⁉\uFE0F Click the corresponding icon button of the following wallet to view the wallet details\n" +
                    "➖➖➖➖➖➖➖➖➖➖➖\n"+stringBuilder;
        }
        ButtonList buttonList=new ButtonList();
        buttonList.sendButton(sendMessage, String.valueOf(userDTO.getUserId()),map);
        accountBot.sendMessage(sendMessage,result);
    }

    //获取用户信息
    private void getUserInfoMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        User user = userService.findByUserId(userDTO.getUserId());
        if (user==null){
            user = new User();
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(48);
//            LocalDateTime tomorrow = LocalDateTime.now().plusHours(8);//英文8小时
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            user.setUserId(userDTO.getUserId());
            user.setUsername(userDTO.getUsername());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setCreateTime(new Date());
            user.setSuperAdmin(true);
            user.setValidTime(validTime);
            user.setValidFree(true);//已经体验过会员
            userService.insertUser(user);

        }else if (!user.isValidFree()) {//还没有体验过免费6小时
//            LocalDateTime tomorrow = LocalDateTime.now().plusHours(8);//英文8小时
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(48);//
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            user.setValidTime(validTime);
            user.setSuperAdmin(true);//默认操作权限管理员
            user.setValidFree(true);//是使用过免费6小时
            userService.updateUser(user);
        }
        LocalDateTime t= LocalDateTime.ofInstant(user.getValidTime().toInstant(), ZoneId.systemDefault());
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", user.getUserId()));
        String time;
        if (groupInfoSetting.getEnglish()){
            time=t.getYear()+"年"+t.getMonthValue()+ "月"+
                    t.getDayOfMonth()+"日"+ t.getHour()+"时"+t.getMinute()+"分" +t.getSecond()+"秒";
        }else {
            time=t.getYear()+"year"+t.getMonthValue()+ "month"+
                    t.getDayOfMonth()+"day"+ t.getHour()+"hour"+t.getMinute()+"minute" +t.getSecond()+"second";
        }
        String firstName=userDTO.getFirstName()==null?"": userDTO.getFirstName();
        String lastName=userDTO.getLastName()==null?"":userDTO.getLastName();
        String message1;
        if (groupInfoSetting.getEnglish()){
            message1="<b>账号个人信息</b>✅：\n" +
                    "\n" +
                    "<b>用户名：</b>@"+userDTO.getUsername()+" \n" +
                    "<b>用户ID：</b><code>"+userDTO.getUserId()+"</code>\n" +
                    "<b>用户昵称：</b>"+firstName+lastName+"\n" +
                    "<b>有效期：</b>"+time;
        }else {
            message1="<b>Account Information</b>✅：\n" +
                    "\n" +
                    "<b>Username：</b>@"+userDTO.getUsername()+" \n" +
                    "<b>UserID：</b><code>"+userDTO.getUserId()+"</code>\n" +
                    "<b>Nickname：</b>"+firstName+lastName+"\n" +
                    "<b>Validity：</b>"+time;
        }
        PaperPlaneBotButton buttonList = new PaperPlaneBotButton();
        Map<String, String> map = new LinkedHashMap<>();
        if (groupInfoSetting.getEnglish()){
            map.put("✅把我添加到群", "https://t.me/"+this.username+"?startgroup=add2chat");
        }else {
            map.put("✅Add me to the group", "https://t.me/"+this.username+"?startgroup=add2chat");
        }
        buttonList.sendButton(sendMessage, map);
        accountBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),message1);
    }
    // 存储用户正在广播的媒体组 ID 及其已处理的消息数量
    private static final Map<String, Set<String>> processedMediaGroups = new ConcurrentHashMap<>();
    private static final Map<String, List<Integer>> pendingBroadcastMessageMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastActiveTimeMap = new ConcurrentHashMap<>();
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredData() {
        long now = System.currentTimeMillis();
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, String> entry : userStates.entrySet()) {
            String userId = entry.getKey();
            Long lastActive = getLastActiveTime(userId);
            // 判断是否超时（24小时）
            if (lastActive == null || now - lastActive > TimeUnit.HOURS.toMillis(24)) {
                toRemove.add(userId);
            }
        }
        // 执行清理
        toRemove.forEach(this::removeUserData);
    }
    private Long getLastActiveTime(String userId) {
        return lastActiveTimeMap.get(userId);
    }
    private void removeUserData(String userId) {
        userStates.remove(userId);
        processedMediaGroups.remove(userId);
        pendingBroadcastMessageMap.remove(userId);
        lastActiveTimeMap.remove(userId);
        log.info("已清理超时用户状态: {}", userId);
    }

    public void handlerPrivateUser(Update update, Message message, SendMessage sendMessage, UserDTO userDTO) {
        if (userDTO.getText()!=null && userDTO.getText().equals("/admin")){
            sendMessage.setText("点击下方按钮进入超级管理后台!");
            Map<String, String> map = new HashMap<>();
            map.put("超级管理后台",vueUrl+"AccountLogin");
            buttonList.sendButtonLink(sendMessage,userDTO.getUserId(),map);
            accountBot.sendMessage(sendMessage);
            return;
        }
        String text = userDTO.getText();
        boolean b = text.equals("获取个人信息（personal information）")|| text.equals("监听列表（listening address）")||
        text.equals("使用说明（illustrate）")||text.equals("群发广播（Group Broadcast）")||text.equals("超级管理员广播（Super Management Broadcast）")||
                text.equals("自助续费（Self-service renewal）") || text.equals("/start");
        if (text!=null && b){
            return;
        }
        String userId = userDTO.getUserId();
        String userState = userStates.getOrDefault(userId, STATE_NORMAL);
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userId));
        if (STATE_BROADCAST.equals(userState)) {
            // 更新最后活跃时间
            updateLastActiveTime(userId);
            // 构建按钮并发送确认消息
            Map<String, String> buttonText = new LinkedHashMap<>();
            List<UserNormal> userNormals = userNormalMapper.selectList(new QueryWrapper<UserNormal>().eq("user_id", userId));
            if (groupInfoSetting.getEnglish()) {
                buttonText.put("确认发送", "confirm_broadcast_" + userDTO.getUserId());
                buttonText.put("取消广播", "cancel_broadcast_" + userDTO.getUserId());
                sendMessage.setText("请确认您要广播的内容! 共计发送到【"+userNormals.size()+"】个群组.");
            } else {
                buttonText.put("Confirm Send", "confirm_broadcast_" + userDTO.getUserId());
                buttonText.put("Cancel Broadcast", "cancel_broadcast_" + userDTO.getUserId());
                sendMessage.setText("Please confirm what you want to broadcast!"+" Total sent to ["+userNormals.size()+"] groups.");
            }
            buttonList.sendButton(sendMessage, userId, buttonText);
            // 获取 mediaGroupId
            String mediaGroupId = message.getMediaGroupId();
            // 缓存消息 ID
            if (mediaGroupId != null) {
                pendingBroadcastMessageMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(message.getMessageId());
            } else if (message.hasText()) {
                pendingBroadcastMessageMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(message.getMessageId());
            }
            // 判断是否是该媒体组的第一个消息
            boolean isFirstMedia = mediaGroupId != null &&
                    processedMediaGroups.computeIfAbsent(userId, k -> new HashSet<>()).add(mediaGroupId);
            // 判断是否是该媒体组的最后一个消息
            boolean isLastMedia = mediaGroupId == null || !isFirstMedia;
            // 只有是最后一个媒体项时才清除状态
            if (isLastMedia) {
                userStates.remove(userId); // ✅ 只在最后一张图清除状态
            }
            accountBot.sendMessageReplay(sendMessage, true, message.getMessageId());
        }else if (ADMIN_STATE_BROADCAST.equals(userState)){
            // 更新最后活跃时间
            updateLastActiveTime(userId);
            // 构建按钮并发送确认消息
            Map<String, String> buttonText = new LinkedHashMap<>();
            List<Status> statuses = statusMapper.selectList(new QueryWrapper<Status>().select("group_id").groupBy("group_id"));
            if (groupInfoSetting.getEnglish()) {
                buttonText.put("确认发送", "admin_confirm_broadcast_" + userDTO.getUserId());
                buttonText.put("取消广播", "admin_cancel_broadcast_" + userDTO.getUserId());
                sendMessage.setText("请确认您要广播的内容! 共计发送到【"+statuses.size()+"】个群组.");
            } else {
                buttonText.put("Confirm Send", "admin_confirm_broadcast_" + userDTO.getUserId());
                buttonText.put("Cancel Broadcast", "admin_cancel_broadcast_" + userDTO.getUserId());
                sendMessage.setText("Please confirm what you want to broadcast!"+" Total sent to ["+statuses.size()+"] groups.");
            }
            buttonList.sendButton(sendMessage, userId, buttonText);
            // 获取 mediaGroupId
            String mediaGroupId = message.getMediaGroupId();
            // 缓存消息 ID
            if (mediaGroupId != null) {
                pendingBroadcastMessageMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(message.getMessageId());
            } else if (message.hasText()) {
                pendingBroadcastMessageMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(message.getMessageId());
            }
            // 判断是否是该媒体组的第一个消息
            boolean isFirstMedia = mediaGroupId != null && processedMediaGroups.computeIfAbsent(userId, k -> new HashSet<>()).add(mediaGroupId);
            // 判断是否是该媒体组的最后一个消息
            boolean isLastMedia = mediaGroupId == null || !isFirstMedia;
            // 只有是最后一个媒体项时才清除状态
            if (isLastMedia) {
                userStates.remove(userId); // ✅ 只在最后一张图清除状态
            }
            accountBot.sendMessageReplay(sendMessage, true, message.getMessageId());
        }
    }

    private void updateLastActiveTime(String userId) {
        lastActiveTimeMap.put(userId, System.currentTimeMillis());
    }


}
