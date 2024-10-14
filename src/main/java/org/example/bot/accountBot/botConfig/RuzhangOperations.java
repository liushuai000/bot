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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    private ShowOperatorName showOperatorName;

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
            if (text.substring(4).startsWith("-")){
                rates.setExchange(new BigDecimal(1));
                rateService.updateExchange(rates.getExchange());
                accountBot.sendMessage(sendMessage,"当前汇率已设置为默认汇率：1");
            }else {
                rates.setExchange(new BigDecimal(text.substring(4)));
                rateService.updateExchange(rates.getExchange());
                accountBot.sendMessage(sendMessage,"设置成功,当前汇率为："+text.substring(4));
            }
        }else if (text.startsWith("设置入款单笔手续费")){
            //
        }
    }

    //撤销入款
    public void repeal(Message message, SendMessage sendMessage, List<Account> accounts, String replyToText, String callBackName, List<Issue> issueList) {
        String text = message.getText();
        if (text.length()>=2||replyToText!=null){
            if (text.equals("撤销入款")){
                accountService.deleteInData(accounts.get(accounts.size()-1).getAddTime());
                issueService.updateIssueDown(accounts.get(accounts.size()-1).getDown());
                accountBot.sendMessage(sendMessage,"撤销成功");
            }else if (text.equals("取消")&&replyToText!=null&&callBackName.equals("chishui_id")){
                log.info("replyToXXXTentacion:{}",replyToText);
                if (replyToText.charAt(0)=='+'){
                    accountService.deleteInData(accounts.get(accounts.size()-1).getAddTime());
                    issueService.updateIssueDown(accounts.get(accounts.size()-1).getDown());
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
        //判断是否符合公式 true 是匹配
        boolean isMatcher = utils.isMatcher(text);
        // 如果 text 的第一个字符是 '+'，或者 '-'，或者 orNo1 为 true，则继续执行
        if (text.charAt(0) != '+' && text.charAt(0) != '-')  return;
        //+0 -0显示账单
        if (showOperatorName.isEmptyMoney(text)){
            showOperatorName.replay(sendMessage,updateAccount,rate,issueList,issue,text);
            return;
        }
        BigDecimal num = new BigDecimal(0);
        //当不是公式入账时才赋值
        if (!utils.isMatcher(text)) {
            if (text.substring(1).endsWith("u")||text.substring(1).endsWith("U")){
                String numberPart = text.substring(0, text.length() - 1);
                num=new BigDecimal(numberPart.substring(1)).multiply(rate.getExchange());
            }else {
                num=new BigDecimal(text.substring(1));
            }
        }
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
//                    LocalDateTime fourAMToday = LocalDate.now().atTime(8, 0);
//                    Date setTime = new Date(fourAMToday.toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    updateAccount.setSetTime(new Date());
                }
                log.info("oldSetTime,{}",dateOperator.oldSetTime);
            }
            updateAccount.setSetTime(accountList.get(accountList.size()-1).getSetTime());
        }else {
            if (dateOperator.oldSetTime!=null){
                updateAccount.setSetTime(dateOperator.oldSetTime);
            }else {
                updateAccount.setSetTime(new Date());
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
                    issue.setSetTime(new Date());
                }
                log.info("oldSetTime,{}",dateOperator.oldSetTime);
            }
            issue.setSetTime(issueList.get(issueList.size()-1).getSetTime());
        }else {
            if (dateOperator.oldSetTime!=null){
                issue.setSetTime(dateOperator.oldSetTime);
            }else {
                issue.setSetTime(new Date());
            }
        }
        char firstChar = text.charAt(0);

        //公式入账 isMatch
        boolean isMatch = utils.calcRecorded(text, userName, updateAccount, total, down,issue,downed,downing,rate);
        if (isMatcher==false){
            //判断是+还是-
            if (firstChar == '+' && ( callBackName == null||callBackName.equals("zqzs18bot"))){
                //+10u
                updateAccount.setTotal(num);
                updateAccount.setHandle(userName);
                //计算应下发   num是当前的total  total里包括了以前的金额 所以用num要计算本次的下发
                downing=utils.dowingAccount(num,rate,downing);
                updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
                updateAccount.setDown(downing.subtract(downed));//总入帐-(总入帐*费率)/汇率=应下发- 已下发= 未下发
                accountService.insertAccount(updateAccount);
//            issueService.insertIssue(issue);
//            issueService.updateIssueDown(down.add(num));
            }else if (firstChar == '-' && ( callBackName == null||callBackName.equals("zqzs18bot"))){
                issue.setHandle(userName);
                issue.setDown(total.subtract(num));
                issue.setDowned(downed.add(num));
                log.info("issue--:{}",issue);
                if (issue.getHandle()!=null){
                    issueService.insertIssue(issue);
                    accountService.updateDown(total.subtract(num));
                    log.info("执行了issue.getHandle()!=null");
                }
            }
        }

        //重新获取最新的数据
        List<Account> accounts = accountService.selectAccountDataStatus0();
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
            if (!accounts.isEmpty()){
                updateAccount=accounts.get(accounts.size()-1);
            }
            //发送要显示的消息
            sendText1 = getSendText(updateAccount, accounts,rate, num, newList,newIssueList,issues,issue);
            sendMessage.setText(sendText1);//  |  360U
            new ButtonList().implList(message, sendMessage);
        }
        accountBot.sendMessage(sendMessage,sendText1);
    }


    //入账时发送的消息  显示操作人也用这个
    public String getSendText(Account updateAccount, List<Account> accounts, Rate rate1, BigDecimal num, List<String> newList, List<String> newIssueList,
                                      List<Issue> issuesList, Issue issue) {
        String iusseText="";
        log.info("newIssueList:{}",newIssueList);
        log.info("issuesList:{}",issuesList);
        log.info("发行issue:{}",issue);
        //这里用查询不用rate 是以后多群组更新应该需要根据id 用户更新getDetailStatus 现在这个rate是全局的没有根据id
        //例如现在显示明细 是所有的数据都更新 没有根据具体的群组 group id更新现在先这么写
        Rate rate = rateService.selectRate();
        //显示操作人
        if (issuesList.size()==1){
            //显示操作人
            String operatorName = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            //显示明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            //显示回复人
            String callBackFirstName= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            iusseText="\n已出账："+issuesList.get(0).getDowned() +"，:共"+(issuesList.size())+"笔:\n"+ newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetail+operatorName+callBackFirstName;

        }else if (issuesList.size()==2){
            //操作人的显示状态，1表示不显示，0表示显示    操作人昵称
            String operatorName = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String operatorName2 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String CallBackName1= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String CallBackName2= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCallBack()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            iusseText="\n已出账："+issuesList.get(1).getDowned() +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text21+operatorName+CallBackName1+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+text22+operatorName2+CallBackName2;
        }else if (issuesList.size()>2){
            //显示操作人
            String operatorName1 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+issuesList.get(issuesList.size()-1).getHandleFirstName() : "";
            String operatorName2 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-2).getHandle()+" "+issuesList.get(issuesList.size()-2).getHandleFirstName() : "";
            String operatorName3 = rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-3).getHandle()+" "+issuesList.get(issuesList.size()-3).getHandleFirstName() : "";
            //显示明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String showDetail2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String showDetail3 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-3).getDowned().subtract(issuesList.get(issuesList.size()-3).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String CallBackName= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String CallBackName1= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCallBack()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
            String CallBackName2= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-3).getCallBack()+" "+issuesList.get(issuesList.size()-3).getCallBackFirstName() : "";
            log.info("newIssueList:{}",newIssueList);
            log.info("issuesList:{}",issuesList);
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetail+operatorName1+CallBackName+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetail2+operatorName2+CallBackName1+"\n"+
                    newIssueList.get(newIssueList.size()-3)+"  "+
                    issuesList.get(issuesList.size()-3).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetail3+operatorName3+CallBackName2;
        } else {
            if (updateAccount.getDown()!=null){
                issue.setDown(updateAccount.getDown());
            }
            issue.setDown(BigDecimal.ZERO);
            issue.setDowned(BigDecimal.ZERO);
            iusseText="\n\n" +"已下发：\n"+ "暂无下发数据";
        }
        //显示操作人/显示明细    updateAccount是新增加的账户信息
        if (accounts.size()==1){
            //是否隐藏操作人 @ +accounts.get(0).getHandle()
            String operatorName = rate.getHandleStatus() == 0 ? "  "+accounts.get(0).getHandleFirstName() : "";
            //是否隐藏明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(0).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String callBackName = rate.getCallBackStatus() == 0 ? " @"+accounts.get(0).getCallBack()+" "+accounts.get(0).getCallBackFirstName() : "";

            return  "\n已入账："+accounts.get(0).getTotal() +"，共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+showDetail+operatorName+callBackName+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDowning().divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "+issue.getDowned().divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ updateAccount.getDown().setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDown().divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U";
        }else if (accounts.size()==2){
            //是否隐藏操作人
            String operatorName = rate.getHandleStatus() == 0 ? "  "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            String operatorName2 = rate.getHandleStatus() == 0 ? "  "+accounts.get(0).getHandleFirstName() : "";
            //是否隐藏明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String showDetail2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(0).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String callBackName = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCallBack()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            String callBackName2 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(0).getCallBack()+" "+accounts.get(0).getCallBackFirstName() : "";
            return "\n已入账："+accounts.get(accounts.size()-1).getTotal().add(accounts.get(accounts.size()-2).getTotal()) +"，:共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(accounts.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+showDetail+operatorName+callBackName+"\n"+
                    newList.get(newList.size()-2)+" "+
                    accounts.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+showDetail2+operatorName2+callBackName2+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().add(accounts.get(accounts.size()-1).getTotal()).setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ updateAccount.getDowning().add(accounts.get(0).getDowning()).setScale(2, RoundingMode.HALF_UP)+"   |    "+updateAccount.getDowning().add(accounts.get(0).getDowning()).divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "+issue.getDowned().divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ updateAccount.getDowning().add(accounts.get(0).getDowning()).subtract(issue.getDowned()).setScale(2, RoundingMode.HALF_UP) +"   |    "+ updateAccount.getDowning().add(accounts.get(0).getDowning()).subtract(issue.getDowned()).divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U";
        }else if (accounts.size()>2){
            //是否隐藏操作人
            String operatorName = rate.getHandleStatus() == 0 ? "  "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            String operatorName2 = rate.getHandleStatus() == 0 ? "  "+accounts.get(accounts.size()-2).getHandleFirstName() : "";
            String operatorName3 = rate.getHandleStatus() == 0 ? "  "+accounts.get(accounts.size()-3).getHandleFirstName() : "";
            //是否隐藏明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String showDetail2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String showDetail3 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-3).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String callBackName = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCallBack()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            String callBackName2 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getCallBack()+" "+accounts.get(accounts.size()-2).getCallBackFirstName() : "";
            String callBackName3 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-3).getCallBack()+" "+accounts.get(accounts.size()-3).getCallBackFirstName() : "";
            //accounts.get(accounts.size()-1).getTotal() 要累加 因为total都是单独记账
            return "\n已入账："+accounts.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add) +"，:共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(accounts.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+showDetail+operatorName+callBackName+"\n"+
                    newList.get(newList.size()-2)+" "+
                    accounts.get(accounts.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+showDetail2+operatorName2+callBackName2+"\n"+
                    newList.get(newList.size()-3)+" "+
                    accounts.get(accounts.size()-3).getTotal().setScale(2, RoundingMode.HALF_UP)+showDetail3+operatorName3+callBackName3+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    //应该用循环写累加 updateAccount.getDowning()
                    "\n应下发："+ accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP)+"   |    "
                    +accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add).divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+

                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "
                    +issue.getDowned().divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+

                    "\n未下发："+ accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP)+"   |    "
                    +accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add).divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U";
        } else {
            //accounts
            return "\n已入账："+accounts.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add)+"，:共"+(accounts.size())+"笔:\n"+
                    " "+ "暂无已入账数据"+
                    iusseText
                    +"已下发:"+issue.getDown()+
                    "\n\n总入账："+ 0+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    //应该用循环写累加 而不是accounts.get(0)
                    "\n应下发："+ 0+"   |    "+0 +"U                  "+
                    "\n已下发："+ issue.getDowned().setScale(2, RoundingMode.HALF_UP)+"   |    "+issue.getDowned().divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP)+"   |    "
                    +accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add).divide(rate.getExchange(),2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP) +"U";
        }

    }


}
