package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    @Qualifier("userOperationService")
    @Autowired
    private UserOperationService userOperationService;

    @PostConstruct
    public void init() {
        // 启动定时任务，首次立即执行，之后每隔30秒执行一次
        scheduler.scheduleAtFixedRate(this::fetchAndCacheData, 0, 15, TimeUnit.SECONDS);
    }
    private Set<String> processedTransactions = Collections.synchronizedSet(new HashSet<>());
    //
    private void fetchAndCacheData() {
        List<WalletListener> walletListeners = walletListenerService.queryAll();
        walletListeners.stream().filter(Objects::nonNull).forEach(w -> {
            String url = tranHistoryUrl+w.getAddress();
            List<TronHistoryDTO> historyTrading = restTemplateConfig.getForObjectHistoryTrading(url, Map.class);
            historyTrading.stream().filter(Objects::nonNull).findFirst().ifPresent(t -> { // 在这里处理第一个非空的 HistoryTrading 对象
                long difference = Math.abs(new Date().getTime() - t.getBlock_ts()); // 计算两个时间戳之间的差值
                long thirtySecondsInMilliseconds = 30 * 1000; // 30 秒转换为毫秒
                System.out.println("两个时间相差 " + difference/ 1000 + " 秒");
                //如果当前时间相差大于30秒，则进行 通知用户否则不通知
                if (difference <= thirtySecondsInMilliseconds && !processedTransactions.contains(t.getTransaction_id())) {
                    String result="";
                    BigDecimal balance = new BigDecimal(t.getQuant());
                    // 计算移动小数点后的 balance
                    BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(t.getTokenInfo().getTokenDecimal()));
                    String type=t.getTo_address().equals(w.getAddress())==true?"入账":"出账";
                    if (t.getTo_address().equals(w.getAddress())==true){
                        result="交易金额： "+bigDecimal+t.getTokenInfo().getTokenAbbr()+" 已确认 #"+type+"\n" +
                                "交易币种： ❇\uFE0F #"+t.getTokenInfo().getTokenAbbr()+"\n" +
                                "收款地址： "+t.getTo_address()+" (https://tronscan.org/#/address/"+t.getTo_address()+"/transfers)\n" +
                                "支付地址： "+t.getFrom_address()+" (https://tronscan.org/#/address/"+t.getTo_address()+"/transfers)\n" +
                                "交易哈希： "+t.getTransaction_id()+" (https://tronscan.org/#/transaction/"+t.getTransaction_id()+")\n" +
                                "转账时间："+sdf.format(new Date(t.getBlock_ts()))+"\n" +
                                "\uD83D\uDCE3 监控地址 ("+w.getAddress()+")" ;
                    }else {
                        result="交易金额： "+bigDecimal+t.getTokenInfo().getTokenAbbr()+" 已确认 #"+type+"\n" +
                                "交易币种： ❇\uFE0F #"+t.getTokenInfo().getTokenAbbr()+"\n" +
                                "收款地址： "+t.getTo_address()+" (https://tronscan.org/#/address/"+t.getTo_address()+"/transfers)\n" +
                                "支付地址： "+t.getFrom_address()+" (https://tronscan.org/#/address/"+t.getTo_address()+"/transfers)\n" +
                                "交易哈希： "+t.getTransaction_id()+" (https://tronscan.org/#/transaction/"+t.getTransaction_id()+")\n" +
                                "转账时间："+sdf.format(new Date(t.getBlock_ts()))+"\n" +
                                "\uD83D\uDCE3 监控地址 ("+w.getAddress()+")";
                    }
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(w.getUserId());
                    sendMessage.disableWebPagePreview();//禁用预览链接
                    accountBot.sendMessage(sendMessage,result);
                    // 标记此交易已处理
                    processedTransactions.add(t.getTransaction_id());
                }
            });
        });
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
        if (text.equals("获取个人信息") || text.equals("personal information")){
            this.getUserInfoMessage(message,sendMessage,userDTO);
            return;
        }else if (text.equals("监听列表") || text.equals("listening address")){
            this.getListening(message,sendMessage,userDTO);
            return;
        }else if (text.equals("使用说明") || text.equals("illustrate")){
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
        if (text.equals("/start")) accountBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),"你好！<b>欢迎使用本机器人：\n" +
                "\n" +
                "点击下方底部按钮：获取个人信息\n" +
                "（将我拉入群组可免费使用8小时）\n" +
                "\n" +
                "将TRC20地址发送给我，即可设置入款通知；\n" +
                "群友在群中发送U地址即可查询该地址当前余额； \n" +
                "\n" +
                "➖➖➖➖➖➖➖➖➖➖➖\n" +
                "本机器人用户名 ： </b><code>@"+username+"</code>\n" +//（点击复制）
                "\n" +
                "<b>联系客服：</b>@vipkefu\n" +
                "<b>双向客服：</b>@yewuvipBot");
    }

    //使用说明
    private void useInfo(Message message, SendMessage sendMessage, UserDTO userDTO) {
        accountBot.sendMessage(sendMessage,"①增加机器人进群。群右上角--Add member-输入  @Evipbot\n" +
                "②输入”设置费率X.X“\n" +
                "③输入”设置汇率X.X“\n" +
                "④取消命令：  撤销入款        撤销下发\n" +
                "\n" +
                "设置操作人 @***** ，注意：@前面有个空格 。\n" +
                "显示操作人\n" +
                "删除操作人 @***** ，注意：@前面有个空格 。\n" +
                "\n" +
                "\n" +
                "清理今天数据：删除账单  \n" +
                "\n" +
                "删除所有账单(关闭日切模式使用)\n" +
                "\n" +
                "Z0：欧易商家实时交易汇率top10\n" +
                "\n" +
                "计算器功能：\n" +
                "（100+100）（1000*0.05）\n" +
                "（1000/5）   （1000-5）\n" +
                "\n" +
                "临时入款汇率：+金额/汇率      演示公式： +100/5\n" +
                "\n" +
                "*注：\n" +
                "\n" +
                "“设置记账时间12”：设置记账时间+（0到23之间的整数）\n" +
                "（日切数据默认北京时间中午12点重置）\n" +
                "\n" +
                "单笔手续费：\n" +
                "“设置入款单笔手续费“关键字 ： 设置入款单笔手续费5\n" +
                "“设置下发单笔手续费“关键字 ： 设置下发单笔手续费5\n" +
                "\n" +
                "电报界面显示命令：\n" +
                "显示操作人名字（显示操作人名字）\n" +
                "显示回复人名字（显示回复人名字）\n" +
                "隐藏名字\n" +
                "显示明细\n" +
                "隐藏明细\n" +
                "显示分类\n" +
                "隐藏分类\n" +
                "显示余额\n" +
                "显示USDT\n" +
                "显示usdt\n" +
                "显示全部\n" +
                "显示1条\n" +
                "显示3条\n" +
                "显示5条\n" +
                "关闭日切\n" +
                "开启日切\n" +
                "显示手续费\n" +
                "隐藏手续费\n" +
                "通知所有人（ 操作员发送才可生效 ）\n" +
                "查询地址余额：群内发送 查询+地址\n" +
                "\n" +
                "售后客服： @vipkefu"  );
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
