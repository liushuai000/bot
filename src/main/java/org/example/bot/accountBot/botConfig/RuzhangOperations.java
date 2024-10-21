package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    protected StatusService statusService;
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
            rateService.insertRate(rates);
            accountBot.sendMessage(sendMessage,"设置成功,当前费率为："+rate);
        }else if (text.startsWith("设置汇率")){
            if (text.substring(4).startsWith("-")){
                rates.setAddTime(new Date());
                rates.setExchange(new BigDecimal(1));
                rateService.insertRate(rates);
                accountBot.sendMessage(sendMessage,"当前汇率已设置为默认汇率：1");
            }else {
                //如果设置汇率为0 就设置成1
                if (text.substring(4).equals("0")){
                    rates.setExchange(new BigDecimal(1));
                }else {
                    rates.setExchange(new BigDecimal(text.substring(4)));
                }
                rates.setAddTime(new Date());
                rateService.insertRate(rates);
                accountBot.sendMessage(sendMessage,"设置成功,当前汇率为："+text.substring(4));
            }
        }
    }

    //撤销入款
    public void repeal(Message message, SendMessage sendMessage, List<Account> accounts, String replyToText, UserDTO userDTO, List<Issue> issueList) {
        String text = message.getText();
        if (text.length()>=2||replyToText!=null){
            if (text.equals("撤销入款")){
                accountService.deleteInData(accounts.get(accounts.size()-1).getAddTime(),userDTO.getGroupId());
                issueService.updateIssueDown(accounts.get(accounts.size()-1).getDown(),userDTO.getGroupId());
                accountBot.sendMessage(sendMessage,"撤销成功");
                //TODO chishui_id  &&callBackName.equals(username)
            }else if (text.equals("取消")&&replyToText!=null){
                log.info("replyToXXXTentacion:{}",replyToText);
                if (replyToText.charAt(0)=='+'){
                    accountService.deleteInData(accounts.get(accounts.size()-1).getAddTime(),userDTO.getGroupId());
                    issueService.updateIssueDown(accounts.get(accounts.size()-1).getDown(),userDTO.getGroupId());
                }else if (replyToText.charAt(0)=='-'){
                    issueService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime(),userDTO.getGroupId());
                    accountService.updateNewestData(issueList.get(issueList.size()-1).getDown(),userDTO.getGroupId());
                }else {
                    return;
                }
                accountBot.sendMessage(sendMessage,"取消成功");
            }else if (text.equals("撤销下发")){
                issueService.deleteNewestIssue(issueList.get(issueList.size()-1).getAddTime(),userDTO.getGroupId());
                accountService.updateNewestData(issueList.get(issueList.size()-1).getDown(),userDTO.getGroupId());
                accountBot.sendMessage(sendMessage,"撤销成功");
            }
        }
    }


    //入账操作 issue 这个和updateAccount 一样只不过没改名 updateIssue
    public void inHandle(String[] split2, String text, Account updateAccount,SendMessage sendMessage,
                         List<Account> accountList, Message message, String[] split3, Rate rate,
                         Issue issue, List<Issue> issueList, UserDTO userDTO,Status status) {
        BigDecimal total;
        BigDecimal down;
        //判断是否符合公式 true 是匹配
        boolean isMatcher = utils.isMatcher(text);
        // 如果 text 的第一个字符是 '+'，或者 '-'，或者 orNo1 为 true，则继续执行
        if (text.charAt(0) != '+' && text.charAt(0) != '-')  return;
        //+0 -0显示账单
        if (showOperatorName.isEmptyMoney(text)){
            showOperatorName.replay(sendMessage,userDTO,updateAccount,rate,issueList,issue,text,status);
            return;
        }
        BigDecimal num = new BigDecimal(0);
        //当不是公式入账时才赋值
        if (!isMatcher) {
            if (text.substring(1).endsWith("u")||text.substring(1).endsWith("U")){
                String numberPart = text.substring(0, text.length() - 1);
                num=new BigDecimal(numberPart.substring(1)).multiply(rate.getExchange());
                rate.setCalcU(true);//是+30U 的不计算费率
            }else {
                num=new BigDecimal(text.substring(1));
            }
        }
        //判断是否是第一次入账
        if (accountList.size()>0){
            //获取最近一次入账记录，方便后续操作
            updateAccount=accountList.get(accountList.size()-1);
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
        updateAccount.setDowning(downing);
        issue.setDowned(downed);
        issue.setUserId(userDTO.getUserId());
        //如果是回复消息，设置回复人的相关消息 用不用判断空 然后给空字符串
        updateAccount.setCallBackUserId(userDTO.getCallBackUserId());
        //设置account的过期时间
        if(!accountList.isEmpty()){
            if (accountList.get(accountList.size()-1).getSetTime()!=null){
                updateAccount.setSetTime(accountList.get(accountList.size()-1).getSetTime());
            }else {
                if (dateOperator.oldSetTime!=null){
                    updateAccount.setSetTime(dateOperator.oldSetTime);
                }else {
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
        if (isMatcher){
            //公式入账 isMatch
            utils.calcRecorded(text, userDTO.getUserId(),userDTO.getUsername(),userDTO.getGroupId(), updateAccount, total, down,issue,downed,downing);
        }
       if (isMatcher==false && !accountBot.showOperatorName.isEmptyMoney(text)){
            if (firstChar == '+' ){
                //+10u
                updateAccount.setTotal(num);
                updateAccount.setUserId(userDTO.getUserId());
                //计算应下发   num是当前的total  total里包括了以前的金额 所以用num要计算本次的下发
                downing=utils.dowingAccount(num,rate,downing);
                updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
                updateAccount.setDown(downing.subtract(downed));//总入帐-(总入帐*费率)/汇率=应下发- 已下发= 未下发
                updateAccount.setRateId(rate.getId());
                updateAccount.setAccountHandlerMoney(status.getAccountHandlerMoney());
                accountService.insertAccount(updateAccount);
            }else if (firstChar == '-' ){
                issue.setUserId(userDTO.getUserId());
                issue.setRateId(rate.getId());
                issue.setDown(updateAccount.getDowning().subtract(num));
                issue.setDowned(num);
                issue.setCallBackUserId(userDTO.getCallBackUserId());
                issue.setIssueHandlerMoney(status.getIssueHandlerMoney());
                User byUserId = userService.findByUserId(userDTO.getUserId());
                if (byUserId!=null){
                    issueService.insertIssue(issue);
                    accountService.updateDown(updateAccount.getDowning().subtract(num),userDTO.getGroupId());
                    log.info("执行了issue.getHandle()!=null issue--:{}",issue);
                }
            }
        }
        //重新获取最新的数据
        List<Account> accounts = accountService.selectAccountDataStatus0(userDTO.getGroupId());
        List<Issue> issues = issueService.selectIssue(userDTO.getGroupId());
        log.info("issues,,{}",issues);
        //获取时间数据方便后续操作
        List<String> newAccountList = new ArrayList<>();
        List<String> newIssueList=new ArrayList<>();
        for (Account account : accounts) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newAccountList.add(sdf.format(account.getAddTime()));
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
            sendText1 = getSendText(updateAccount, accounts,rate, num, newAccountList,newIssueList,issues,issue,status);
            sendMessage.setText(sendText1);
            new ButtonList().implList(message, sendMessage);
        }
        accountBot.sendMessage(sendMessage,sendText1);
    }


    //入账时发送的消息  显示操作人也用这个
    public String getSendText(Account updateAccount, List<Account> accounts, Rate rate, BigDecimal num, List<String> newList, List<String> newIssueList,
                              List<Issue> issuesList, Issue issue,Status status) {
        String iusseText;
        int issueHandleStatus = 0;
        int issueCallBackStatus = 0;
        int issueDetailStatus = 0;
        if (!issuesList.isEmpty()){
            issueHandleStatus=status.getHandleStatus();
            issueCallBackStatus=status.getCallBackStatus();
            issueDetailStatus=status.getDetailStatus();
        }
        List<String> operatorNameList = this.forOperatorName(issuesList,issueHandleStatus);
        List<String> showDetailList = this.forShowDetail(issuesList,issueDetailStatus);
        List<String> callBackNames = this.forCallBackName(issuesList,issueCallBackStatus);
        StringBuilder issuesStringBuilder = new StringBuilder();
        for (int i = 0; i < status.getShowFew(); i++) {
            if (issuesList.size()>i){
                issuesStringBuilder.append(
                        newIssueList.get(i)+"  "+ issuesList.get(i).getDowned().setScale(2, RoundingMode.HALF_UP)+(showDetailList.isEmpty()? ""
                                : showDetailList.get(i))+operatorNameList.get(i));
                if (!callBackNames.isEmpty()){
                    issuesStringBuilder.append(callBackNames.get(i));
                }
                issuesStringBuilder.append("\n");
            }
        }
        //显示分类 只查询有回复人的
        if (status.getDisplaySort()==0){
            List<Issue> collect = issuesList.stream().filter(Objects::nonNull).filter(issue1 -> isNotBlank(issue1.getCallBackUserId())).collect(Collectors.toList());
            BigDecimal callBackMoney=collect.stream().map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            issuesStringBuilder.append("出账分类："+callBackMoney+"\n");
            for (int i = 0; i < status.getShowFew() ; i++) {
                if (!collect.isEmpty()&& collect.size()>i){
                    Rate rate1=rateService.selectRateByID(collect.get(i).getRateId());
                    String xf;
                    if (status.getShowMoneyStatus()==0){
                        xf=collect.get(i).getDowned()+"\n";
                    }else if (status.getShowMoneyStatus()==1){
                        xf=collect.get(i).getDowned().divide(rate1.getExchange(),2, RoundingMode.HALF_UP)+"U\n";
                    }else {
                        xf=collect.get(i).getDowned()+" | "+collect.get(i).getDowned().divide(rate1.getExchange(),2, RoundingMode.HALF_UP)+"U\n";
                    }
                    User byUserId = userService.findByUserId(collect.get(i).getCallBackUserId());
                    String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
                    String callBackLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
                    String name=callBackFirstName+callBackLastName;
                    String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(collect.get(i).getUserId()), name);
                    issuesStringBuilder.append(format+": "+xf);
                }
            }
        }
        BigDecimal sxfCount2 = new BigDecimal(0);
        if (!issuesList.isEmpty()){
            sxfCount2 =issuesList.stream().map(Issue::getIssueHandlerMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            iusseText="\n已出账："+downed +"，:共"+(issuesList.size())+"笔:\n"+ issuesStringBuilder;
        }else {
            if (updateAccount.getDown()!=null){
                issue.setDown(updateAccount.getDown());
            }
            issue.setDown(BigDecimal.ZERO);
            issue.setDowned(BigDecimal.ZERO);
            iusseText="\n\n" +"已下发：\n"+ "暂无下发数据";
        }
        int accountHandleStatus = 0;
        int accountCallBackStatus = 0;
        int accountDetailStatus = 0;
        if (!accounts.isEmpty()){
            accountHandleStatus=status.getHandleStatus();
            accountCallBackStatus=status.getCallBackStatus();
            accountDetailStatus=status.getDetailStatus();
        }
        List<String> accountOperatorNames = this.forAccountOperatorName(accounts,accountHandleStatus);
        List<String> accountCallBackNames = this.forAccountCallBackName(accounts,accountCallBackStatus);
        List<String> accountDetails = this.forAccountShowDetail(accounts,accountDetailStatus);
        BigDecimal yingxiafa=this.forYingxiafa(accounts);//应下方
        BigDecimal yixiafa=this.forYixiafa(issuesList);//已下方
        if (!accounts.isEmpty()){
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = accounts.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            String yxf;//应下方
            String yixf;//已下发
            String wxf;//未下发
            if (status.getShowMoneyStatus()==0){
                yxf=downing.setScale(2, RoundingMode.HALF_UP)+"";
                yixf=downed.setScale(2, RoundingMode.HALF_UP)+"";
                wxf=downing.subtract(downed).setScale(2, RoundingMode.HALF_UP) +"";
            }else if (status.getShowMoneyStatus()==1){
                yxf=yingxiafa +"U";// \n换行加不加
                yixf=yixiafa +"U";
                wxf=yingxiafa.subtract(yixiafa)+"U";
            }else{
                yxf=downing.setScale(2, RoundingMode.HALF_UP)+"   |    "+yingxiafa +"U";
                yixf= downed.setScale(2, RoundingMode.HALF_UP)+"   |    "+yixiafa +"U";
                wxf=downing.subtract(downed).setScale(2, RoundingMode.HALF_UP) +"   |    "+ yingxiafa.subtract(yixiafa)+"U";
            }
            StringBuilder stringBuilder=new StringBuilder();
            for (int i = 0; i < status.getShowFew(); i++) {
                if (accounts.size()>i){
                    stringBuilder.append(
                            newList.get(i)+" "+ accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP)+" "
                                    +accountDetails.get(i)+" "+accountOperatorNames.get(i)+" ");
                    if (!accountCallBackNames.isEmpty()){
                        stringBuilder.append(accountCallBackNames.get(i));
                    }
                    stringBuilder.append("\n");
                }
            }
            //显示分类
            if (status.getDisplaySort()==0){
                List<Account> collect = accounts.stream().filter(Objects::nonNull).filter(account -> isNotBlank(account.getCallBackUserId())).collect(Collectors.toList());
                BigDecimal callBackMoney =collect.stream().map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
                stringBuilder.append("入款分类："+callBackMoney+"\n");
                for (int i = 0; i < status.getShowFew() ; i++) {
                    if (!collect.isEmpty() && collect.size()>i){
                        Rate rate1=rateService.selectRateByID(collect.get(i).getRateId());
                        String xf;
                        if (status.getShowMoneyStatus()==0){
                            xf=collect.get(i).getTotal()+"\n";
                        }else if (status.getShowMoneyStatus()==1){
                            xf=collect.get(i).getDowning().divide(rate1.getExchange(),2, RoundingMode.HALF_UP)+"U\n";
                        }else {
                            xf=collect.get(i).getTotal()+" | "+collect.get(i).getDowning().divide(rate1.getExchange(),2, RoundingMode.HALF_UP)+"U\n";
                        }
                        User byUserId = userService.findByUserId(collect.get(i).getCallBackUserId());
                        String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
                        String callBackLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
                        String name=callBackFirstName+callBackLastName;
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(collect.get(i).getUserId()), name);
                        stringBuilder.append(format+": "+xf);
                    }
                }
            }
            String sxf = "";
            if (status.getShowHandlerMoneyStatus()==0){
                BigDecimal sxfCount =accounts.stream().map(Account::getAccountHandlerMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
                sxf=    "\n单笔入款手续费："+ status.getAccountHandlerMoney()+
                        "\n单笔下方手续费："+ status.getIssueHandlerMoney()+
                        "\n入款手续费总："+ sxfCount+
                        "\n下发手续费总："+ sxfCount2;//sxf2 是下发手续费
            }
//            入款分类：
            return "\n已入账："+total +"，:共"+(accounts.size())+"笔:\n"+
                    stringBuilder +iusseText+"\n"+
                    "\n\n总入账："+ total.setScale(2, RoundingMode.HALF_UP)+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ yxf+
                    "\n已下发："+ yixf+
                    "\n未下发："+ wxf+sxf;
        } else {
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = accounts.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            String yxf;//应下方
            String yixf;//已下发
            String wxf;//未下发
            if (status.getShowMoneyStatus()==0){
                yxf=downing.setScale(2, RoundingMode.HALF_UP)+"";
                yixf=downed.setScale(2, RoundingMode.HALF_UP)+"";
                wxf=downing.subtract(downed).setScale(2, RoundingMode.HALF_UP) +"";
            }else if (status.getShowMoneyStatus()==1){
                yxf=yingxiafa +"U";
                yixf=yixiafa +"U";
                wxf=yingxiafa.subtract(yixiafa)+"U";
            }else{
                yxf=downing.setScale(2, RoundingMode.HALF_UP)+"   |    "+yingxiafa +"U";
                yixf= downed.setScale(2, RoundingMode.HALF_UP)+"   |    "+yixiafa +"U";
                wxf=downing.subtract(downed).setScale(2, RoundingMode.HALF_UP) +"   |    "+ yingxiafa.subtract(yixiafa)+"U";
            }
            StringBuilder stringBuilder=new StringBuilder();
            if (!accounts.isEmpty()){
                for (int i = 0; i < status.getShowFew(); i++) {
                    if (accounts.size()>i){
                        stringBuilder.append(
                                newList.get(i)+" "+ accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP)+" "
                                        +accountDetails.get(i)+" "+accountOperatorNames.get(i)+" "+accountCallBackNames.get(i)+"\n");
                    }
                }
            }
            String sxf = "";
            if (status.getShowHandlerMoneyStatus()==0){
                BigDecimal sxfCount =accounts.stream().map(Account::getAccountHandlerMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
                sxf=    "\n单笔入款手续费："+ status.getAccountHandlerMoney()+
                        "\n单笔下方手续费："+ status.getIssueHandlerMoney()+
                        "\n入款手续费总："+ sxfCount+
                        "\n下发手续费总："+ sxfCount2;//sxf2 是下发手续费
            }
            return "\n已入账："+total+"，:共"+(accounts.size())+"笔:\n"+
                    " "+ "暂无已入账数据"+ iusseText+
                    "\n\n总入账："+ 0+
                    "\n汇率："+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "\n费率："+ rate.getRate().setScale(2, RoundingMode.HALF_UP)+
                    "\n应下发："+ yxf+
                    "\n已下发："+ yixf+
                    "\n未下发："+ wxf+sxf;
        }

    }

    private BigDecimal forYixiafa(List<Issue> issuesList) {
        BigDecimal temp = new BigDecimal(0);
        for (int i = 0; i < issuesList.size(); i++) {
            Rate rate1=rateService.selectRateByID(issuesList.get(i).getRateId());
            BigDecimal exchange=rate1.getExchange();
            BigDecimal divide = issuesList.get(i).getDowned().divide(exchange, 2, RoundingMode.HALF_UP);
            temp=temp.add(divide);
        }
        return temp;
    }
    //应下方
    private BigDecimal forYingxiafa(List<Account> accounts) {
        BigDecimal temp = new BigDecimal(0);
        for (int i = 0; i < accounts.size(); i++) {
            Rate rate1=rateService.selectRateByID(accounts.get(i).getRateId());
            BigDecimal exchange=rate1.getExchange();
            BigDecimal divide = accounts.get(i).getDowning().divide(exchange, 2, RoundingMode.HALF_UP);
            temp=temp.add(divide);
        }
        return temp;
    }

    //-----------------------------------Account 操作---------------------------------------
    public List<String> forAccountOperatorName(List<Account> accounts,int handleStatus){
        //是否隐藏操作人 @ +accounts.get(0).getHandle()
        List<String> operatorNameList=new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            User byUserId = userService.findByUserId(accounts.get(i).getUserId());
            String operatorFirstName = byUserId.getFirstName()==null ? "": byUserId.getFirstName();
            String operatorNameLast =  byUserId.getLastName()==null ? "": byUserId.getLastName();
            //显示操作人
            String operatorName = handleStatus == 0 ? operatorFirstName+operatorNameLast : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(accounts.get(i).getUserId()), operatorName);
            operatorNameList.add(format);
        }
        return operatorNameList;
    }
    public List<String> forAccountCallBackName(List<Account> accounts,int callBackStatus){
        List<String> callBackNameList=new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getCallBackUserId()==null){
                continue;
            }
            User byUserId = userService.findByUserId(accounts.get(i).getCallBackUserId());
            String callBackFirstName = byUserId.getFirstName() == null ? "" :  byUserId.getFirstName();
            String callBackLastName =  byUserId.getLastName()  == null ? "" :  byUserId.getLastName();
            //显示回复人
            String callBackName= callBackStatus ==0 ? callBackFirstName+callBackLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(accounts.get(i).getUserId()), callBackName);
            callBackNameList.add(format);
        }
        return callBackNameList;
    }
    public List<String> forAccountShowDetail(List<Account> accounts,int detailStatus){
        List<String> showDetailList=new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            Rate rate=rateService.selectRateByID(accounts.get(i).getRateId());
            BigDecimal exchange=rate.getExchange().setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP);
            BigDecimal rateRate = rate.getRate().multiply(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalTimesRate1 = total.multiply(rateRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total2=total.subtract(totalTimesRate1);
            //显示明细
            String showDetail = detailStatus == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+
                    rate.getRate().setScale(2, RoundingMode.HALF_UP)+"=" +
                    total2.divide(exchange,2, RoundingMode.HALF_UP): "";//-不涉及费率
            showDetailList.add(showDetail);
        }
        return showDetailList;
    }
    //-----------------------------------Issue 操作---------------------------------------
    public List<String> forOperatorName(List<Issue> issueList,int handleStatus){
        List<String> operatorNameList=new ArrayList<>();
        for (int i = 0; i < issueList.size(); i++) {
            User byUserId = userService.findByUserId(issueList.get(i).getUserId());
            String handleFirstName = byUserId.getFirstName()==null?"":byUserId.getFirstName();
            String handleLastName =byUserId.getLastName()==null?"":byUserId.getLastName();
            //显示操作人
            String operatorName = handleStatus == 0 ? handleFirstName+handleLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(issueList.get(i).getUserId()), operatorName);
            operatorNameList.add(format);
        }
        return operatorNameList;
    }
    public List<String> forCallBackName(List<Issue> issuesList,int callBackStatus){
        List<String> callBackNameList=new ArrayList<>();
        for (int i = 0; i < issuesList.size(); i++) {
            if (issuesList.get(i).getCallBackUserId()==null){
                continue;
            }
            User byUserId = userService.findByUserId(issuesList.get(i).getCallBackUserId());//查询回复人信息
            String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
            String callBackLastName = byUserId.getLastName() == null ? "" :  byUserId.getLastName();
            //显示回复人
            String callBackName= callBackStatus ==0 ? callBackFirstName+callBackLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(issuesList.get(i).getUserId()), callBackName);
            callBackNameList.add(format);
        }
        return callBackNameList;
    }
    public List<String> forShowDetail(List<Issue> issuesList,int detailStatus){
        List<String> showDetailList=new ArrayList<>();
        for (int i = 0; i < issuesList.size(); i++) {
            Rate rate=rateService.selectRateByID(issuesList.get(i).getRateId());
            BigDecimal exchange=rate.getExchange().setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = issuesList.get(i).getDowned().setScale(2, RoundingMode.HALF_UP);
            //显示明细
            String showDetail = detailStatus == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+
                    "=" + total.divide(exchange,2, RoundingMode.HALF_UP): "";
            if (isNotBlank(showDetail)) {
                showDetailList.add(showDetail);
            }
        }
        return showDetailList;
    }


}
