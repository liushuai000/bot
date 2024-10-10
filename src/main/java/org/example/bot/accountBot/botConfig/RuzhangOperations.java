package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 入账操作  ||  入账时发送的消息   isMatcher: 是否匹配公式入账 例:+1000*0.05/7 这种公式
 */

@Slf4j
@Service
public class RuzhangOperations{
    @Autowired
    Utils utils;
    //操作时间
    @Autowired
    DateOperator dateOperator;
    @Autowired
    AccountBot accountBot;
    @Autowired
    protected RateService rateService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected IssueService issueService;
    @Autowired
    protected AccountService accountService;

    //设置费/汇率
    protected void setRate(Message message,SendMessage sendMessage,Rate rates) {
        String text = message.getText();
        if (text.length()<4){return;}
        if (text.startsWith("设置费率")){
            String rate = text.substring(4);
            BigDecimal bigDecimal = new BigDecimal(rate);
//            bigDecimal=bigDecimal.multiply(BigDecimal.valueOf(0.01));
            rates.setRate(bigDecimal);
            rates.setAddTime(new Date());
            log.info("rates:{}",rates);
            rateService.updateRate(String.valueOf(bigDecimal));
            accountBot.sendMessage(sendMessage,"设置成功,当前费率为："+rate);
        }else if (text.startsWith("设置汇率")){
            rates.setExchange(new BigDecimal(text.substring(4)));
            rateService.updateExchange(rates.getExchange());
            accountBot.sendMessage(sendMessage,"设置成功,当前汇率为："+text.substring(4));
        }else if (text.startsWith("设置入款单笔手续费")){
            //
        }
    }

    //撤销入款
    public void repeal(Message message, SendMessage sendMessage, List<Account> list, String replyToText, String callBackName, List<Issue> issueList) {
        String text = message.getText();
        if (text.length()>=2||replyToText!=null){
            if (text.equals("撤销入款")){
                accountService.deleteInData(list.get(list.size()-1).getAddTime());
                issueService.updateIssueDown(list.get(list.size()-1).getDown());
                accountBot.sendMessage(sendMessage,"撤销成功");
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
                accountBot.sendMessage(sendMessage,"取消成功");
            }else if (text.equals("撤销下发")){
                issueService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime());
                accountService.updateNewestData(issueList.get(issueList.size()-1).getDown());
                accountBot.sendMessage(sendMessage,"撤销成功");
            }
        }
    }


    //入账操作
    public void inHandle(String[] split2, String text, Account updateAccount, String userName, SendMessage sendMessage,
                          List<Account> accountList, Message message, String[] split3, Rate rate, String callBackFirstName, String callBackName,
                          String firstName, Issue issue, List<Issue> issueList) {
        BigDecimal total;
        BigDecimal down;
        //判断是否符合公式
        boolean isMatcher = utils.isMatcher(text);
        // 如果 text 的第一个字符是 '+'，或者 '-'，或者 orNo1 为 true，则继续执行
        if (text.charAt(0) != '+' && text.charAt(0) != '-')  return;
        BigDecimal num = new BigDecimal(0);
        //当不是公式入账时才赋值
        if (!utils.isMatcher(text))  num=new BigDecimal(text.substring(1));
        //判断是否是第一次入账
        if (accountList.size()>0){
            //获取最近一次入账记录，方便后续操作
            updateAccount=accountList.get(accountList.size()-1);
            if (callBackName==null){
                updateAccount.setCallBack(" ");
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
        if (accountList.size()==0){
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
            updateAccount.setCallBack(callBackName);
            updateAccount.setCallBackFirstName(callBackFirstName);
        }
        //设置account的过期时间
        if(!accountList.isEmpty()){
            if (accountList.get(accountList.size()-1).getSetTime()!=null){
                updateAccount.setSetTime(accountList.get(accountList.size()-1).getSetTime());
            }else {
                if (dateOperator.oldSetTime!=null){
                    updateAccount.setSetTime(dateOperator.oldSetTime);
                }else {
                    LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                    Date setTime = new Date(fourAMToday.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    updateAccount.setSetTime(setTime);
                    log.info("setTime:{}",setTime);
                }
                log.info("oldSetTime,{}",dateOperator.oldSetTime);
            }
            updateAccount.setSetTime(accountList.get(accountList.size()-1).getSetTime());
        }else {
            if (dateOperator.oldSetTime!=null){
                updateAccount.setSetTime(dateOperator.oldSetTime);
            }else {
                LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
                Date setTime = new Date(fourAMToday.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
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
                    Date setTime = new Date(fourAMToday.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
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
                Date setTime = new Date(fourAMToday.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                issue.setSetTime(setTime);
            }
        }
        char firstChar = text.charAt(0);
        //公式入账
        boolean orNo = utils.calcRecorded(text, userName, updateAccount, total, down,issue,downed,downing);
        //判断是+还是-
        if (firstChar == '+' && ( callBackName == null||callBackName.equals("zqzs18bot"))&&orNo==false){
            total=total.add(num);
            updateAccount.setTotal(total);
            updateAccount.setHandle(userName);
            //计算应下发   num是当前的total  total里包括了以前的金额 所以用num要计算本次的下发
            downing=utils.dowingAccount(num,rate,downing);
            updateAccount.setDowning(downing);
            updateAccount.setDown(downing.subtract(downed));//总入帐-(总入帐*费率)/汇率=应下发- 已下发= 未下发
            accountService.insertAccount(updateAccount);
            issueService.updateIssueDown(down.add(num));
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
        String sendText1 = null;
        if (split2.length>1||split3.length>1||isMatcher) {
            updateAccount=accounts.get(accounts.size()-1);
            //发送要显示的消息
            sendText1 = getSendText(updateAccount, accounts,rate, num, newList,newIssueList,issues,issue);
            sendMessage.setText(sendText1);//  |  360U
            new ButtonList().implList(message, sendMessage);
        }
        accountBot.sendMessage(sendMessage,sendText1);
    }


    //入账时发送的消息
    public String getSendText(Account updateAccount, List<Account> list, Rate rate, BigDecimal num, List<String> newList, List<String> newIssueList,
                                      List<Issue> issuesList, Issue issue) {
        String iusseText="";
        log.info("newIssueList:{}",newIssueList);
        log.info("issuesList:{}",issuesList);
        log.info("发行issue:{}",issue);
        //显示操作人
        if (issuesList.size()==1){
            //显示操作人
            String text1 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            //显示明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            //显示回复人
            String text3= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text2+text1+text3;

        }else if (issuesList.size()==2){
            String text11 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text31= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String text32= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCallBack()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32;
        }else if (issuesList.size()>2){
            //显示操作人
            String text11 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-2).getHandle()+" "+issuesList.get(issuesList.size()-2).getHandleFirstName() : "";
            String text13 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-3).getHandle()+" "+issuesList.get(issuesList.size()-3).getHandleFirstName() : "";
            //显示明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text23 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-3).getDowned().subtract(issuesList.get(issuesList.size()-3).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text31= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String text32= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCallBack()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            String text33= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-3).getCallBack()+" "+issuesList.get(issuesList.size()-3).getCallBackFirstName() : "";
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
            String text1 = rate.getHandleStatus() == 0 ? " @"+list.get(0).getHandle()+" "+list.get(0).getHandleFirstName() : "";

            //是否隐藏明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(0).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text3 = rate.getCallBackStatus() == 0 ? " @"+list.get(0).getCallBack()+" "+list.get(0).getCallBackFirstName() : "";
            return  "\n已入账："+num +"，共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+text2+text1+text3+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDowning().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "+issue.getDowned().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDown().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U";
        }else if (list.size()==2){
            //是否隐藏操作人
            String text11 = rate.getHandleStatus() == 0 ? " @"+list.get(list.size()-1).getHandle()+" "+list.get(list.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandleStatus() == 0 ? " @"+list.get(0).getHandle()+" "+list.get(0).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(0).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-1).getCallBack()+" "+list.get(list.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+list.get(0).getCallBack()+" "+list.get(0).getCallBackFirstName() : "";
            return "\n已入账："+num +"，:共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(list.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    list.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDowning().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "+issue.getDowned().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP) +"   |    "+ updateAccount.getDown().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U";
        }else if (list.size()>2){
            //是否隐藏操作人
            String text11 = rate.getHandleStatus() == 0 ? " @"+list.get(list.size()-1).getHandle()+" "+list.get(list.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandleStatus() == 0 ? " @"+list.get(list.size()-2).getHandle()+" "+list.get(list.size()-2).getHandleFirstName() : "";
            String text13 = rate.getHandleStatus() == 0 ? " @"+list.get(list.size()-3).getHandle()+" "+list.get(list.size()-3).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text23 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    list.get(list.size()-3).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-1).getCallBack()+" "+list.get(list.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-2).getCallBack()+" "+list.get(list.size()-2).getCallBackFirstName() : "";
            String text33 = rate.getCallBackStatus() == 0 ? " @"+list.get(list.size()-3).getCallBack()+" "+list.get(list.size()-3).getCallBackFirstName() : "";
            return "\n已入账："+num +"，:共"+(list.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    list.get(list.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+text21+text11+text31+"\n"+
                    newList.get(newList.size()-2)+" "+
                    list.get(list.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+text22+text12+text32+"\n"+
                    newList.get(newList.size()-3)+" "+
                    list.get(list.size()-3).getTotal().setScale(2, RoundingMode.HALF_UP)+text23+text13+text33+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDowning().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "+issue.getDowned().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDown().multiply(rate.getExchange().multiply(BigDecimal.valueOf(0.01))).setScale(2, RoundingMode.HALF_UP) +"U";
        } else {
            return "入账："+ num ;
        }

    }


}
