package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
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
@Service
public class ShowOperatorName {
    @Autowired
    AccountService accountService;
    @Autowired
    IssueService issueService;
    @Autowired
    AccountBot accountBot;
    @Autowired
    RuzhangOperations ruzhangOperations;

    //显示操作人名字
    public void  replay(SendMessage sendMessage, Account updateAccount, Rate rate, List<Issue> issuesList, Issue issue, String text) {
        if (!text.equals("显示操作人名字") && !text.equals("显示操作人名称")){
            return;
        }
        //TODO message 先给null
        new ButtonList().implList(null, sendMessage);
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
        iusseText = ruzhangOperations.getSendText(updateAccount, accounts, rate, num, newList, newIssueList, issuesList, issue);
        accountBot.sendMessage(sendMessage,iusseText);
    }

}
