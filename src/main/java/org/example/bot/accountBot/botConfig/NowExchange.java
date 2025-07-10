package org.example.bot.accountBot.botConfig;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.*;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.mapper.UserNormalMapper;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.WalletListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 获取最新汇率
 */

@Slf4j
@Service
public class NowExchange {

    @Value("${tranAccountUrl}")
    protected String tranAccountUrl;//查询账户余额
    @Value("${tranHistoryUrl}")
    protected String tranHistoryUrl;//查询账户历史交易
    @Resource
    RestTemplateConfig restTemplateConfig;
    // 创建 SimpleDateFormat 对象，指定日期格式
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    protected WalletListenerService walletListenerService;
    @Resource
    protected AccountBot accountBot;
    @Autowired
    private ConfigEditHandler configEditHandler;
    private final OkHttpClient client = new OkHttpClient();
    // 定时任务调度器
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;
    @Autowired
    private UserNormalMapper userNormalMapper;
    @Autowired
    private StatusMapper statusMapper;
//    @Autowired
//    private SettingOperatorPerson settingOperatorPerson;


    public void Query(SendMessage sendMessage,Update update,GroupInfoSetting groupInfoSetting){
        String text = update.getMessage().getText();
        String substring;
        if (text.startsWith("Query")){
            substring = text.substring("Query".length(), text.length());
        }else {
            substring = text.substring(2, text.length());
        }
        Long groupId = update.getMessage().getChatId();
        if (StringUtils.isBlank(substring)){
            return;
        }
        String url = tranAccountUrl+substring;
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
                    substring+"\n" +
                    "——————————\n" + usdt+ trx+
                    "——————————\n" +
                    "交易次数：" +tronAccount.getTransactions()+"\n"+
                    "   -出："+tronAccount.getTransactions_out()+"\n"+
                    "   -入："+tronAccount.getTransactions_in()+"\n"+
                    "地址创建时间："+sdf.format(new Date(tronAccount.getDate_created()))+"\n"+
                    "最新交易时间："+sdf.format(new Date(tronAccount.getLatest_operation_time()))+"\n";
        }else{
            result="✅✅✅✅\n" +
                    substring+"\n" +
                    "——————————\n" + usdt+ trx+
                    "——————————\n" +
                    "Number of transactions：" +tronAccount.getTransactions()+"\n"+
                    "   -out："+tronAccount.getTransactions_out()+"\n"+
                    "   -enter："+tronAccount.getTransactions_in()+"\n"+
                    "Address creation time："+sdf.format(new Date(tronAccount.getDate_created()))+"\n"+
                    "Latest trading hours："+sdf.format(new Date(tronAccount.getLatest_operation_time()))+"\n";
        }
        accountBot.tronAccountMessageText(sendMessage,groupId+"",result);
    }

    //查询交易记录
    private void queryHistoryTrading(Message message,SendMessage sendMessage,GroupInfoSetting groupInfoSetting) {
        String text = message.getText();
        String substring = text.substring(4, text.indexOf("——————————")).trim();
        String url = tranHistoryUrl+substring;
        String result;
        if (groupInfoSetting.getEnglish()){
            result = "|    时间    |类型|   金额  |地址   \n" +
                    "|-----------|----|--------|---- \n";
        }else {
            result = "|    time    |type|   Amount  |address   \n" +
                    "|-----------|----|--------|---- \n";
        }
        List<TronHistoryDTO> tradingList=restTemplateConfig.getForObjectHistoryTrading(url,TronHistoryDTO.class);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(result);
        for (int i = 0; i < tradingList.size(); i++) {
            BigDecimal balance = new BigDecimal(tradingList.get(i).getQuant());
            // 计算移动小数点后的 balance
            BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(tradingList.get(i).getTokenInfo().getTokenDecimal()));
            String type;
            if (groupInfoSetting.getEnglish()){
                type=tradingList.get(i).getTo_address().equals(substring)==true?"入账":"出账";
            }else {
                type=tradingList.get(i).getTo_address().equals(substring)==true?"Entry" : "Out";
            }
            stringBuilder.append("|"+sdf.format(new Date(tradingList.get(i).getBlock_ts()))+"|"+type+"|"+bigDecimal+"|"+tradingList.get(i).getTokenInfo().getTokenAbbr()+"|"+"\n");
        }
        accountBot.sendMessage(sendMessage,stringBuilder.toString());
    }
    private void walletListenerAddress(Message message, SendMessage sendMessage,GroupInfoSetting groupInfoSetting) {
        Long id = message.getChat().getId();
        String text = message.getText();
        String address = text.substring(4, text.indexOf("——————————")).trim();
        WalletListener temp=walletListenerService.findByAddress(address,id);
        if (temp!=null){
            if (groupInfoSetting.getEnglish()){
                accountBot.sendMessage(sendMessage,address+"已设置入款!无需重复设置");
            }else {
                accountBot.sendMessage(sendMessage,address+" <b>Deposit has been set up! No need to set it up again</b>");
            }
        }else {
            WalletListener walletListener = new WalletListener();
            List<WalletListener> walletListeners=walletListenerService.queryAll(id+"");
            walletListener.setAddress(address);
            walletListener.setCreateTime(new Date());
            walletListener.setUserId(id+"");
            walletListener.setNickname(walletListeners.size()+1+"");
            walletListenerService.createWalletListener(walletListener);
            if (groupInfoSetting.getEnglish()){
                accountBot.sendMessage(sendMessage,"✔\uFE0F设置监听成功");
            }else {
                accountBot.sendMessage(sendMessage,"✔\uFE0F Set up listening successfully");
            }

        }
    }
    public void CallbackQuery(Message message, SendMessage sendMessage,  Rate rate, Update update) {
        String payMethod="0";//0所有 1银行卡  2支付宝 3微信
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData(); // 获取回调数据
            String text = update.getCallbackQuery().getMessage().getText();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId(); // 获取消息ID
            List<WalletListener> walletListeners = walletListenerService.queryAll(chatId+"");
            Map<String, String> nicknameToAddressMap = walletListeners.stream().filter(Objects::nonNull).collect(Collectors.toMap(
                    WalletListener::getNickname, WalletListener::getAddress));
            List<String> nicknameList = walletListeners.stream().filter(Objects::nonNull).map(WalletListener::getNickname).collect(Collectors.toList());
            sendMessage.setChatId(String.valueOf(chatId));
            GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", chatId));
            if ("查询交易记录".equals(callbackData)){
                this.queryHistoryTrading(update.getCallbackQuery().getMessage(),sendMessage,groupInfoSetting);
                return;
            }else if ("监听该地址".equals(callbackData)){
                this.walletListenerAddress(update.getCallbackQuery().getMessage(),sendMessage,groupInfoSetting);
                return;
            } else if (nicknameToAddressMap.values().contains(callbackData)) {
                this.getWallerListener(update.getCallbackQuery().getMessage(),sendMessage,callbackData,nicknameToAddressMap,groupInfoSetting);
                return;

            } else if (callbackData.startsWith("cancelOrder")) {
                String userId = callbackData.replace("cancelOrder", "");
                configEditHandler.cancelOrder(userId);
                return;
            } else if (callbackData.startsWith("confirm_broadcast_")) {
                String userId = callbackData.replace("confirm_broadcast_", "");
                List<UserNormal> userNormals = userNormalMapper.selectList(new QueryWrapper<UserNormal>().eq("user_id", userId));
                if (userNormals.isEmpty()) {
                    return;
                }
                for (UserNormal userNormal : userNormals){
                    handleConfirmBroadcast(userNormal.getGroupId(), update.getCallbackQuery().getMessage().getReplyToMessage());
                }
                if (groupInfoSetting.getEnglish()) {
                    accountBot.sendMessage(sendMessage,"群发已结束!");
                }else{
                    accountBot.sendMessage(sendMessage,"The group message has ended!");
                }
                accountBot.nowDeleteMessage(Long.valueOf(userId), messageId);
                return;
            } else if (callbackData.startsWith("admin_cancel_broadcast_")) {
                String userId = callbackData.replace("admin_cancel_broadcast_", "");
                accountBot.nowDeleteMessage(Long.valueOf(userId), messageId);
                return;
            } else if (callbackData.startsWith("admin_confirm_broadcast_")) {
                String userId = callbackData.replace("admin_confirm_broadcast_", "");
                List<Status> statuses = statusMapper.selectList(null);
                if (statuses.isEmpty()) {
                    return;
                }
                for (Status status : statuses){
                    handleConfirmBroadcast(status.getGroupId(), update.getCallbackQuery().getMessage().getReplyToMessage());
                }
                if (groupInfoSetting.getEnglish()) {
                    accountBot.sendMessage(sendMessage,"群发已结束!");
                }else{
                    accountBot.sendMessage(sendMessage,"The group message has ended!");
                }
                accountBot.nowDeleteMessage(Long.valueOf(userId), messageId);
                return;
            } else if (callbackData.startsWith("cancel_broadcast_")) {
                String userId = callbackData.replace("cancel_broadcast_", "");
                accountBot.nowDeleteMessage(Long.valueOf(userId), messageId);
                return;
            } else if (callbackData.startsWith("ButtonId:")) {
                String buttonId = callbackData.replace("ButtonId:", "");
                configEditHandler.sendButtonMessage(buttonId,chatId,groupInfoSetting);
                return;
            } else if (callbackData.equals("取消通知")) {
                // 找到第一个左括号和右括号的位置
                int start = text.indexOf('(');
                int end = text.indexOf(')', start + 1);
                // 提取括号内的内容
                String address = text.substring(start + 1, end).trim();
                List<WalletListener> collect = walletListeners.stream().filter(Objects::nonNull).filter(
                        n -> n.getAddress().equals(address) && n.getUserId().equals(chatId + "")).collect(Collectors.toList());
                collect.stream().filter(Objects::nonNull).forEach(wallet->walletListenerService.deleteWalletListener(wallet));
                if (groupInfoSetting.getEnglish()){
                    accountBot.sendMessage(sendMessage,"❗\uFE0F 取消地址通知成功。");
                }else {
                    accountBot.sendMessage(sendMessage,"❗\uFE0F Cancel address notification successful.");
                }
                return;
            }else if (callbackData.equals("设置名称")) {
                // 找到第一个左括号和右括号的位置
                int start = text.indexOf('(');
                int end = text.indexOf(')', start + 1);
                // 提取括号内的内容
                String address = text.substring(start + 1, end).trim();
                String result;
                if (groupInfoSetting.getEnglish()){
                    result="⁉\uFE0F 为您的每一个钱包设置单独的名字，方便您进行多钱包监听并识别\n" +
                            "\n" +
                            "\uD83D\uDCB3|"+address+"\n" +
                            "                \n" +
                            "接下来 复制您的钱包地址 回复 如下消息 即可修改您的钱包地址备注\n" +
                            "## 后面的就是您钱包地址的新备注\n" +
                            "\n" +
                            "                    \n" +
                            "如："+address+"##钱多多";
                }else {
                    result="⁉\uFE0F Set a separate name for each of your wallets to facilitate multi-wallet monitoring and identification\n" +
                            "\n" +
                            "\uD83D\uDCB3|"+address+"\n" +
                            "                \n" +
                            "Next, copy your wallet address and reply to the following message to modify your wallet address note\n" +
                            "## The following is the new note of your wallet address\n" +
                            "\n" +
                            "                    \n" +
                            "example："+address+"##Lots of money";
                }
                accountBot.sendMessage(sendMessage,result);
                return;
            }else if (callbackData.equals("查询余额")) {
                // 找到第一个左括号和右括号的位置
                int start = text.indexOf('(');
                int end = text.indexOf(')', start + 1);
                // 提取括号内的内容
                String address = text.substring(start + 1, end).trim();
                String url = tranAccountUrl+address;
                TronAccountDTO tronAccount = restTemplateConfig.getForObjectTronAccount(url);

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
                String usdt;
                String trx;
                if (bigDecimal.get()!=null){
                    if (groupInfoSetting.getEnglish()){
                        usdt="\uD83D\uDCB0 余额: "+bigDecimal+" U\n";
                    }else {
                        usdt="\uD83D\uDCB0 Balance: "+bigDecimal+" U\n";
                    }
                }else {
                    if (groupInfoSetting.getEnglish()){
                        usdt="\uD83D\uDCB0 余额: 0 U\n";
                    }else {
                        usdt="\uD83D\uDCB0 Balance: 0 U\n";
                    }
                }
                if (trxBigDecimal.get()!=null){
                    trx="\uD83D\uDCB0 "+trxBigDecimal+" TRX\n";
                }else {
                    trx ="\uD83D\uDCB0 : 0 TRX\n";
                }
                String t1;
                if (groupInfoSetting.getEnglish()){
                    t1="地址";
                }else {
                    t1="Address";
                }
                String result="➖➖➖➖➖➖➖➖➖➖➖\n" +
                        "✉\uFE0F "+t1+"： "+tronAccount.getAddress()+"\n" +
                        usdt+trx+
                        "➖➖➖➖➖➖➖➖➖➖➖";
                accountBot.sendMessage(sendMessage,result);
                return;
            }
            if (callbackData.equals("银行卡")) {
                payMethod="银行卡";
            } else if (callbackData.equals("所有")) {
                payMethod="所有";
            } else if (callbackData.equals("微信")) {
                payMethod="微信";
            } else if (callbackData.equals("支付宝")) {
                payMethod="支付宝";
            }
            String string = this.fetchRealTimeUSDTPriceFromOKX("");
            String result;
            if (groupInfoSetting.getEnglish()){
                result = "欧易网商家实时交易汇率top10\n" +
                        string + "\n" +
                        "本群费率：" + rate.getRate() + "%\n" +
                        "本群汇率：" + rate.getExchange();
            }else{
                result = "Real-time exchange rate for merchants on OUYI top10\n" +
                        string + "\n" +
                        "This group rate：" + rate.getRate() + "%\n" +
                        "Exchange rate of this group：" + rate.getExchange();
            }

            ButtonList buttonList = new ButtonList();
            EditMessageText editMessage = new EditMessageText();
            buttonList.editText(editMessage,chatId+"",payMethod,groupInfoSetting);
            // 更新消息文本
            accountBot.editMessageText(editMessage,chatId, messageId, result);
        }
    }

    /**
     * 确定发送的逻辑
     * @param chatId  这个
     */
    private void handleConfirmBroadcast(String chatId, Message originalMessage) {
        try {
            // 调用 AccountBot 发送广播消息
            if (originalMessage.hasText()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText(originalMessage.getText());
                sendMessage.setParseMode("HTML");
                accountBot.sendMessage(sendMessage);
            } else if (originalMessage.hasPhoto()) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(originalMessage.getPhoto().get(0).getFileId())); //不应该只第一个发送文本消息
                if (originalMessage.getCaption() != null) {
                    sendPhoto.setCaption(originalMessage.getCaption());
                    sendPhoto.setParseMode("HTML");
                }
                accountBot.sendPhone(sendPhoto, false, null);
            } else if (originalMessage.hasVideo()) {
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setVideo(new InputFile(originalMessage.getVideo().getFileId()));
                if (originalMessage.getCaption() != null) {
                    sendVideo.setCaption(originalMessage.getCaption());
                    sendVideo.setParseMode("HTML");
                }
                accountBot.sendVideo(sendVideo, false, null);
            } else if (originalMessage.getMediaGroupId()!=null) {
                SendMediaGroup mediaGroup = new SendMediaGroup();
                mediaGroup.setChatId(chatId);
                List<InputMedia> mediaList = new ArrayList<>();
                for (PhotoSize photo : originalMessage.getPhoto()) {
                    InputMediaPhoto inputMedia = new InputMediaPhoto();
                    inputMedia.setMedia(photo.getFileId());
                    if (mediaList.isEmpty() && originalMessage.getCaption() != null) {
                        inputMedia.setCaption(originalMessage.getCaption());
                        inputMedia.setParseMode("HTML");
                    }
                    mediaList.add(inputMedia);
                }
                mediaGroup.setMedias(mediaList);
                accountBot.sendMediaGroup(mediaGroup);
            } else {
                log.warn("不支持的消息类型");
            }
        } catch (Exception e) {
            log.error("发送广播消息失败", e);
        }
    }


    private void getWallerListener(Message message, SendMessage sendMessage, String callbackData,Map<String, String> map1,GroupInfoSetting groupInfoSetting) {
        String urls = tranHistoryUrl+callbackData;//callbackData这里的key昵称
        List<TronHistoryDTO> tradingList=restTemplateConfig.getForObjectHistoryTrading(urls,TronHistoryDTO.class);
        String result;
        if (groupInfoSetting.getEnglish()){
            result="\uD83D\uDD30 ("+callbackData+")共:("+tradingList.size()+")笔交易\n";
        }else {
            result="\uD83D\uDD30 ("+callbackData+")count:("+tradingList.size()+")Transactions\n";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(result);
        for (int i = 0; i < tradingList.size(); i++) {
            stringBuilder.append("\n" +
                    " ");
            BigDecimal balance = new BigDecimal(tradingList.get(i).getQuant());
            // 计算移动小数点后的 balance
            BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(tradingList.get(i).getTokenInfo().getTokenDecimal()));
            if (groupInfoSetting.getEnglish()){
                String type=tradingList.get(i).getTo_address().equals(callbackData)==true?"入账":"出账";
                stringBuilder.append("TXID :"+tradingList.get(i).getTransaction_id()+"\n");
                stringBuilder.append("收款人 :\uD83E\uDD16 \n监控钱包：("+tradingList.get(i).getTo_address()+")\n");
                stringBuilder.append("付款人 :"+tradingList.get(i).getFrom_address()+"\n");
                stringBuilder.append("类型 :"+type+"\n");
                stringBuilder.append("币种 :"+tradingList.get(i).getTokenInfo().getTokenAbbr()+"\n");
                stringBuilder.append("金额 :"+bigDecimal+"\n");
                stringBuilder.append("时间 :"+sdf.format(new Date(tradingList.get(i).getBlock_ts()))+"\n");
                stringBuilder.append("\n" +
                        " ");
            }else {
                String type=tradingList.get(i).getTo_address().equals(callbackData)==true?"Entry" : "Out";
                stringBuilder.append("TXID :"+tradingList.get(i).getTransaction_id()+"\n");
                stringBuilder.append("Payee :\uD83E\uDD16 \nMonitoring Wallet：("+tradingList.get(i).getTo_address()+")\n");
                stringBuilder.append("Payer :"+tradingList.get(i).getFrom_address()+"\n");
                stringBuilder.append("Type :"+type+"\n");
                stringBuilder.append("Currency :"+tradingList.get(i).getTokenInfo().getTokenAbbr()+"\n");
                stringBuilder.append("Amount :"+bigDecimal+"\n");
                stringBuilder.append("Time :"+sdf.format(new Date(tradingList.get(i).getBlock_ts()))+"\n");
                stringBuilder.append("\n" +
                        " ");
            }
        }
        ButtonList buttonList=new ButtonList();
        Map<String, String> map = new HashMap<>();
        if (groupInfoSetting.getEnglish()){
            map.put("设置名称","设置名称");
            map.put("取消通知","取消通知");
            map.put("查询余额","查询余额");
        }else  {
            map.put("Set Name","设置名称");
            map.put("Cancel Notice","取消通知");
            map.put("Check balance","查询余额");
        }
        buttonList.sendButton(sendMessage,message.getChatId().toString(),map);
        accountBot.sendMessage(sendMessage,stringBuilder.toString());
    }


    public void getNowExchange(SendMessage sendMessage, UserDTO userDTO, Rate rate,GroupInfoSetting groupInfoSetting) {
        String string = this.fetchRealTimeUSDTPriceFromOKX("");
        String result;
        if (groupInfoSetting.getEnglish()){
            result= "欧易商家实时交易汇率top10\n" +
                    string+ "\n" +
                    "本群费率："+rate.getRate().stripTrailingZeros().toPlainString()+"\n" +
                    "本群汇率："+rate.getExchange().stripTrailingZeros().toPlainString();

        }else{
            result = "Real-time exchange rate for merchants on OUYI top10\n" +
                    string + "\n" +
                    "This group rate：" + rate.getRate().stripTrailingZeros().toPlainString() + "\n" +
                    "Exchange rate of this group：" + rate.getExchange().stripTrailingZeros().toPlainString();
        }
        ButtonList buttonList = new ButtonList();
        buttonList.exchangeList(sendMessage,userDTO.getGroupId(),groupInfoSetting);
        accountBot.sendMessage(sendMessage,result);
    }
    private String fetchRealTimeUSDTPriceFromOKX(String type) {
        String url = "https://www.okx.com/v3/c2c/tradingOrders/books" +
                "?quoteCurrency=cny&baseCurrency=usdt&side=sell&paymentMethod="+type+"&userType=all&limit=10";
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                JSONObject data1 = json.getJSONObject("data");
                JSONArray data = data1.getJSONArray("sell");
                StringBuilder result = new StringBuilder("\n");
                for (int i = 0; i < data.size(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    String price = item.getStr("price");
                    String name = item.getStr("nickName");
                    result.append(String.format("%d.) %s %s\n", i + 1, price, name));
                }
                return result.toString();
            } else {
                return "请求失败：" + response.code();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }



}
