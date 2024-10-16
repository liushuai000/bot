package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
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

import javax.annotation.Resource;
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
    @Resource
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
                //TODO chishui_id  &&callBackName.equals(username)
            }else if (text.equals("取消")&&replyToText!=null){
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
    public void inHandle(String[] split2, String text, Account updateAccount,SendMessage sendMessage,
                         List<Account> accountList, Message message, String[] split3, Rate rate,
                         Issue issue, List<Issue> issueList, UserDTO userDTO) {
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
            if (userDTO.getCallBackName()==null){
                updateAccount.setCallBack(" ");
                updateAccount.setCallBackFirstName(" ");
                updateAccount.setCallBackLastName(" ");
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
        updateAccount.setHandleFirstName(userDTO.getFirstName());
        updateAccount.setHandleLastName(userDTO.getLastName());
        issue.setHandle(userDTO.getUsername());
        issue.setUserId(userDTO.getUserId());
        issue.setHandleFirstName(userDTO.getFirstName());
        issue.setHandleLastName(userDTO.getLastName());
        //如果是回复消息，设置回复人的相关消息
//        if (userDTO.getCallBackName()!=null){
            updateAccount.setCallBack(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
            updateAccount.setCallBackFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
            updateAccount.setCallBackLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
//        }
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
        utils.calcRecorded(text, userDTO.getUserId(),userDTO.getUsername(), updateAccount, total, down,issue,downed,downing,rate);
        if (isMatcher==false){
            //判断是+还是-  ||callBackName.equals(username)
            if (firstChar == '+' ){
                //+10u
                updateAccount.setTotal(num);
                updateAccount.setUserId(userDTO.getUserId());
                updateAccount.setHandle(userDTO.getUsername());
                //计算应下发   num是当前的total  total里包括了以前的金额 所以用num要计算本次的下发
                downing=utils.dowingAccount(num,rate,downing);
                updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
                updateAccount.setDown(downing.subtract(downed));//总入帐-(总入帐*费率)/汇率=应下发- 已下发= 未下发
                accountService.insertAccount(updateAccount);
//            issueService.insertIssue(issue);
//            issueService.updateIssueDown(down.add(num));
                //||callBackName.equals("zqzs18bot")
            }else if (firstChar == '-' ){
                issue.setHandle(userDTO.getUsername());
                issue.setUserId(userDTO.getUserId());
                issue.setDown(updateAccount.getDowning().subtract(num));
                issue.setDowned(num);
                if (issue.getHandle()!=null){
                    issueService.insertIssue(issue);
                    accountService.updateDown(updateAccount.getDowning().subtract(num));
                    log.info("执行了issue.getHandle()!=null issue--:{}",issue);
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
            sendMessage.setText(sendText1);
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
        List<String> operatorNameList = this.forOperatorName(issuesList,rate);
        List<String> showDetailList = this.forShowDetail(issuesList, rate);
        List<String> callBackNames = this.forCallBackName(issuesList, rate);
        if (issuesList.size()==1){
            iusseText="\n已出账："+issuesList.get(0).getDowned() +"，:共"+(issuesList.size())+"笔:\n"+ newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetailList+operatorNameList.get(0)+callBackNames.get(0);
        }else if (issuesList.size()==2){
            iusseText="\n已出账："+issuesList.get(1).getDowned() +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(1).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetailList.get(1)+operatorNameList.get(1)+callBackNames.get(1)+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(0).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetailList.get(0)+operatorNameList.get(0)+callBackNames.get(0);
        }else if (issuesList.size()>2){
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetailList.get(issuesList.size()-1)+operatorNameList.get(issuesList.size()-1)+callBackNames.get(issuesList.size()-1)+"\n"+
                    newIssueList.get(newIssueList.size()-2)+"  "+
                    issuesList.get(issuesList.size()-2).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetailList.get(issuesList.size()-2)+operatorNameList.get(issuesList.size()-2)+callBackNames.get(issuesList.size()-2)+"\n"+
                    newIssueList.get(newIssueList.size()-3)+"  "+
                    issuesList.get(issuesList.size()-3).getDowned().setScale(2, RoundingMode.HALF_UP)+showDetailList.get(issuesList.size()-3)+operatorNameList.get(issuesList.size()-3)+callBackNames.get(issuesList.size()-3);
        } else {
            if (updateAccount.getDown()!=null){
                issue.setDown(updateAccount.getDown());
            }
            issue.setDown(BigDecimal.ZERO);
            issue.setDowned(BigDecimal.ZERO);
            iusseText="\n\n" +"已下发：\n"+ "暂无下发数据";
        }
        List<String> accountOperatorNames = this.forAccountOperatorName(accounts, rate);
        List<String> accountCallBackNames = this.forAccountCallBackName(accounts, rate);
        List<String> accountDetails = this.forAccountShowDetail(accounts, rate);
        if (accounts.size()==1){
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            //未下发
            BigDecimal down = accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add);
            return  "\n已入账："+accounts.get(0).getTotal() +"，共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+" "+accountDetails.get(0)
                    +accountOperatorNames.get(0)+accountCallBackNames.get(0)+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ downing.setScale(2, RoundingMode.HALF_UP)+"   |    "+downing.divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ downed.setScale(2, RoundingMode.HALF_UP)+"   |    "+downed.divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ downing.subtract(downed).setScale(2, RoundingMode.HALF_UP)+"   |    "+downing.subtract(downed).divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U";
        }else if (accounts.size()==2){
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            //未下发
            BigDecimal down = accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add);
            return "\n已入账："+accounts.get(accounts.size()-1).getTotal().add(accounts.get(accounts.size()-2).getTotal()) +"，:共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(1).getTotal().setScale(2, RoundingMode.HALF_UP)+" "+accountDetails.get(1)+accountOperatorNames.get(1)+accountCallBackNames.get(1)+"\n"+
                    newList.get(newList.size()-2)+" "+
                    accounts.get(0).getTotal().setScale(2, RoundingMode.HALF_UP)+" "+accountDetails.get(0)+accountOperatorNames.get(0)+accountCallBackNames.get(0)+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().add(accounts.get(1).getTotal()).setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ downing.setScale(2, RoundingMode.HALF_UP)+"   |    "+downing.divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ downed.setScale(2, RoundingMode.HALF_UP)+"   |    "+downed.divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ downing.subtract(downed).setScale(2, RoundingMode.HALF_UP) +"   |    "+ downing.subtract(downed).divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U";
        }else if (accounts.size()>2){
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            //未下发
            BigDecimal down = accounts.stream().filter(Objects::nonNull).map(Account::getDown).reduce(BigDecimal.ZERO, BigDecimal::add);
            //accounts.get(accounts.size()-1).getTotal() 要累加 因为total都是单独记账
            return "\n已入账："+accounts.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add) +"，:共"+(accounts.size())+"笔:\n"+
                    newList.get(newList.size()-1)+" "+
                    accounts.get(accounts.size()-1).getTotal().setScale(2, RoundingMode.HALF_UP)+" "+accountDetails.get(accounts.size()-1)+accountOperatorNames.get(accounts.size()-1)+accountCallBackNames.get(accounts.size()-1)+"\n"+
                    newList.get(newList.size()-2)+" "+
                    accounts.get(accounts.size()-2).getTotal().setScale(2, RoundingMode.HALF_UP)+" "+accountDetails.get(accounts.size()-2)+accountOperatorNames.get(accounts.size()-2)+accountCallBackNames.get(accounts.size()-2)+"\n"+
                    newList.get(newList.size()-3)+" "+
                    accounts.get(accounts.size()-3).getTotal().setScale(2, RoundingMode.HALF_UP)+" "+accountDetails.get(accounts.size()-3)+accountOperatorNames.get(accounts.size()-3)+accountCallBackNames.get(accounts.size()-3)+"\n"+iusseText+"\n"+
                    "\n\n总入账："+ updateAccount.getTotal().setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    //应该用循环写累加 updateAccount.getDowning()
                    "\n应下发："+ downing.setScale(2, RoundingMode.HALF_UP)+"   |    " +downing.divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U"+
                    "\n已下发："+ downed.setScale(2, RoundingMode.HALF_UP)+"   |    " +downed.divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U"+
                    "\n未下发："+ downing.subtract(downed).setScale(2, RoundingMode.HALF_UP) +"   |    "+ downing.subtract(downed).divide(rate.getExchange(),2, RoundingMode.HALF_UP) +"U";
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
    //-----------------------------------Account 操作---------------------------------------
    public List<String> forAccountOperatorName(List<Account> accounts,Rate rate){
        //是否隐藏操作人 @ +accounts.get(0).getHandle()
        List<String> operatorNameList=new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            String operatorFirstName = accounts.get(i).getHandleFirstName()==null ? "": accounts.get(i).getHandleFirstName();
            String operatorNameLast =  accounts.get(i).getHandleLastName()==null ? "": accounts.get(i).getHandleLastName();
            //显示操作人
            String operatorName = rate.getHandleStatus() == 0 ? operatorFirstName+operatorNameLast : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(accounts.get(i).getUserId()), operatorName);
            operatorNameList.add(format);
        }
        return operatorNameList;
    }
    public List<String> forAccountCallBackName(List<Account> accounts,Rate rate){
        List<String> callBackNameList=new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            String callBackFirstName = accounts.get(i).getCallBackFirstName() == null ? "" : accounts.get(i).getCallBackFirstName();
            String callBackLastName = accounts.get(i).getCallBackLastName() == null ? "" : accounts.get(i).getCallBackLastName();
            //显示回复人
            String callBackName= rate.getCallBackStatus() ==0 ? callBackFirstName+callBackLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(accounts.get(i).getUserId()), callBackName);
            callBackNameList.add(format);
        }
        return callBackNameList;
    }
    public List<String> forAccountShowDetail(List<Account> accounts,Rate rate){
        List<String> showDetailList=new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            //显示明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(i).getDowning().subtract(accounts.get(i).getDowning().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            showDetailList.add(showDetail);
        }
        return showDetailList;
    }
    //-----------------------------------Issue 操作---------------------------------------
    public List<String> forOperatorName(List<Issue> issueList,Rate rate){
        List<String> operatorNameList=new ArrayList<>();
        for (int i = 0; i < issueList.size(); i++) {
            String handleFirstName = issueList.get(i).getHandleFirstName()==null?"":issueList.get(i).getHandleFirstName();
            String handleLastName =issueList.get(i).getHandleLastName()==null?"":issueList.get(i).getHandleLastName();
            //显示操作人
            String operatorName = rate.getHandleStatus() == 0 ? handleFirstName+handleLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(issueList.get(i).getUserId()), operatorName);
            operatorNameList.add(format);
        }
        return operatorNameList;
    }
    public List<String> forCallBackName(List<Issue> issuesList,Rate rate){
        List<String> callBackNameList=new ArrayList<>();
        for (int i = 0; i < issuesList.size(); i++) {
            String callBackFirstName = issuesList.get(i).getCallBackFirstName() == null ? "" : issuesList.get(i).getCallBackFirstName();
            String callBackLastName = issuesList.get(i).getCallBackLastName() == null ? "" : issuesList.get(i).getCallBackLastName();
            //显示回复人
            String callBackName= rate.getCallBackStatus() ==0 ? callBackFirstName+callBackLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(issuesList.get(i).getUserId()), callBackName);
            callBackNameList.add(format);
        }
        return callBackNameList;
    }
    public List<String> forShowDetail(List<Issue> issuesList,Rate rate){
        List<String> showDetailList=new ArrayList<>();
        for (int i = 0; i < issuesList.size(); i++) {
            //显示明细
            String showDetail = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(i).getDowned().subtract(issuesList.get(i).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            showDetailList.add(showDetail);
        }
        return showDetailList;
    }


}
