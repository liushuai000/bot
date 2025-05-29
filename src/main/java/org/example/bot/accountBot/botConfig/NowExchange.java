package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.*;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.GroupInfoSetting;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.WalletListener;
import org.example.bot.accountBot.service.WalletListenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 获取最新汇率
 */

@Slf4j
@Service
public class NowExchange {

    @Value("${getExchangeUrl}")
    protected String url;
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

    // 缓存变量，使用Map存储不同支付方式的缓存结果
    private final Map<String, List<Merchant>> cachedMerchants = new ConcurrentHashMap<>();
    private final Map<String, String> cachedResults = new ConcurrentHashMap<>();

    // 定时任务调度器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;

    @PostConstruct
    public void init() {
        // 启动定时任务，首次立即执行，之后每隔20秒执行一次
        scheduler.scheduleAtFixedRate(this::fetchAndCacheData, 0, 10, TimeUnit.SECONDS);
    }

    public void Query(SendMessage sendMessage,Update update){
        String text = update.getMessage().getText();
        String substring = text.substring(2, text.length());
        Long groupId = update.getMessage().getChatId();
        if (StringUtils.isBlank(substring)){
            return;
        }
        String url = tranAccountUrl+substring;
        TronAccountDTO tronAccount = restTemplateConfig.getForObjectTronAccount(url);
        String htmlText = "[USDT余额](https://tronscan.org/#/address/"+ substring + "/transfers)";
        String trxText = "[TRX余额](https://tronscan.org/#/address/"+ substring + "/transfers)";
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
                substring+"\n" +
                "——————————\n" +
                usdt+ trx+"——————————\n" +
                "交易次数：" +tronAccount.getTransactions()+"\n"+
                "   -出："+tronAccount.getTransactions_out()+"\n"+
                "   -入："+tronAccount.getTransactions_in()+"\n"+
                "地址创建时间："+sdf.format(new Date(tronAccount.getDate_created()))+"\n"+
                "最新交易时间："+sdf.format(new Date(tronAccount.getLatest_operation_time()))+"\n";
        accountBot.tronAccountMessageText(sendMessage,groupId+"",result);
    }

    // 定时任务方法，用于获取最新数据并缓存
    private void fetchAndCacheData() {
        try {
            List<String> payMethodList=new ArrayList();
            payMethodList.add("0");// 所有
            payMethodList.add("1");// 银行卡
            payMethodList.add("2");// 支付宝
            payMethodList.add("3");// 微信
            for (String payMethod : payMethodList){
                String url = this.url + "?coinId=2&currency=172&tradeType=sell&currPage=1&payMethod=" + payMethod +
                        "&acceptOrder=0&country=&blockType=general&online=1&range=0&amount=&isThumbsUp=false&isMerchant=false" +
                        "&isTraded=false&onlyTradable=false&isFollowed=false&makerCompleteRate=0";
                List<Merchant> merchants = restTemplateConfig.getForObjectMerchant(url, NowExchangeDTO.class);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < merchants.size(); i++) {
                    stringBuilder.append(i + 1).append(")   ").append(merchants.get(i).getPrice()).append("   ").append("<code>").append(merchants.get(i).getUserName()).append("</code>").append("\n");
                }
                // 更新缓存
                cachedMerchants.put(payMethod, merchants);
                cachedResults.put(payMethod, stringBuilder.toString());
            }
        } catch (Exception e) {
            log.error("Error fetching and caching data", e);
        }
    }
    //查询交易记录
    private void queryHistoryTrading(Message message,SendMessage sendMessage) {
        String text = message.getText();
        String substring = text.substring(4, text.indexOf("——————————")).trim();
        String url = tranHistoryUrl+substring;
        String result = "|    时间    |类型|   金额  |地址   \n" +
                "|-----------|----|--------|---- \n";
        List<TronHistoryDTO> tradingList=restTemplateConfig.getForObjectHistoryTrading(url,TronHistoryDTO.class);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(result);
        for (int i = 0; i < tradingList.size(); i++) {
            BigDecimal balance = new BigDecimal(tradingList.get(i).getQuant());
            // 计算移动小数点后的 balance
            BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(tradingList.get(i).getTokenInfo().getTokenDecimal()));
            String type=tradingList.get(i).getTo_address().equals(substring)==true?"入账":"出账";
            stringBuilder.append("|"+sdf.format(new Date(tradingList.get(i).getBlock_ts()))+"|"+type+"|"+bigDecimal+"|"+tradingList.get(i).getTokenInfo().getTokenAbbr()+"|"+"\n");
        }
        accountBot.sendMessage(sendMessage,stringBuilder.toString());
    }
    private void walletListenerAddress(Message message, SendMessage sendMessage) {
        Long id = message.getChat().getId();
        String text = message.getText();
        String address = text.substring(4, text.indexOf("——————————")).trim();
        WalletListener temp=walletListenerService.findByAddress(address,id);
        if (temp!=null){
            accountBot.sendMessage(sendMessage,address+"已设置入款!无需重复设置");
        }else {
            WalletListener walletListener = new WalletListener();
            List<WalletListener> walletListeners=walletListenerService.queryAll(id+"");
            walletListener.setAddress(address);
            walletListener.setCreateTime(new Date());
            walletListener.setUserId(id+"");
            walletListener.setNickname(walletListeners.size()+1+"");
            walletListenerService.createWalletListener(walletListener);
            accountBot.sendMessage(sendMessage,"✔\uFE0F设置监听成功");
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
            if ("查询交易记录".equals(callbackData)){
                this.queryHistoryTrading(update.getCallbackQuery().getMessage(),sendMessage);
                return;
            }else if ("监听该地址".equals(callbackData)){
                this.walletListenerAddress(update.getCallbackQuery().getMessage(),sendMessage);
                return;
            } else if (nicknameToAddressMap.values().contains(callbackData)) {
                this.getWallerListener(update.getCallbackQuery().getMessage(),sendMessage,callbackData,nicknameToAddressMap);
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
                accountBot.sendMessage(sendMessage,"❗\uFE0F 取消地址通知成功。");
                return;
            }else if (callbackData.equals("设置名称")) {
                // 找到第一个左括号和右括号的位置
                int start = text.indexOf('(');
                int end = text.indexOf(')', start + 1);
                // 提取括号内的内容
                String address = text.substring(start + 1, end).trim();
                String result="⁉\uFE0F 为您的每一个钱包设置单独的名字，方便您进行多钱包监听并识别\n" +
                        "\n" +
                        "\uD83D\uDCB3|"+address+"\n" +
                        "                \n" +
                        "接下来 复制您的钱包地址 回复 如下消息 即可修改您的钱包地址备注\n" +
                        "## 后面的就是您钱包地址的新备注\n" +
                        "\n" +
                        "                    \n" +
                        "如："+address+"##钱多多";
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
                    usdt="\uD83D\uDCB0 余额: "+bigDecimal+" U\n";
                }else {
                    usdt="\uD83D\uDCB0 余额: 0 U\n";
                }
                if (trxBigDecimal.get()!=null){
                    trx="\uD83D\uDCB0 "+trxBigDecimal+" TRX\n";
                }else {
                    trx ="\uD83D\uDCB0 : 0 TRX\n";
                }
                String result="➖➖➖➖➖➖➖➖➖➖➖\n" +
                        "✉\uFE0F 地址： "+tronAccount.getAddress()+"\n" +
                        usdt+trx+
                        "➖➖➖➖➖➖➖➖➖➖➖";
                accountBot.sendMessage(sendMessage,result);
                return;
            }
            if (callbackData.equals("银行卡")) {
                payMethod="1";
            } else if (callbackData.equals("所有")) {
                payMethod="0";
            } else if (callbackData.equals("微信")) {
                payMethod="3";
            } else if (callbackData.equals("支付宝")) {
                payMethod="2";
            }
            // 使用缓存的数据
            String result = "火币网商家实时交易汇率top10\n" +
                    cachedResults.get(payMethod).toString() + "\n" +
                    "本群费率：" + rate.getRate() + "%\n" +
                    "本群汇率：" + rate.getExchange();

            ButtonList buttonList = new ButtonList();
            EditMessageText editMessage = new EditMessageText();
            GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", chatId));
            buttonList.editText(editMessage,chatId+"",payMethod,groupInfoSetting);
            // 更新消息文本
            accountBot.editMessageText(editMessage,chatId, messageId, result);
        }
    }

    private void getWallerListener(Message message, SendMessage sendMessage, String callbackData,Map<String, String> map1) {
        String urls = tranHistoryUrl+callbackData;//callbackData这里的key昵称
        List<TronHistoryDTO> tradingList=restTemplateConfig.getForObjectHistoryTrading(urls,TronHistoryDTO.class);
        String result="\uD83D\uDD30 ("+callbackData+")共:("+tradingList.size()+")笔交易\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(result);
        for (int i = 0; i < tradingList.size(); i++) {
            stringBuilder.append("\n" +
                    " ");
            BigDecimal balance = new BigDecimal(tradingList.get(i).getQuant());
            // 计算移动小数点后的 balance
            BigDecimal bigDecimal = balance.divide(BigDecimal.TEN.pow(tradingList.get(i).getTokenInfo().getTokenDecimal()));
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
        }
        ButtonList buttonList=new ButtonList();
        Map<String, String> map = new HashMap<>();
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", message.getChatId().toString()));
        map.put("设置名称","设置名称");
        map.put("取消通知","取消通知");
        map.put("查询余额","查询余额");
        buttonList.sendButton(sendMessage,message.getChatId().toString(),map,groupInfoSetting);
        accountBot.sendMessage(sendMessage,stringBuilder.toString());
    }


    public void getNowExchange(Message message, SendMessage sendMessage, UserDTO userDTO, Rate rate, Update update) {
        List<Merchant> merchants = restTemplateConfig.getForObjectMerchant(url+"" +
                        "?coinId=2&currency=172&tradeType=sell" + "&currPage=1" + "&payMethod=0" +
                        "&acceptOrder=0&country=&blockType=general&online=1&range=0&amount=&isThumbsUp=false&isMerchant=false" +
                        "&isTraded=false&onlyTradable=false&isFollowed=false&makerCompleteRate=0" ,
                NowExchangeDTO.class);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < merchants.size(); i++) {
            stringBuilder.append(i+1).append(")   ").append(merchants.get(i).getPrice()).append("   ").append("<code>").append(merchants.get(i).getUserName()).append("</code>").append("\n");
        }
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getGroupId()));
        String result= "火币网商家实时交易汇率top10\n" +
                stringBuilder+ "\n" +
                "本群费率："+rate.getRate()+"%\n" +
                "本群汇率："+rate.getExchange();
        ButtonList buttonList = new ButtonList();
        buttonList.exchangeList(sendMessage,userDTO.getGroupId(),groupInfoSetting);
        accountBot.sendMessage(sendMessage,result);
    }
}
