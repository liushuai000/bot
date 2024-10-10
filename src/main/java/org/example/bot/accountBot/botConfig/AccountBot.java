package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.Issue;

import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class AccountBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.token}")
    protected String botToken;
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    protected RateService rateService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected IssueService issueService;
    @Autowired
    protected AccountService accountService;
    @Autowired
    protected Utils utils;
    @Autowired
    protected DateOperator dateOperator; //操作时间
    @Autowired
    protected SettingOperatorPerson settingOperatorPerson;   //设置操作人员
    @Autowired
    protected ShowOperatorName showOperatorName;   //显示操作人名字
    //按钮
    ButtonList buttonList = new ButtonList();
    @Autowired
    protected RuzhangOperations ruzhangOperations;    //入账和入账时发送的消息

    @Override
    public String getBotUsername() {
        return username;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }
    @Override
    public void onUpdateReceived(Update update) {
        String replyToText=null;
        if (update != null && update.getMessage() != null && update.getMessage().getReplyToMessage() != null) {
            replyToText = update.getMessage().getReplyToMessage().getText();
            if (replyToText != null) {
                log.info("ReplyToText: {}", replyToText);
            }
        }
        //接收消息
        assert update != null;
        Message message = update.getMessage();
        //回复人的名称
        String callBackName = null;
        //回复人的昵称
        String callBackFirstName=null;
        if (update.getMessage() != null && update.getMessage().getFrom() != null &&
                update.getMessage().getReplyToMessage() != null && update.getMessage().getReplyToMessage().getFrom() != null) {
            callBackName = update.getMessage().getReplyToMessage().getFrom().getUserName();  // 确保 userName 不为 null
            callBackFirstName = update.getMessage().getReplyToMessage().getFrom().getFirstName();  // 确保 userName 不为 null
            if (callBackName == null) {
                callBackName = "No username"; // 或其他适当的默认值
            }
        }
        log.info("callBackName,callBackFirstName: {},{}", callBackName,callBackFirstName);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        if (update.hasMessage() && update.getMessage().hasText()) this.BusinessHandler(message,sendMessage,callBackName,callBackFirstName,replyToText);
    }

    public void BusinessHandler(Message message,SendMessage sendMessage,String callBackName,String callBackFirstName,String replyToText) {
        String firstName = message.getFrom().getFirstName();
        String userName = message.getFrom().getUserName();
        //判断是否为管理员
        List<User> userList = userService.selectAll();
        if (!userList.stream().anyMatch(user -> Objects.equals(user.getUsername(), userName))){
            this.sendMessage(sendMessage,"不是管理 请联系管理员!");
            return;
        }
        String[] split1 = message.getText().split(" ");
        String[] split2 = message.getText().split("\\+");
        String[] split3 = message.getText().split("-");
        //初始化
        Rate rate=rateService.getInitRate();
        Account updateAccount = new Account();
        Issue issue=new Issue();
        //搜索出历史账单/判断是否过期
        List<Account> accountList=dateOperator.isOver24Hour(message,sendMessage);
        //搜索出历史下发订单/判断是否过期
        List<Issue> issueList =dateOperator.issueIsOver24Hour(message,sendMessage);
        //设置操作人员
        settingOperatorPerson.setHandle(split1, userName,firstName, userService.selectAll(), sendMessage, message,callBackName,callBackFirstName,message.getText());
        //设置费率/汇率
        ruzhangOperations.setRate(message,sendMessage,rate);
        //撤销入款
        ruzhangOperations.repeal(message,sendMessage,accountList,replyToText,callBackName,issueList);
        //入账操作
        ruzhangOperations.inHandle(split2,message.getText(),  updateAccount,  userName, sendMessage, accountList, message,split3,
                rate,callBackFirstName,callBackName, firstName,issue,issueList);
        //显示操作人名字
        showOperatorName.replay(sendMessage,updateAccount,rate,issueList,issue,message.getText());
        //删除操作人员
        settingOperatorPerson.deleteHandle(message.getText(),sendMessage);
        //设置记账时间 24小时制  设置13

//TODO

        //删除今日数据/关闭日切/
        dateOperator.deleteTodayData(message,sendMessage,accountList,replyToText);
        //计算器功能
        utils.counter(message,sendMessage);
        //通知功能
        inform(message.getText(),sendMessage);
    }

     //通知功能实现/48 小时内在群组发言过的所有人
    private void inform(String text, SendMessage sendMessage) {
        if (text.equals("通知")){
            List<String> users=accountService.inform(new Date());
            Set<String> set = new HashSet<>(users);
            List<String> uniqueUsers = new ArrayList<>(set);
            StringBuilder sb = new StringBuilder();
            sb.append("48 小时内在群组发言过的所有人: @");
            for (int i = 0; i < uniqueUsers.size(); i++) {
                sb.append(uniqueUsers.get(i));
                if (i < uniqueUsers.size() - 1) {
                    sb.append(" @");
                }
            }
            String usertest = sb.toString();
            sendMessage.setText(usertest);
            try {
                log.info("发送消息66");
                execute(sendMessage);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    protected void sendMessage(SendMessage sendMessage,String text) {
        sendMessage.setText(text);
        try {
            log.info("发送消息:{}", text);
            execute(sendMessage);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
 }


