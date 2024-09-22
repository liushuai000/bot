package org.example.bot.accountBot.botConfig;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.Issue;

import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.accService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class accountBot extends TelegramLongPollingBot {

    private accService accService;
    private Date oldSetTime;
    boolean Over24Hour=false;
    //填你自己的token和username
    private String token ="7174037873:AAFvDejFixb94JY66NHSr_ROB6fXG4LioRs";
    private String username ="zqzs18bot";

    public accountBot() {
        this( new DefaultBotOptions());
    }

    public accountBot(DefaultBotOptions options) {
        super(options);
    }
    @Override
    public String getBotToken() {
        return this.token;
    }
    @Override
    public String getBotUsername() {
        return this.username;
    }

    @Override
    public void onUpdateReceived(Update update) {
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
        Message message = update.getMessage();
        //回复人的名称
        String callBackName = null;
        //回复人的昵称
        String callBackFirstName=null;
        if (update.getMessage() != null &&
                update.getMessage().getFrom() != null &&
                update.getMessage().getReplyToMessage() != null &&
                update.getMessage().getReplyToMessage().getFrom() != null) {
            callBackName = update.getMessage().getReplyToMessage().getFrom().getUserName();  // 确保 userName 不为 null
            callBackFirstName = update.getMessage().getReplyToMessage().getFrom().getFirstName();  // 确保 userName 不为 null
            if (callBackName == null) {
                callBackName = "No username"; // 或其他适当的默认值
            }
        }
        log.info("callBackName,callBackFirstName: {},{}", callBackName,callBackFirstName);
        if (update.hasMessage() && update.getMessage().hasText()) {
            //获取操作人列表
            List<User> userList = accService.selectAll();
            String firstName = message.getFrom().getFirstName();
            String userName = message.getFrom().getUserName();
            //判断是否为管理员
//            if (!userList.stream().anyMatch(user -> Objects.equals(user.getUsername(), userName))){
//                return;
//            }
            String[] split1 = message.getText().split(" ");
            String[] split2 = message.getText().split("\\+");
            String[] split3 = message.getText().split("-");
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            Rate rate=new Rate();
            rate.setAddTime(new Date());
            //查询Rate
            if (accService.selectRate()!=null){
                rate=accService.selectRate();
                log.info("rates:{}",rate);
            }else {
                Long overdue=3153600000000l;
                rate.setOverDue(overdue);
                rate.setHandlestatus(1);
                rate.setCallBackStatus(1);
                rate.setDetailStatus(1);
                accService.insertRate(rate);
            }
            //搜索出历史账单/判断是否过期
            List<Account> accountList=isOver24Hour(message,sendMessage);
            //搜索出历史下发订单/判断是否过期
            List<Issue> issueList =issueIsOver24Hour(message,sendMessage);
            BigDecimal total=new BigDecimal(0);
            BigDecimal down = new BigDecimal(0);
            Account updateAccount = new Account();
            Issue issue=new Issue();

            //设置操作人员
            setHandle(split1, userName,firstName, userList, sendMessage, message,callBackName,callBackFirstName,message.getText());

            //设置费率/汇率
            setRate(message,sendMessage,rate);
            //撤销入款
            repeal(message,sendMessage,accountList,replyToText,callBackName,issueList);

            //入账操作
            inHandle(split2,message.getText(), updateAccount, total, userName, down, sendMessage, accountList, message,split3,
                    rate,callBackFirstName,callBackName, firstName,issue,issueList);
            //显示操作人名字
            replay(sendMessage,updateAccount,rate,issueList,issue,message.getText());
            //删除操作人员
            deleteHandle(message.getText(),sendMessage);
            //删除今日数据/关闭日切/
            deleteTedayData(message,sendMessage,accountList,replyToText);

            //计算器功能
            counter(message,sendMessage);
            //通知功能
            inform(message.getText(),sendMessage);

        }
    }


    //显示操作人名字
    public void  replay(SendMessage sendMessage,Account updateAccount, Rate rate,  List<Issue> issuesList, Issue issue, String text) {
        if (!text.equals("显示操作人名字")){
            return;
        }
        String iusseText="";
        //重新获取最新的数据
        List<Account> accounts = accService.selectAccount();
        List<String> newList = new ArrayList<>();
        List<String> newIssueList=new ArrayList<>();
        for (Account account : accounts) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newList.add(sdf.format(account.getAddTime()));
        }
        //已出账
        BigDecimal num = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
//        BigDecimal num = new BigDecimal(0);
//        if (!isOrNo(text)){
//            //当不是公式入账时才赋值
//            num=new BigDecimal(text.substring(1));
//        }

        List<Issue> issues = accService.selectIssue();
        log.info("issues,,{}",issues);
        //获取时间数据方便后续操作

        for (Account account : accounts) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newList.add(sdf.format(account.getAddTime()));
        }
        for (Issue issue1 : issues) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newIssueList.add(sdf.format(issue1.getAddTime()));
        }
        //显示操作人
        if (issuesList.size()==1){
            //显示操作人 rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+
            String text1 = issuesList.get(issuesList.size()-1).getHandleFirstName();
            //显示明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            //显示回复人
            String callBackFirstName= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCall_back()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text2+text1+" "+callBackFirstName+accounts.get(0).getHandleFirstName();
        }else if (issuesList.size()==2){
            //操作人的显示状态，1表示不显示，0表示显示    操作人昵称
            String handleFirstName = issuesList.get(issuesList.size()-1).getHandleFirstName();
            //handleFirstName rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+
            String handleFirstName2 = issuesList.get(issuesList.size()-1).getHandleFirstName();
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text31= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCall_back()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String text32= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCall_back()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text21+handleFirstName+text31+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+text22+handleFirstName2+text32;
        } else {
            if (updateAccount.getDown()!=null){
                issue.setDown(updateAccount.getDown());
            }
            issue.setDown(BigDecimal.ZERO);
            iusseText="\n\n" +"已下发：\n"+
                    "暂无下发数据";
        }
        List<Account> allAccount = accService.selectAccount();
        //显示操作人/显示1明细
        if (accounts.size()==1){
            //是否隐藏操作人
            String text1 = rate.getHandlestatus() == 0 ? " @"+accounts.get(accounts.size()-1).getHandle()+" "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            updateAccount=allAccount.get(0);
            //是否隐藏明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text3 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCall_back()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            iusseText="\n已入账："+num +"，共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(accounts.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text2+text1+text3+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP);
        }else if (accounts.size()==2){
            updateAccount=allAccount.get(1);
            Account accountFirsit = allAccount.get(0);
            //是否隐藏操作人
            String text11 = rate.getHandlestatus() == 0 ? " @"+accounts.get(accounts.size()-1).getHandle()+" "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+accounts.get(accounts.size()-2).getHandle()+" "+accounts.get(accounts.size()-2).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCall_back()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getCall_back()+" "+accounts.get(accounts.size()-2).getCallBackFirstName() : "";
            iusseText="\n已入账："+num +"，:共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(accounts.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    accounts.get(accounts.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP);
        }else if (accounts.size()>2){
           //取所有账户总和
            BigDecimal total =  allAccount.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downing =  allAccount.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal down =  allAccount.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal issuesDown = issuesList.stream().filter(Objects::nonNull).map(Issue::getDown).reduce(BigDecimal.ZERO, BigDecimal::add);
            //是否隐藏操作人
            String text11 = rate.getHandlestatus() == 0 ? " @"+accounts.get(accounts.size()-1).getHandle()+" "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+accounts.get(accounts.size()-2).getHandle()+" "+accounts.get(accounts.size()-2).getHandleFirstName() : "";
            String text13 = rate.getHandlestatus() == 0 ? " @"+accounts.get(accounts.size()-3).getHandle()+" "+accounts.get(accounts.size()-3).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text23 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-3).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCall_back()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getCall_back()+" "+accounts.get(accounts.size()-2).getCallBackFirstName() : "";
            String text33 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-3).getCall_back()+" "+accounts.get(accounts.size()-3).getCallBackFirstName() : "";
            iusseText="\n已入账："+num +"，:共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(accounts.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    accounts.get(accounts.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+
                    newList.get(newList.size()-3)+" "+
                    accounts.get(accounts.size()-3).getTotal().setScale(2, RoundingMode.HALF_UP)+text23+text13+text33+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ total.setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ downing.setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issuesDown.setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ down.setScale(2, RoundingMode.HALF_UP);
        } else {
            iusseText="入账："+ num ;
        }



        sendMessage.setText(iusseText);
        //        String sendText1 = getSendText(updateAccount, accounts,rate, num, newList,newIssueList,issues,issue,text);
        try {
            log.info("发送消息1");
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //通知功能实现/48 小时内在群组发言过的所有人
    private void inform(String text, SendMessage sendMessage) {
        if (text.equals("通知")){
            List<String> users=accService.inform(new Date());
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
        List<Issue> list=accService.selectIssue();
        int i=8;
        Date setTime=new Date();
        if (list.size()>0){
            oldSetTime=list.get(list.size()-1).getSetTime();
            setTime=list.get(list.size()-1).getSetTime();
        }
        log.info("setTime;;;{}",setTime);
        if (accService.selectIssue().size()!=0){
            list=accService.selectIssue();
            //获取设置当天的时间
            long diff =list.get(list.size()-1).getAddTime().getTime()-list.get(list.size()-1).getSetTime().getTime();
            //boolean over24hour=diff > 24 * 60 * 60 * 1000;
            Rate rate=accService.selectRate();
            boolean over24hour=diff >  rate.getOverDue();
            setTime = list.get(list.size()-1).getSetTime();

            if (over24hour){
                Over24Hour=true;
                accService.updateIssueDataStatus();
                Rate rate1=new Rate();
                accService.updateIssueSetTime(setTime);
                log.info("setTime,,:{}",setTime);
                accService.updateRate(String.valueOf(rate1.getRate()));
                accService.updateExchange(rate1.getExchange());
                log.info("over24hour---------:{}",over24hour);
                list=accService.selectIssue();
                log.info("listover24:{}",list);
            }
        }
        log.info("Over24Hour,,:{}",Over24Hour);

        setOver24Hour(Over24Hour);

        return list;

    }

    //计算器功能
    private void counter(Message message, SendMessage sendMessage) {
        try {
            String text = message.getText();
            String calculate = calculate(text);
            sendMessage.setText(calculate);
            try {
                log.info("发送消息6");
                execute(sendMessage);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }catch (Exception e){
            log.info("计算器功能异常");
        }


    }
    //计算器的判断是否符合+-*/
    private String calculate(String text) {
        // 正则表达式匹配形如 "数字 运算符 数字" 的模式
        Pattern pattern = Pattern.compile("^(\\d+)([+\\-*/])(\\d+)$");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // 解析数字
            int number1 = Integer.parseInt(matcher.group(1));
            int number2 = Integer.parseInt(matcher.group(3));
            char operator = matcher.group(2).charAt(0);

            // 计算结果
            int result = 0;
            switch (operator) {
                case '+':
                    result = number1 + number2;
                    break;
                case '-':
                    result = number1 - number2;
                    break;
                case '*':
                    result = number1 * number2;
                    break;
                case '/':
                    if (number2 != 0) {
                        result = number1 / number2;
                    } else {
                        return "Error: Division by zero";
                    }
                    break;
                default:
                    return "Invalid operator";
            }

            // 返回结果字符串
            return text + "=" + result;
        } else {
            // 如果不匹配，返回 null
            return null;
        }
    }

    //撤销入款
    private void repeal(Message message, SendMessage sendMessage, List<Account> list, String replyToText, String callBackName, List<Issue> issueList) {
        String text = message.getText();

        if (text.length()>=2||replyToText!=null){
            if (text.equals("撤销入款")){
                accService.deleteInData(list.get(list.size()-1).getAddTime());
                accService.updateissueDown(list.get(list.size()-1).getDown());
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
                    accService.deleteInData(list.get(list.size()-1).getAddTime());
                    accService.updateissueDown(list.get(list.size()-1).getDown());
                }else if (replyToText.charAt(0)=='-'){
                    accService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime());
                    accService.updateNewestData(issueList.get(issueList.size()-1).getDown());
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
                accService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime());
                accService.updateNewestData(issueList.get(issueList.size()-1).getDown());
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
            accService.deleteHandele(deleteName);
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
        if (text.length()<4){
            return;
        }

        if (text.substring(0,4).equals("设置费率")){
            String rate = text.substring(4);
            BigDecimal bigDecimal = new BigDecimal(rate);
            bigDecimal=bigDecimal.multiply(BigDecimal.valueOf(0.01));
            log.info("bigDecimal:{}",bigDecimal);
            rates.setRate(bigDecimal);
            rates.setAddTime(new Date());
            log.info("rates:{}",rates);
            accService.updateRate(String.valueOf(bigDecimal));
            sendMessage.setText("设置成功,当前费率为："+rate);
            try {
                log.info("发送消息9");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (text.substring(0,4).equals("设置汇率")){
            rates.setExchange(new BigDecimal(text.substring(4)));
            accService.updateExchange(rates.getExchange());
            sendMessage.setText("设置成功,当前汇率为："+text.substring(4));
            try {
                log.info("发送消息10");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //入账操作
    private void inHandle(String[] split2, String text,
                          Account updateAccount, BigDecimal total, String userName,
                          BigDecimal down, SendMessage sendMessage, List<Account> list,
                          Message message, String[] split3, Rate rate, String callBackFirstName,
                          String callBackName, String firstName, Issue issue, List<Issue> issueList) {
        //判断是否符合公式
        boolean orNo1 = isMatcher(text);
        if (text.charAt(0) == '+' || text.charAt(0) == '-') {
            // 如果 text 的第一个字符是 '+'，或者 '-'，或者 orNo1 为 true，则继续执行
            log.info("yesyesyesyes");
        } else {
            // 如果 text 的第一个字符不是 '+' 也不是 '-'，并且 orNo1 为 false，则返回
            log.info("ysyesyes");
            return;
        }


        BigDecimal num = new BigDecimal(0);
        if (!isMatcher(text)){
            //当不是公式入账时才赋值
            num=new BigDecimal(text.substring(1));
        }
        //判断是否是第一次入账
        if (list.size()>0){
            //获取最近一次入账记录，方便后续操作
            updateAccount=list.get(list.size()-1);
            if (callBackName==null){
                updateAccount.setCall_back(" ");
                updateAccount.setCallBackFirstName(" ");
            }
        }
        //判断是否是第一次出账
        if (issueList.size()>0){
            //获取最近一次出账记录，方便后续操作
            issue=issueList.get(issueList.size()-1);
        }
        //当前时间
        updateAccount.setAddTime(new Date());
        //数据状态默认是0
        updateAccount.setDataStatus(0);
        issue.setAddTime(new Date());
        issue.setDataStatus(0);
        down = updateAccount.getDown();
        BigDecimal downed = issue.getDowned();
        BigDecimal downing = updateAccount.getDowning();
        total = updateAccount.getTotal();
        //如果是第一次入账，初始化
        if (list.size()==0){
            down = new BigDecimal(0);
            downed =new BigDecimal(0);
            downing = new BigDecimal(0);
            total=new BigDecimal(0);
        }
        //如果是第一次出账，初始化
        if (issueList.size()==0){
            downed =new BigDecimal(0);
        }
        updateAccount.setTotal(total);
        issue.setDowned(downed);
        updateAccount.setDowning(downing);
        updateAccount.setHandleFirstName(firstName);
        issue.setHandle(userName);
        issue.setHandleFirstName(firstName);
        //如果是回复消息，设置回复人的相关消息
        if (callBackName!=null){
            updateAccount.setCall_back(callBackName);
            updateAccount.setCallBackFirstName(callBackFirstName);
        }
        //设置account的过期时间
        if(list.size()>0){
            if (list.get(list.size()-1).getSetTime()!=null){
                updateAccount.setSetTime(list.get(list.size()-1).getSetTime());
            }else {
                if (oldSetTime!=null){
                    updateAccount.setSetTime(oldSetTime);
                }else {
                    LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                    Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                    updateAccount.setSetTime(setTime);
                    log.info("setTime:{}",setTime);
                }
                log.info("oldSetTime,{}",oldSetTime);

            }
            updateAccount.setSetTime(list.get(list.size()-1).getSetTime());
        }else {
            if (oldSetTime!=null){
                updateAccount.setSetTime(oldSetTime);
            }else {
                LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                updateAccount.setSetTime(setTime);
            }
        }
        //设置issue的过期时间
        if(issueList.size()>0){
            if (issueList.get(issueList.size()-1).getSetTime()!=null){
                issue.setSetTime(issueList.get(issueList.size()-1).getSetTime());
            }else {
                if (oldSetTime!=null){
                    issue.setSetTime(oldSetTime);
                }else {
                    LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                    Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                    issue.setSetTime(setTime);
                    log.info("setTime:{}",setTime);
                }
                log.info("oldSetTime,{}",oldSetTime);

            }
            issue.setSetTime(issueList.get(issueList.size()-1).getSetTime());
        }else {
            if (oldSetTime!=null){
                issue.setSetTime(oldSetTime);
            }else {
                LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                issue.setSetTime(setTime);
            }
        }
        char firstChar = text.charAt(0);
        //公式入账（重写了isOrNo方法）
        boolean orNo = isOrNo(text, userName, updateAccount, total, down,issue,downed,downing);
        //判断是+还是-
        if (firstChar == '+' && ( callBackName == null||callBackName.equals("zqzs18bot"))&&orNo==false){
            total=total.add(num);
            updateAccount.setTotal(total);
            updateAccount.setHandle(userName);
            //计算应下发
            downing=dowingAccount(rate,downing,total,num);
            updateAccount.setDowning(downing);
            updateAccount.setDown(down.add(num));
            accService.insertAccount(updateAccount);
            accService.uodateIssueDown(down.add(num));
        }else if (firstChar == '-' && ( callBackName == null||callBackName.equals("zqzs18bot"))&&orNo==false){
            issue.setHandle(userName);
            issue.setDown(down.subtract(num));
            issue.setDowned(downed.add(num));
            log.info("issue--:{}",issue);
            if (issue.getHandle()!=null){
                accService.insertIssue(issue);
                accService.updateDown(down.subtract(num));
                log.info("执行了================");
            }
        }
        //重新获取最新的数据
        List<Account> accounts = accService.selectAccount();
        List<Issue> issues = accService.selectIssue();
        log.info("issues,,{}",issues);
        //获取时间数据方便后续操作
        List<String> newList = new ArrayList<>();
        List<String> newIssueList=new ArrayList<>();
        for (Account account : accounts) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newList.add(sdf.format(account.getAddTime()));
        }
        for (Issue issue1 : issues) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newIssueList.add(sdf.format(issue1.getAddTime()));
        }
        if (split2.length>1||split3.length>1||orNo1) {
            updateAccount=accounts.get(accounts.size()-1);
            //发送要显示的消息
            String sendText1 = getSendText(updateAccount, accounts,rate, num, newList,newIssueList,issues,issue,text);
            sendMessage.setText(sendText1);
            implList(message, sendMessage);
        }
        try {
            log.info("发送消息1");
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //计算应下发：(t-t*r)/c     +1000（金额）*0.05（费率）/7（汇率）= （单位结果）
    private BigDecimal calc(Rate rate, BigDecimal downing, BigDecimal total, BigDecimal num) {
        BigDecimal divide = num.multiply(rate.getRate()).divide(rate.getExchange());
        String input = "+1000/7*0.05";
        // 正则表达式匹配数字（包括负数和小数）
        String regex = "[-+]?\\d*\\.\\d+|[-+]?\\d+";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        List<String> numbers = new ArrayList<>();

        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return downing;
    }

    //计算应下发：(t-t*r)/c     +1000（金额）*0.05（费率）/7（汇率）= （单位结果）
    private BigDecimal dowingAccount(Rate rate, BigDecimal downing, BigDecimal total, BigDecimal num) {
        log.info("numsssssss:{}",num);
        BigDecimal rate1 = rate.getRate();
        rate1=rate1.multiply(BigDecimal.valueOf(0.01));
        log.info("rate1:{}", rate1);
        BigDecimal exchange = rate.getExchange();
        log.info("exchange:{}", exchange);
        BigDecimal totalTimesRate1 = num.multiply(rate1);
        log.info("totalTimesRate1:{}", totalTimesRate1);
        // 计算 total - (total * rate1)
        BigDecimal totalMinusTotalTimesRate1 = num.subtract(totalTimesRate1);
        log.info("totalMinusTotalTimesRate1:{}", totalMinusTotalTimesRate1);
        // 计算最终结果 d = (total - (total * rate1)) / exchange
        BigDecimal d = totalMinusTotalTimesRate1.divide(exchange, 2, RoundingMode.HALF_UP); // 保留两位小数
        log.info("d:{}", d);
        downing=downing.add(d);
        log.info("downing:{}", downing);
        return downing;
    }

    //入账操作2.0，匹配公式
    private boolean isOrNo(String text1, String userName, Account updateAccount, BigDecimal total, BigDecimal down, Issue issue, BigDecimal downed, BigDecimal downing) {
        String text = text1.substring(1);
        if (text1.charAt(0)!='+'&&text1.charAt(0)!='-'){
            log.info("999999999999");
            return false;
        }
        Rate rate=accService.selectRate();
        //匹配100*8/9
        Pattern pattern = Pattern.compile("^(\\d+)\\*(\\d+)\\/(\\d+)$");
        //匹配100/9
        Pattern pattern1 = Pattern.compile("^(\\d+)\\/(\\d+)$");
        //匹配100*8
        Pattern pattern2 = Pattern.compile("^(\\d+)\\*(\\d+)$");
        Matcher matcher = pattern.matcher(text);
        Matcher matcher1 = pattern1.matcher(text);
        Matcher matcher2 = pattern2.matcher(text);
        boolean matchFound = matcher.find();
        log.info("matchFound:{}",matchFound);
        boolean matchFound1 = matcher1.find();
        log.info("matchFound1:{}",matchFound1);
        boolean matchFound2 = matcher2.find();
        log.info("matchFound2",matchFound2);
        BigDecimal t = new BigDecimal(0);
        BigDecimal r = new BigDecimal(0);
        BigDecimal e = new BigDecimal(0);
        log.info("matchFound:{},matchFound1:{},matchFound2:{}",matchFound,matchFound1,matchFound2);
        //获取匹配到的数字
        if (matchFound){
            t = BigDecimal.valueOf(Long.parseLong(matcher.group(1)));
            r = BigDecimal.valueOf(Long.parseLong(matcher.group(2)));
            e = BigDecimal.valueOf(Long.parseLong(matcher.group(3)));
        }else if (matchFound1){
            t = BigDecimal.valueOf(Long.parseLong(matcher1.group(1)));
            e = BigDecimal.valueOf(Long.parseLong(matcher1.group(2)));
        }else if (matchFound2){
            t = BigDecimal.valueOf(Long.parseLong(matcher2.group(1)));
            r = BigDecimal.valueOf(Long.parseLong(matcher2.group(2)));
        }
        log.info(".....t:{},r:{},e:{}", t, r, e);
        //按照公式进行计算
        if (matchFound&&text1.charAt(0)=='+') {
            log.info("t:{},r:{},e:{}", t, r, e);
            rate.setRate(r);
            rate.setExchange(e);
            downing =dowingAccount(t,rate,downing) ;
            log.info("downingsssssss:{}", downing);
            total = total.add(t);
            updateAccount.setTotal(total);
            log.info("total-,,-:{}",total);
            updateAccount.setHandle(userName);
            log.info("downingaaaaa:{}",downing);
            updateAccount.setDowning(downing);
            updateAccount.setDown(down.add(t));
            log.info("account/////:{}",updateAccount);
            accService.insertAccount(updateAccount);
            accService.uodateIssueDown(down.add(t));
            return true;
        } else if (matchFound&&text1.charAt(0)=='-'){
            // 解析数字
            log.info("t:{},r:{},e:{}", t, r, e);
            rate.setRate(r);
            rate.setExchange(e);
            issue.setHandle(userName);
            issue.setDown(down.subtract(t));
            issue.setDowned(downed.add(t));
            if (issue.getHandle()!=null){
                accService.insertIssue(issue);
                accService.updateDown(down.subtract(t));
                log.info("执行了1================");
            }

            return true;
        }else if (matchFound1&&text1.charAt(0)=='+'){
            //匹配100/9
            log.info("t:{},r:{},e:{}", t, r, e);
            rate.setExchange(e);
            downing =dowingAccount(t,rate,downing) ;
            log.info("downing:{}", downing);
            total = total.add(t);
            updateAccount.setTotal(total);
            log.info("total-....-:{}",total);
            updateAccount.setHandle(userName);
            log.info("downingjjjjj:{}",downing);
            updateAccount.setDowning(downing);
            updateAccount.setDown(down.add(t));
            accService.insertAccount(updateAccount);
            accService.uodateIssueDown(down.add(t));
            return true;
        }else if (matchFound1&&text1.charAt(0)=='-'){
            log.info("t:{},r:{},e:{}", t, r, e);
            rate.setExchange(e);
            log.info("rate111:{}",rate);
            issue.setHandle(userName);
            issue.setDown(down.subtract(t));
            issue.setDowned(downed.add(t));
            if (issue.getHandle()!=null){
                accService.insertIssue(issue);
                accService.updateDown(down.subtract(t));
                log.info("执行了1================");
            }

            return true;
        }else if (matchFound2&&text1.charAt(0)=='+'){
            //匹配100*9
            log.info("t:{},r:{},e:{}", t, r, e);
            rate.setRate(r);
            downing =dowingAccount(t,rate,downing) ;
            log.info("downing:{}", downing);
            total = total.add(t);
            updateAccount.setTotal(total);
            log.info("total--:{}",total);
            updateAccount.setHandle(userName);
            //计算应下发
            downing=dowingAccount(total,rate,downing);
            log.info("downingddddddd:{}", downing);
            updateAccount.setDowning(downing);
            updateAccount.setDown(down.add(t));
            accService.insertAccount(updateAccount);
            accService.uodateIssueDown(down.add(t));
            return true;
        }else if (matchFound2&&text1.charAt(0)=='-'){
            log.info("t:{},r:{},e:{}", t, r, e);
            rate.setRate(r);
            issue.setHandle(userName);
            issue.setDown(down.subtract(t));
            issue.setDowned(downed.add(t));
            if (issue.getHandle()!=null){
                accService.insertIssue(issue);
                accService.updateDown(down.subtract(t));
                log.info("执行了1================");
            }

            return true;
        }
        return false;
    }
    //是否匹配公式入账
    private boolean isMatcher(String text1) {
        String text = text1.substring(1);
        //匹配100*8/9
        Pattern pattern = Pattern.compile("^(\\d+)\\*(\\d+)\\/(\\d+)$");
        //匹配100/9
        Pattern pattern1 = Pattern.compile("^(\\d+)\\/(\\d+)$");
        //匹配100*8
        Pattern pattern2 = Pattern.compile("^(\\d+)\\*(\\d+)$");
        Matcher matcher = pattern.matcher(text);
        Matcher matcher1 = pattern1.matcher(text);
        Matcher matcher2 = pattern2.matcher(text);
        boolean matchFound = matcher.find();
        log.info("matchFound:{}",matchFound);
        boolean matchFound1 = matcher1.find();
        log.info("matchFound1:{}",matchFound1);
        boolean matchFound2 = matcher2.find();
        log.info("matchFound2",matchFound2);
        if (matchFound||matchFound1||matchFound2) {
            return true;
        }
        return false;
    }

    //应下发计算公式：d=(total-(total*rate1))/exchange
    private BigDecimal dowingAccount(BigDecimal tatol, Rate rate,BigDecimal downing) {
        BigDecimal rate1 = rate.getRate();
        rate1=rate1.multiply(BigDecimal.valueOf(0.01));
        log.info("rate1:{}", rate1);
        BigDecimal exchange = rate.getExchange();
        log.info("exchange:{}", exchange);
        BigDecimal totalTimesRate1 = tatol.multiply(rate1);
        log.info("totalTimesRate1:{}", totalTimesRate1);
        // 计算 total - (total * rate1)
        BigDecimal totalMinusTotalTimesRate1 = tatol.subtract(totalTimesRate1);
        log.info("totalMinusTotalTimesRate1:{}", totalMinusTotalTimesRate1);
        // 计算最终结果 d = (total - (total * rate1)) / exchange
        BigDecimal d = totalMinusTotalTimesRate1.divide(exchange, 2, RoundingMode.HALF_UP); // 保留两位小数
        log.info("d:{}", d);
        downing=downing.add(d);
        return downing;
    }

    //判断是否过期
    private List<Account> isOver24Hour(Message message, SendMessage sendMessage) {

        List<Account> list=accService.selectAccount();
        //默认日切是8点
        int i=8;
        Date setTime=new Date();
        if (list.size()>0){
            oldSetTime=list.get(list.size()-1).getSetTime();
            setTime=list.get(list.size()-1).getSetTime();
        }
        log.info("setTime;;;{}",setTime);

        if (message.getText().length()>=4&&message.getText().substring(0,4).equals("设置日切")){
            i = Integer.parseInt(message.getText().substring(4));
            log.info("i:{}",i);
            LocalDateTime fourAMToday = LocalDate.now().atTime(i, 0);
            setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
            log.info("setTime2:{}",setTime);
            accService.updateSetTime(setTime);
            //过期时间是一天
            accService.updateOverDue((long) (24 * 60 * 60 * 1000));
            //accService.updateOverDue((long) ( 60 * 1000));
            sendMessage.setText("设置成功");

            try {
                log.info("发送消息2");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
        if (accService.selectAccount().size()!=0){
            list=accService.selectAccount();
            //获取设置当天的时间
            long diff =list.get(list.size()-1).getAddTime().getTime()-list.get(list.size()-1).getSetTime().getTime();
            //boolean over24hour=diff > 24 * 60 * 60 * 1000;
            Rate rate=accService.selectRate();
            log.info("ratesssssssss:{}",rate);
            boolean over24hour=diff >  rate.getOverDue();
            setTime = list.get(list.size()-1).getSetTime();

            if (over24hour){
                Over24Hour=true;
                accService.updateDataStatus();
                Rate rate1=new Rate();
                accService.updateSetTime(setTime);
                log.info("setTime,,:{}",setTime);
                accService.updateRate(String.valueOf(rate1.getRate()));
                accService.updateExchange(rate1.getExchange());
                log.info("over24hour---------:{}",over24hour);
                list=accService.selectAccount();
                log.info("listover24:{}",list);
            }
        }
        log.info("Over24Hour,,:{}",Over24Hour);

        setOver24Hour(Over24Hour);

        return list;
    }
    //删除今日数据/关闭日切
    private void deleteTedayData(Message message, SendMessage sendMessage, List<Account> list, String replyToText) {
        String text = message.getText();
        if (text.length()>=4){
            //删除今日账单关键词： 清理今天数据 删除今天数据 清理今天账单 删除今天账单
            if (text.equals("清理今天数据")||text.equals("删除今天数据")||text.equals("清理今天账单")||text.equals("删除今天账单")){
                accService.deleteTedayData();
                accService.deleteTedayIusseData();
                sendMessage.setText("操作成功");
                try {
                    log.info("发送消息3");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("deleteTedayData异常");
                }

            }else if (text.equals("关闭日切")){
                Long overdue=3153600000000l;
                accService.updateOverDue(overdue);
                sendMessage.setText("操作成功,关闭日切");
                try {
                    log.info("发送消息3");
                    execute(sendMessage);
                } catch (Exception e) {
                    log.info("deleteTedayData异常");
                }
            }

        }
    }

    private boolean setOver24Hour(boolean over24Hour) {
        return over24Hour;
    }
    //转换时间
    private static Date timeExchange(String timeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // 将字符串转换为 LocalTime 类型
        LocalTime time = LocalTime.parse(timeStr, formatter);
        // 获取当前日期
        LocalDateTime localDateTime = LocalDateTime.now().with(time);
        // 转换为指定时区的日期时间
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("GMT+8"));
        // 转换为 Date 类型
        Date addTime = Date.from(zonedDateTime.toInstant());
        // 输出转换后的日期
        log.info("Converted date: " + addTime);
        return addTime;
    }
    //入账时发送的消息
    private static String getSendText(Account updateAccount, List<Account> list, Rate rate, BigDecimal num, List<String> newList, List<String> newIssueList,
                                      List<Issue> issuesList, Issue issue, String text) {
        String iusseText="";
        log.info("newIssueList:{}",newIssueList);
        log.info("issuesList:{}",issuesList);
        log.info("issue:{}",issue);
        //显示操作人
        if (issuesList.size()==1){
            //显示操作人
            String text1 = rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            //显示明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            //显示回复人
            String text3= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCall_back()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text2+text1+text3;

        }else if (issuesList.size()==2){
            String text11 = rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text31= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCall_back()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String text32= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCall_back()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32;
        }else if (issuesList.size()>2){
            //显示操作人
            String text11 = rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-2).getHandle()+" "+issuesList.get(issuesList.size()-2).getHandleFirstName() : "";
            String text13 = rate.getHandlestatus() == 0 ? " @"+issuesList.get(issuesList.size()-3).getHandle()+" "+issuesList.get(issuesList.size()-3).getHandleFirstName() : "";
            //显示明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text23 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-3).getDowned().subtract(issuesList.get(issuesList.size()-3).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text31= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCall_back()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String text32= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCall_back()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            String text33= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-3).getCall_back()+" "+issuesList.get(issuesList.size()-3).getCallBackFirstName() : "";
            log.info("newIssueList:{}",newIssueList);
            log.info("issuesList:{}",issuesList);
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+
                    newIssueList.get(newIssueList.size()-3)+"  "+
                    issuesList.get(issuesList.size()-3).getDowned().setScale(2, RoundingMode.HALF_UP)+text23+text13+text33;

        } else {
            if (updateAccount.getDown()!=null){
                issue.setDown(updateAccount.getDown());
            }
            issue.setDown(BigDecimal.ZERO);
            iusseText="\n\n" +"已下发：\n"+
                    "暂无下发数据";
        }
        //显示操作人/显示明细
        if (list.size()==1){
            //是否隐藏操作人
            String text1 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-1).getHandle()+" "+list.get(list.size()-1).getHandleFirstName() : "";

            //是否隐藏明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text3 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-1).getCall_back()+" "+list.get(list.size()-1).getCallBackFirstName() : "";
            return  "\n已入账："+num +"，共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(list.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text2+text1+text3+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP);
        }else if (list.size()==2){
            //是否隐藏操作人
            String text11 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-1).getHandle()+" "+list.get(list.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-2).getHandle()+" "+list.get(list.size()-2).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-1).getCall_back()+" "+list.get(list.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-2).getCall_back()+" "+list.get(list.size()-2).getCallBackFirstName() : "";
            return "\n已入账："+num +"，:共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(list.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    list.get(list.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP);
        }else if (list.size()>2){
            //是否隐藏操作人
            String text11 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-1).getHandle()+" "+list.get(list.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-2).getHandle()+" "+list.get(list.size()-2).getHandleFirstName() : "";
            String text13 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-3).getHandle()+" "+list.get(list.size()-3).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text23 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-3).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-1).getCall_back()+" "+list.get(list.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-2).getCall_back()+" "+list.get(list.size()-2).getCallBackFirstName() : "";
            String text33 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-3).getCall_back()+" "+list.get(list.size()-3).getCallBackFirstName() : "";
            return "\n已入账："+num +"，:共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(list.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    list.get(list.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+
                    newList.get(newList.size()-3)+" "+
                    list.get(list.size()-3).getTotal().setScale(2, RoundingMode.HALF_UP)+text23+text13+text33+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP);
        } else {
            return "入账："+ num ;
        }

    }


    //设置操作人员
    private void setHandle(String[] split1, String userName, String firstName, List<User> userList,
                           SendMessage sendMessage, Message message, String callBackName,
                           String callBackFirstName, String text) {
        if (userList.stream().anyMatch(user -> Objects.equals(user.getUsername(), firstName))){
            sendMessage.setText("已设置该操作员无需重复设置");
            implList(message, sendMessage);
            try {
                log.info("发送消息4");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("设置操作员")||split1[0].equals("设置操作人")){
//            if (!userList.isEmpty()&&userName.equals(userList.get(0).getUsername())){
            User user = new User();
            if (callBackName!=null){
                user.setUsername(callBackName);
                user.setFirstname(callBackFirstName);
                accService.insertUser(user);
            }else {
                Pattern pattern = Pattern.compile("@(\\w+)");
                Matcher matcher = pattern.matcher(text);
                List<String> userLists = new ArrayList<>();
                while (matcher.find()) {
                    // 将匹配到的用户名添加到列表中
                    userLists.add(matcher.group(1));
                }

                // 打印提取到的用户列表
                for (String users : userLists) {
                    user.setUsername(users);
                    accService.insertUser(user);
                }
            }
            sendMessage.setText("设置成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            }
        }else if (split1[0].equals("显示操作人")||split1[0].equals("显示操作员")){
            int size = userList.size();
            StringBuilder sb = new StringBuilder("当前操作人: ");
            sb.append(" @");
            for (int i = 0; i < userList.size(); i++) {
                sb.append(userList.get(i).getUsername());
                if (i < userList.size() - 1) {
                    sb.append(" @");
                }
            }
            sendMessage.setText(sb.toString());
            implList(message, sendMessage);
            try {
                log.info("发送消息11");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("将操作员显示")){
            accService.updateHandleStatus(0);
            sendMessage.setText("操作成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("关闭显示")){
            accService.updateHandleStatus(1);
            sendMessage.setText("操作成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("将回复人显示")){
            accService.updateCallBackStatus(0);
            sendMessage.setText("操作成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("关闭回复人显示")){
            accService.updateCallBackStatus(1);
            sendMessage.setText("操作成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("显示明细")){
            accService.updateDatilStatus(0);
            sendMessage.setText("操作成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("隐藏明细")){
            accService.updateDatilStatus(1);
            sendMessage.setText("操作成功");
            implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //实现list按钮
    private void implList(Message message, SendMessage sendMessage) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        //11111111111
//        Long chatId = message.getChatId();
//
//        String text = message.getText();
//        sendMsg(text,chatId);
        // 创建第一个按钮
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("按钮一名称");
        button1.setUrl("https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_8595438888645751841%22%7D&n_type=-1&p_from=-1");

        // 创建第二个按钮
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("按钮二名称");
        button2.setUrl("https://your-url-for-button-two.com");

        rowInline.add(button1);
        rowInline.add(button2);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);

    }

    public void setService(accService accService) {
        this.accService = accService;
    }
}


