package org.example.bot.accountBot.botConfig;

import javassist.compiler.ast.Variable;
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
@SuppressWarnings("unchecked")
public class AccountBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.token}")
    private String botToken;
    @Value("${telegram.bot.username}")
    private String username;
//111111测试GitHub联通
    @Autowired
    private RateService rateService;
    @Autowired
    private UserService userService;
    @Autowired
    private IssueService issueService;
    @Autowired
    private AccountService accountService;
    Utils utils=new Utils();
    DateOperator dateOperator=new DateOperator();
    SettingOperatorPerson settingOperatorPerson=new SettingOperatorPerson();
    ShowOperatorName showOperatorName=new ShowOperatorName();
    ButtonList buttonList=new ButtonList();


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



    //业务处理
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
        inHandle(split2,message.getText(), updateAccount,  userName, sendMessage, accountList, message,split3,
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
                issueService.updateissueDown(list.get(list.size()-1).getDown());
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
                    issueService.updateissueDown(list.get(list.size()-1).getDown());
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
            userService.deleteHandele(deleteName);
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
        if (text.substring(0,4).equals("设置费率")){
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
        }else if (text.substring(0,4).equals("设置汇率")){
            rates.setExchange(new BigDecimal(text.substring(4)));
            rateService.updateExchange(rates.getExchange());
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
    private void inHandle(String[] split2, String text, Account updateAccount,  String userName, SendMessage sendMessage,
                          List<Account> list, Message message, String[] split3, Rate rate, String callBackFirstName, String callBackName,
                          String firstName, Issue issue, List<Issue> issueList) {
        BigDecimal total;
        BigDecimal down;
        //判断是否符合公式
        boolean orNo1 = isMatcher(text);
        if (text.charAt(0) != '+' || text.charAt(0) != '-') {
            return;// 如果 text 的第一个字符是 '+'，或者 '-'，或者 orNo1 为 true，则继续执行
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
        if(!list.isEmpty()){
            if (list.get(list.size()-1).getSetTime()!=null){
                updateAccount.setSetTime(list.get(list.size()-1).getSetTime());
            }else {
                if (dateOperator.oldSetTime!=null){
                    updateAccount.setSetTime(dateOperator.oldSetTime);
                }else {
                    LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                    Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                    updateAccount.setSetTime(setTime);
                    log.info("setTime:{}",setTime);
                }
                log.info("oldSetTime,{}",dateOperator.oldSetTime);

            }
            updateAccount.setSetTime(list.get(list.size()-1).getSetTime());
        }else {
            if (dateOperator.oldSetTime!=null){
                updateAccount.setSetTime(dateOperator.oldSetTime);
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
                if (dateOperator.oldSetTime!=null){
                    issue.setSetTime(dateOperator.oldSetTime);
                }else {
                    LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                    Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                    issue.setSetTime(setTime);
                    log.info("setTime:{}",setTime);
                }
                log.info("oldSetTime,{}",dateOperator.oldSetTime);

            }
            issue.setSetTime(issueList.get(issueList.size()-1).getSetTime());
        }else {
            if (dateOperator.oldSetTime!=null){
                issue.setSetTime(dateOperator.oldSetTime);
            }else {
                LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                Date setTime = new Date(fourAMToday.toInstant(java.time.ZoneOffset.ofHours(8)).toEpochMilli());
                issue.setSetTime(setTime);
            }
        }
        char firstChar = text.charAt(0);
        //公式入账（重写了isOrNo方法）
        boolean orNo = utils.calcRecorded(text, userName, updateAccount, total, down,issue,downed,downing);
        //判断是+还是-
        if (firstChar == '+' && ( callBackName == null||callBackName.equals("zqzs18bot"))&&orNo==false){
            total=total.add(num);
            updateAccount.setTotal(total);
            updateAccount.setHandle(userName);
            //计算应下发
            downing=utils.dowingAccount(num,rate,downing);
            updateAccount.setDowning(downing);
            updateAccount.setDown(down.add(num));
            accountService.insertAccount(updateAccount);
            issueService.uodateIssueDown(down.add(num));
        }else if (firstChar == '-' && ( callBackName == null||callBackName.equals("zqzs18bot"))&&orNo==false){
            issue.setHandle(userName);
            issue.setDown(down.subtract(num));
            issue.setDowned(downed.add(num));
            log.info("issue--:{}",issue);
            if (issue.getHandle()!=null){
                issueService.insertIssue(issue);
                accountService.updateDown(down.subtract(num));
                log.info("执行了================");
            }
        }
        //重新获取最新的数据
        List<Account> accounts = accountService.selectAccount();
        List<Issue> issues = issueService.selectIssue();
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

    //是否匹配公式入账 例:+1000*0.05/7 这种公式
    private boolean isMatcher(String text1) {
        return utils.isMatcher(text1);
    }

    //判断是否过期
    private List<Account> isOver24Hour(Message message, SendMessage sendMessage) {
        return dateOperator.isOver24Hour(message,sendMessage);
    }
    //删除今日数据/关闭日切
    private void deleteTodayData(Message message, SendMessage sendMessage, List<Account> accountList, String replyToText) {
        dateOperator.deleteTodayData(message,sendMessage,accountList,replyToText);
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
            String text1 = rate.getHandlestatus() == 0 ? " @"+list.get(0).getHandle()+" "+list.get(0).getHandleFirstName() : "";

            //是否隐藏明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(0).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text3 = rate.getCallBackStatus() == 0 ? " @"+list.get(0).getCall_back()+" "+list.get(0).getCallBackFirstName() : "";
            return  "\n已入账："+num +"，共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+text2+text1+text3+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP);
        }else if (list.size()==2){
            //是否隐藏操作人
            String text11 = rate.getHandlestatus() == 0 ? " @"+list.get(list.size()-1).getHandle()+" "+list.get(list.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandlestatus() == 0 ? " @"+list.get(0).getHandle()+" "+list.get(0).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(0).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-1).getCall_back()+" "+list.get(list.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+list.get(0).getCall_back()+" "+list.get(0).getCallBackFirstName() : "";
            return "\n已入账："+num +"，:共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(list.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    list.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+iusseText+"\n"+
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

    /**
     * 设置操作人员
     * @param split1
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

    //实现list按钮
    private void implList(Message message, SendMessage sendMessage) {
        buttonList.implList(message,sendMessage);
    }

}


