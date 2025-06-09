package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.TronAccountDTO;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.UserNormalService;
import org.example.bot.accountBot.service.UserOperationService;
import org.example.bot.accountBot.service.UserService;
import org.example.bot.accountBot.service.WalletListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    @Value("${adminUserId}")
    protected String adminUserId;
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    UserNormalService userNormalService;
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;
    @Autowired
    WalletListenerService walletListenerService;
    @Value("${tranAccountUrl}")
    protected String tranAccountUrl;//查询账户余额
    @Value("${tranHistoryUrl}")
    protected String tranHistoryUrl;//查询账户历史交易
    @Resource
    RestTemplateConfig restTemplateConfig;
    // 创建 SimpleDateFormat 对象，指定日期格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 定时任务调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);
    @Qualifier("userOperationService")
    @Autowired
    private UserOperationService userOperationService;
    // 设置缓存过期时间（比如1小时）
    private static final long TRANSACTION_EXPIRE_TIME = TimeUnit.HOURS.toMillis(1);
    private final Map<String, Long> lastTransactionTimeMap = new ConcurrentHashMap<>();
    private void cleanUpCache() {
        long now = System.currentTimeMillis();
        lastTransactionTimeMap.entrySet().removeIf(entry -> now - entry.getValue() > TRANSACTION_EXPIRE_TIME);
    }
    @SneakyThrows
    @PostConstruct
    public void init() {
        // 启动定时任务，首次立即执行，之后每隔30秒执行一次
        scheduler.scheduleAtFixedRate(this::fetchAndCacheData, 0, 30, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::cleanUpCache, 0, 1, TimeUnit.HOURS);
    }
    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
        asyncExecutor.shutdownNow();
    }
    private void fetchAndCacheData() {
        List<WalletListener> walletListeners = walletListenerService.queryAll();//每秒最多调用5次查询
        walletListeners.parallelStream().filter(Objects::nonNull).forEach(w ->  CompletableFuture.runAsync(() ->{
            try {
                // 每次请求前等待一段时间，控制频率
                Thread.sleep( 200); // 200ms 间隔
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String url = tranHistoryUrl+w.getAddress();
            List<TronHistoryDTO> historyTrading = restTemplateConfig.getForObjectHistoryTrading(url, Map.class);
            historyTrading.stream().filter(Objects::nonNull).findFirst().ifPresent(t -> { // 在这里处理第一个非空的 HistoryTrading 对象
                long now = System.currentTimeMillis();//这个缓存有问题 我这个userId不一样但是监听的地址都一样所以只提醒了一个应该是 TODO
                long blockTime = t.getBlock_ts(); // TRON 时间戳是毫秒级
                // 设置一个“有效时间窗口”，例如 60 秒内才视为新交易
                long NEW_TRANSACTION_WINDOW = TimeUnit.SECONDS.toMillis(60);
                if (now - blockTime <= NEW_TRANSACTION_WINDOW && !lastTransactionTimeMap.containsKey(t.getTransaction_id()+w.getUserId())) {
                    String result="";
                    BigDecimal balance = new BigDecimal(t.getQuant());
                    // 计算移动小数点后的 balance
                    BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(t.getTokenInfo().getTokenDecimal()));
                    String type=t.getTo_address().equals(w.getAddress())==true?"入账":"出账";
                    if (t.getTo_address().equals(w.getAddress())==true){
                        result="交易金额： "+bigDecimal+t.getTokenInfo().getTokenAbbr()+" 已确认 #"+type+"\n" +
                                "交易币种： ❇\uFE0F #"+t.getTokenInfo().getTokenAbbr()+"\n" +
                                "收款地址： <code>"+t.getTo_address()+"</code>\n" +
                                "支付地址： <code>"+t.getFrom_address()+"</code>\n" +
                                "交易哈希： "+t.getTransaction_id()+" (https://tronscan.org/#/transaction/"+t.getTransaction_id()+")\n" +
                                "转账时间："+sdf.format(new Date(t.getBlock_ts()))+"\n" +
                                "\uD83D\uDCE3 监控地址 ("+w.getAddress()+")" ;
                    }else {
                        result="交易金额： "+bigDecimal+t.getTokenInfo().getTokenAbbr()+" 已确认 #"+type+"\n" +
                                "交易币种： ❇\uFE0F #"+t.getTokenInfo().getTokenAbbr()+"\n" +
                                "收款地址： <code>"+t.getTo_address()+"</code>\n" +
                                "支付地址： <code>"+t.getFrom_address()+"</code>\n" +
                                "交易哈希： "+t.getTransaction_id()+" (https://tronscan.org/#/transaction/"+t.getTransaction_id()+")\n" +
                                "转账时间："+sdf.format(new Date(t.getBlock_ts()))+"\n" +
                                "\uD83D\uDCE3 监控地址 ("+w.getAddress()+")";
                    }
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(w.getUserId());
                    sendMessage.disableWebPagePreview();//禁用预览链接
                    accountBot.sendMessage(sendMessage,result);
                    // 标记此交易已处理
                    lastTransactionTimeMap.put(t.getTransaction_id()+w.getUserId(), now);
                }
            });
        }));
    }
    //获取个人账户信息
    protected void handleTronAccountMessage(SendMessage sendMessage, Update update,UserDTO userDTO){
        String text = userDTO.getText();
        // 检查是否符合标准
        if (text.length() != 34) {
            return;
        }
        String userId = userDTO.getUserId();
        String url = tranAccountUrl+text;
        TronAccountDTO tronAccount = restTemplateConfig.getForObjectTronAccount(url);
        String htmlText = "[USDT余额](https://tronscan.org/#/address/"+ text + "/transfers)";
        String trxText = "[TRX余额](https://tronscan.org/#/address/"+ text + "/transfers)";
        AtomicReference<BigDecimal> bigDecimal= new AtomicReference<>();
        AtomicReference<String> trxBigDecimal= new AtomicReference<>();
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
        String result="✅✅✅✅\n" +
                text+"\n" +
                "——————————\n" + usdt+ trx+
                "——————————\n" +
                "交易次数：" +tronAccount.getTransactions()+"\n"+
                "   -出："+tronAccount.getTransactions_out()+"\n"+
                "   -入："+tronAccount.getTransactions_in()+"\n"+
                "地址创建时间："+sdf.format(new Date(tronAccount.getDate_created()))+"\n"+
                "最新交易时间："+sdf.format(new Date(tronAccount.getLatest_operation_time()))+"\n";
        ButtonList buttonList = new ButtonList();
        Map<String,String> buttonTextMap=new HashMap<>();
        buttonTextMap.put("监听该地址","监听该地址");
        buttonTextMap.put("查询交易记录","查询交易记录");
        //这里是私聊的查询 所以用user_id
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getUserId()));
        buttonList.sendButton(sendMessage, String.valueOf(userId),buttonTextMap,groupInfoSetting);
        accountBot.tronAccountMessageText(sendMessage,userId,result);
    }



    //设置机器人在群组内的有效时间 默认免费使用日期6小时. 机器人底部按钮 获取个人信息 获取最新用户名 获取个人id 使用日期;
    protected void handleNonGroupMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        String text = userDTO.getText().toLowerCase();
        //授权-123456789-30  用户id -30
        PaperPlaneBotButton buttonList = new PaperPlaneBotButton();
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getUserId()));
        ReplyKeyboardMarkup replyKeyboardMarkup = buttonList.sendReplyKeyboard(groupInfoSetting);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);//是否在onUpdateReceived设置
        String regex = "^授权-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
        String regexEn = "^authorization-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
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
        }else if (text.contains("##") &&text.length()>=37){
            // 使用 split 方法分割字符串
            String[] parts = text.split("##");
            String messageResult="";
            if (parts.length == 2) {
                String address = parts[0];
                String nickName = parts[1];
                //只有长度大于37才进行修改
                walletListenerService.updateWalletListener(userDTO.getUserId() + "", address, nickName);
                messageResult="修改成功";
            } else {
                messageResult="字符串格式不正确，没有找到 ## 或者 ## 数量不对";
            }
            accountBot.sendMessage(sendMessage,messageResult);
            return;
        }
        if (text.contains("授权")){
            if (text.startsWith("授权-")&&!userDTO.getUserId().equals(adminUserId)){
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
            LocalDateTime tomorrow= LocalDateTime.now().plusDays(Long.parseLong(validTimeText));
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
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
                UserNormal userNormal = new UserNormal();
                userNormal.setAdmin(true);
                userNormal.setGroupId(message.getChatId().toString());
                userNormal.setUserId(userDTO.getUserId());
                userNormal.setCreateTime(new Date());
                userNormal.setUsername(userDTO.getUsername());
                userNormalService.insertUserNormal(userNormal);
                UserOperation userOperation = new UserOperation();
                userOperation.setAdminUserId(userDTO.getUserId());
                userOperation.setOperation(true);
                userOperation.setUserId(userDTO.getUserId());
                userOperation.setUsername(userDTO.getUsername());
                userOperation.setGroupId(message.getChatId().toString());
                userOperation.setCreateTime(new Date());
                userOperationService.insertUserOperation(userOperation);
            }else {
                user.setSuperAdmin(true);//默认操作权限管理员
                user.setValidTime(validTime);
                user.setValidFree(true);
                userService.updateUserValidTime(user,validTime);
                UserNormal userNormal = new UserNormal();
                userNormal.setAdmin(true);
                userNormal.setGroupId(message.getChatId().toString());
                userNormal.setUserId(userDTO.getUserId());
                userNormal.setCreateTime(new Date());
                userNormal.setUsername(userDTO.getUsername());
                userNormalService.insertUserNormal(userNormal);
                UserOperation userOperation = new UserOperation();
                userOperation.setAdminUserId(userDTO.getUserId());
                userOperation.setOperation(true);
                userOperation.setUserId(userDTO.getUserId());
                userOperation.setUsername(userDTO.getUsername());
                userOperation.setGroupId(message.getChatId().toString());
                userOperation.setCreateTime(new Date());
                userOperationService.insertUserOperation(userOperation);
            }
            accountBot.sendMessage(sendMessage,"用户ID: "+userId+" 有效期:"+tomorrow.getYear()+"年"+tomorrow.getMonthValue()+ "月"+
                    tomorrow.getDayOfMonth()+"日"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
            return;
        } else if (text.contains("authorization")) {
            if (text.startsWith("authorization-")&&!userDTO.getUserId().equals(adminUserId)){
                accountBot.sendMessage(sendMessage,"您不是超级管理!无权限设置管理员!");
                return;
            }
            boolean matches = text.matches(regexEn);
            String validTimeText = "";
            String userId=split3[1];
            User user=userService.findByUserId(userId);
            if (matches) {
                validTimeText=split3[2];
            }else {
                accountBot.sendMessage(sendMessage,"格式不匹配!");
                return;
            }
            LocalDateTime tomorrow= LocalDateTime.now().plusDays(Long.parseLong(validTimeText));
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
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
                UserNormal userNormal = new UserNormal();
                userNormal.setAdmin(true);
                userNormal.setGroupId(message.getChatId().toString());
                userNormal.setUserId(userDTO.getUserId());
                userNormal.setCreateTime(new Date());
                userNormal.setUsername(userDTO.getUsername());
                userNormalService.insertUserNormal(userNormal);
                UserOperation userOperation = new UserOperation();
                userOperation.setAdminUserId(userDTO.getUserId());
                userOperation.setOperation(true);
                userOperation.setUserId(userDTO.getUserId());
                userOperation.setUsername(userDTO.getUsername());
                userOperation.setGroupId(message.getChatId().toString());
                userOperation.setCreateTime(new Date());
                userOperationService.insertUserOperation(userOperation);
            }else {
                user.setSuperAdmin(true);//默认操作权限管理员
                user.setValidTime(validTime);
                user.setValidFree(true);
                userService.updateUserValidTime(user,validTime);
                UserNormal userNormal = new UserNormal();
                userNormal.setAdmin(true);
                userNormal.setGroupId(message.getChatId().toString());
                userNormal.setUserId(userDTO.getUserId());
                userNormal.setCreateTime(new Date());
                userNormal.setUsername(userDTO.getUsername());
                userNormalService.insertUserNormal(userNormal);
                UserOperation userOperation = new UserOperation();
                userOperation.setAdminUserId(userDTO.getUserId());
                userOperation.setOperation(true);
                userOperation.setUserId(userDTO.getUserId());
                userOperation.setUsername(userDTO.getUsername());
                userOperation.setGroupId(message.getChatId().toString());
                userOperation.setCreateTime(new Date());
                userOperationService.insertUserOperation(userOperation);
            }
            accountBot.sendMessage(sendMessage,"用户ID: "+userId+" 有效期:"+tomorrow.getYear()+"年"+tomorrow.getMonthValue()+ "月"+
                    tomorrow.getDayOfMonth()+"日"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
            return;
        }
        if (text.equals("/start")) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("✅把我添加到群", "https://t.me/"+this.username+"?startgroup=add2chat");
            buttonList.sendButton(sendMessage, map);
            accountBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),"你好！<b>欢迎使用本机器人：\n" +
                    "\n" +
                    "点击下方底部按钮：获取个人信息\n" +
                    "（将我拉入群组可免费使用8小时）\n" +
                    "\n" +
                    "将TRC20地址发送给我，即可设置入款通知；\n" +
                    "群友在群中发送U地址即可查询该地址当前余额； \n" +
                    "\n" +
                    "➖➖➖➖➖➖➖➖➖➖➖\n" +
                    "本机器人用户名 ： </b><code>@"+username+"</code>\n" +
                    "\n" +
                    "<b>联系客服：</b>@vipkefu\n" +
                    "<b>双向客服：</b>@yaokevipBot");
        }
    }

    //使用说明
    private void useInfo(Message message, SendMessage sendMessage, UserDTO userDTO) {
        String msg="1\uFE0F⃣增加机器人进群。群右上角--Add member-输入 @TTpayvipbot\n" +
                " Add robots to the group. In the upper right corner of the group--Add member-enter @TTpayvipbot\n" +
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
        map.entrySet().forEach(entry -> {stringBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");});
        String result="\uD83D\uDCB0 您已设置 "+map.entrySet().size()+"个通知：\n" +
                "⁉\uFE0F 点击下列钱包对应标识按钮，查看钱包详情\n" +
                "➖➖➖➖➖➖➖➖➖➖➖\n"+stringBuilder;
        ButtonList buttonList=new ButtonList();
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", message.getChatId().toString()));
        buttonList.sendButton(sendMessage, String.valueOf(userDTO.getUserId()),map,groupInfoSetting);
        accountBot.sendMessage(sendMessage,result);
    }

    //获取用户信息
    private void getUserInfoMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        User user = userService.findByUserId(userDTO.getUserId());
        if (user==null){
            user = new User();
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(8);
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
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(8);
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            user.setValidTime(validTime);
            user.setSuperAdmin(true);//默认操作权限管理员
            user.setValidTime(validTime);
            user.setValidFree(true);//是使用过免费6小时
            userService.updateUser(user);
        }
        LocalDateTime t= LocalDateTime.ofInstant(user.getValidTime().toInstant(), ZoneId.systemDefault());
        String   time=" 有效期:"+t.getYear()+"年"+t.getMonthValue()+ "月"+
                t.getDayOfMonth()+"日"+ t.getHour()+"时"+t.getMinute()+"分" +t.getSecond()+"秒";
        String firstName=userDTO.getFirstName()==null?"": userDTO.getFirstName();
        String lastName=userDTO.getLastName()==null?"":userDTO.getLastName();
        String message1="<b>账号个人信息</b>✅：\n" +
                "\n" +
                "<b>用户名：</b>@"+userDTO.getUsername()+" \n" +
                "<b>用户ID：</b><code>"+userDTO.getUserId()+"</code>\n" +
                "<b>用户昵称：</b>"+firstName+lastName+"\n" +
                "<b>有效期：</b>"+time;
        accountBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),message1);
    }


}
