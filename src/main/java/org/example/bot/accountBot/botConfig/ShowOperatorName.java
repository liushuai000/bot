package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 显示操作人名字
 */
@Slf4j

public class ShowOperatorName {
    @Resource
    AccountService accountService;
    @Resource
    IssueService issueService;
    //显示操作人名字
    public void  replay(SendMessage sendMessage, Account updateAccount, Rate rate, List<Issue> issuesList, Issue issue, String text) {
        if (!text.equals("显示操作人名字")){
            return;
        }
        String iusseText="";
        //重新获取最新的数据
        List<Account> accounts = accountService.selectAccount();
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

        List<Issue> issues = issueService.selectIssue();
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
            //显示操作人 rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+
            String text1 = issuesList.get(issuesList.size()-1).getHandleFirstName();
            //显示明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            //显示回复人
            String callBackFirstName= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            iusseText="\n已出账："+num +"，:共"+(issuesList.size())+"笔:\n"+
                    newIssueList.get(newIssueList.size()-1)+"  "+
                    issuesList.get(issuesList.size()-1).getDowned().setScale(2, RoundingMode.HALF_UP)+text2+text1+" "+callBackFirstName+accounts.get(0).getHandleFirstName();
        }else if (issuesList.size()==2){
            //操作人的显示状态，1表示不显示，0表示显示    操作人昵称
            String handleFirstName = issuesList.get(issuesList.size()-1).getHandleFirstName();
            //handleFirstName rate.getHandleStatus() == 0 ? " @"+issuesList.get(issuesList.size()-1).getHandle()+" "+
            String handleFirstName2 = issuesList.get(issuesList.size()-1).getHandleFirstName();
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-1).getDowned().subtract(issuesList.get(issuesList.size()-1).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    issuesList.get(issuesList.size()-2).getDowned().subtract(issuesList.get(issuesList.size()-2).getDowned().multiply(rate.getRate())).divide(rate.getExchange(), 2, BigDecimal.ROUND_HALF_UP) : "";
            String text31= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-1).getCallBack()+" "+issuesList.get(issuesList.size()-1).getCallBackFirstName() : "";
            String text32= rate.getCallBackStatus() ==0 ? " @"+issuesList.get(issuesList.size()-2).getCallBack()+" "+issuesList.get(issuesList.size()-2).getCallBackFirstName() : "";
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
        List<Account> allAccount = accountService.selectAccount();
        //显示操作人/显示1明细
        if (accounts.size()==1){
            //是否隐藏操作人
            String text1 = rate.getHandleStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getHandle()+" "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            updateAccount=allAccount.get(0);
            //是否隐藏明细
            String text2 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text3 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCallBack()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
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
            String text11 = rate.getHandleStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getHandle()+" "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandleStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getHandle()+" "+accounts.get(accounts.size()-2).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCallBack()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getCallBack()+" "+accounts.get(accounts.size()-2).getCallBackFirstName() : "";
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
            String text11 = rate.getHandleStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getHandle()+" "+accounts.get(accounts.size()-1).getHandleFirstName() : "";
            String text12 = rate.getHandleStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getHandle()+" "+accounts.get(accounts.size()-2).getHandleFirstName() : "";
            String text13 = rate.getHandleStatus() == 0 ? " @"+accounts.get(accounts.size()-3).getHandle()+" "+accounts.get(accounts.size()-3).getHandleFirstName() : "";
            //是否隐藏明细
            String text21 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-1).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text22 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-2).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            String text23 = rate.getDetailStatus() == 0 ? "/"+ rate.getExchange().setScale(2, RoundingMode.HALF_UP)+"*"+ rate.getRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)+"=" +
                    accounts.get(accounts.size()-3).getDowning().setScale(2, RoundingMode.HALF_UP) : "";
            //是否显示回复人
            String text31 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-1).getCallBack()+" "+accounts.get(accounts.size()-1).getCallBackFirstName() : "";
            String text32 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-2).getCallBack()+" "+accounts.get(accounts.size()-2).getCallBackFirstName() : "";
            String text33 = rate.getCallBackStatus() == 0 ? " @"+accounts.get(accounts.size()-3).getCallBack()+" "+accounts.get(accounts.size()-3).getCallBackFirstName() : "";
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
//                String sendText1 = getSendText(updateAccount, accounts,rate, num, newList,newIssueList,issues,issue,text);

    }

}
