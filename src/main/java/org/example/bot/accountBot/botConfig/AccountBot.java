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
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

@Slf4j
@Component
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
    /**
     * 入账操作2.0，匹配公式
     * 加法  用于matchFound 0 1
     * 加法2 用于matchFound 2 3
     * 减法
     * 是否匹配公式入账
     * 应下发计算公式：d=(total-(total*rate1))/exchange
     * 计算器的判断是否符合
     **/
    //初始化
    Utils utils;
    //操作时间
    DateOperator dateOperator;
    //设置操作人员
    SettingOperatorPerson settingOperatorPerson;
    //显示操作人名字
    ShowOperatorName showOperatorName;
    //按钮
    ButtonList buttonList;
    //入账和入账时发送的消息
    RuzhangOperations ruzhangOperations;
    private void init(){
        utils=new Utils();
        dateOperator=new DateOperator();
        settingOperatorPerson=new SettingOperatorPerson();
        showOperatorName=new ShowOperatorName();
        buttonList=new ButtonList();
        ruzhangOperations=new RuzhangOperations();
    }
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
        this.init();
        log.warn("收到消息：{}", update);
        //回复的消息
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
//            if (!userList.stream().anyMatch(user -> Objects.equals(user.getUsername(), userName))){
//                return;
//            }
        String[] split1 = message.getText().split(" ");
        String[] split2 = message.getText().split("\\+");
        String[] split3 = message.getText().split("-");
        //初始化
        Rate rate=rateService.getInitRate();
        Account updateAccount = new Account();
        Issue issue=new Issue();
        //搜索出历史账单/判断是否过期
        List<Account> accountList=isOver24Hour(message,sendMessage);
        //搜索出历史下发订单/判断是否过期
        List<Issue> issueList =issueIsOver24Hour(message,sendMessage);
        //设置操作人员
        setHandle(split1, userName,firstName, userService.selectAll(), sendMessage, message,callBackName,callBackFirstName,message.getText());
        //设置费率/汇率
        setRate(message,sendMessage,rate);
        //撤销入款
        repeal(message,sendMessage,accountList,replyToText,callBackName,issueList);
        //入账操作
        inHandle(split2, updateAccount,  userName, sendMessage, accountList, message,split3,
                rate,callBackFirstName,callBackName, firstName,issue,issueList);
        //显示操作人名字
        showOperatorName.replay(sendMessage,updateAccount,rate,issueList,issue,message.getText());
        //删除操作人员
        deleteHandle(message.getText(),sendMessage);
        //删除今日数据/关闭日切/
        deleteTodayData(message,sendMessage,accountList,replyToText);
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
    //获取并判断下发订单是否过期
    private List<Issue> issueIsOver24Hour(Message message, SendMessage sendMessage) {
        return dateOperator.issueIsOver24Hour(message,sendMessage);
    }



    //撤销入款
    private void repeal(Message message, SendMessage sendMessage, List<Account> list, String replyToText, String callBackName, List<Issue> issueList) {
        String text = message.getText();

        if (text.length()>=2||replyToText!=null){
            if (text.equals("撤销入款")){
                accountService.deleteInData(list.get(list.size()-1).getAddTime());
                issueService.updateIssueDown(list.get(list.size()-1).getDown());
                sendMessage.setText("撤销成功");
                try {
                    log.info("发送消息7");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("repeal异常");
                }
            }else if (text.equals("取消")&&replyToText!=null&&callBackName.equals("chishui_id")){
                log.info("replyToXXXTentacion:{}",replyToText);
                if (replyToText.charAt(0)=='+'){
                    accountService.deleteInData(list.get(list.size()-1).getAddTime());
                    issueService.updateIssueDown(list.get(list.size()-1).getDown());
                }else if (replyToText.charAt(0)=='-'){
                    issueService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime());
                    accountService.updateNewestData(issueList.get(issueList.size()-1).getDown());
                }else {
                    return;
                }
                sendMessage.setText("取消成功");
                try {
                    log.info("发送消息7");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("repeal异常");
                }
            }else if (text.equals("撤销下发")){
                issueService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime());
                accountService.updateNewestData(issueList.get(issueList.size()-1).getDown());
                sendMessage.setText("撤销成功");
                try {
                    log.info("发送消息7");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("repeal异常");
                }
            }


        }

    }



    //删除操作人员
    private void deleteHandle(String text,SendMessage sendMessage) {
        log.info("text:{}",text);
        if (text.length()<4){
            return;
        }
        String[] split = text.split(" ");
        if (split[0].equals("删除操作员")||split[0].equals("删除操作人")){
            String deleteName = split[1].substring(1);
            log.info("删除操作员:{}",deleteName);
            userService.deleteHandler(deleteName);
            sendMessage.setText("删除成功");
            try {
                log.info("发送消息8");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    //设置费/汇率
    private void setRate(Message message,SendMessage sendMessage,Rate rates) {
        String text = message.getText();
        if (text.length()<4){return;}
        if (text.startsWith("设置费率")){
            String rate = text.substring(4);
            BigDecimal bigDecimal = new BigDecimal(rate);
            bigDecimal=bigDecimal.multiply(BigDecimal.valueOf(0.01));
            rates.setRate(bigDecimal);
            rates.setAddTime(new Date());
            log.info("rates:{}",rates);
            rateService.updateRate(String.valueOf(bigDecimal));
            sendMessage.setText("设置成功,当前费率为："+rate);
            try {
                log.info("发送消息9");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (text.startsWith("设置汇率")){
            rates.setExchange(new BigDecimal(text.substring(4)));
            rateService.updateExchange(rates.getExchange());
            sendMessage.setText("设置成功,当前汇率为："+text.substring(4));
            try {
                log.info("发送消息10");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (text.startsWith("设置入款单笔手续费")){

        }



    }

    //入账操作
    private void inHandle(String[] split2,Account updateAccount,  String userName, SendMessage sendMessage,
                          List<Account> accountList, Message message, String[] split3, Rate rate, String callBackFirstName, String callBackName,
                          String firstName, Issue issue, List<Issue> issueList) {
        ruzhangOperations.inHandle(split2,message.getText(), updateAccount,  userName, sendMessage, accountList, message,split3,
                rate,callBackFirstName,callBackName, firstName,issue,issueList);
    }

    //判断是否过期
    private List<Account> isOver24Hour(Message message, SendMessage sendMessage) {
        return dateOperator.isOver24Hour(message,sendMessage);
    }
    //删除今日数据/关闭日切
    private void deleteTodayData(Message message, SendMessage sendMessage, List<Account> accountList, String replyToText) {
        dateOperator.deleteTodayData(message,sendMessage,accountList,replyToText);
    }

    /**
     * 设置操作人员
     * @param split1 传输的文本 是否是 设置操作员
     * @param userName 用户名
     * @param firstName ????
     * @param userList 获取操作人列表
     * @param sendMessage 发生的消息
     * @param message 消息
     * @param callBackName 回复人的名称
     * @param callBackFirstName 回复人的昵称
     * @param text  消息文本
     */
    private void setHandle(String[] split1, String userName, String firstName, List<User> userList,
                           SendMessage sendMessage, Message message, String callBackName,
                           String callBackFirstName, String text) {
        settingOperatorPerson.setHandle(split1, userName,firstName, userService.selectAll(), sendMessage, message,callBackName,callBackFirstName,text);
    }
}


