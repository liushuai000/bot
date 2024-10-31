package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.utils.BaseConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

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
    @Autowired
    private DateOperator dateOperator;

    //显示操作人名字  显示账单用
    public void  replay(SendMessage sendMessage, UserDTO userDTO, Account updateAccount, Rate rate, List<Issue> issuesList, Issue issue, String text, Status status) {
        if (!BaseConstant.showReplay(text)) return;
        //TODO message 先给null
        new ButtonList().implList(null, sendMessage);
        String iusseText="";
        //重新获取最新的数据
        List<Account> accounts = dateOperator.selectIsRiqie(sendMessage,status,userDTO.getGroupId());
        List<String> newList = new ArrayList<>();
        List<String> newIssueList=new ArrayList<>();
        for (Account account : accounts) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newList.add(sdf.format(account.getAddTime()));
        }
        //已出账
        BigDecimal num = issuesList.stream().filter(Objects::nonNull).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Issue> issues = dateOperator.selectIsIssueRiqie(sendMessage,status,userDTO.getGroupId());
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
        if (accounts.size()>=1){
            updateAccount=accounts.get(accounts.size()-1);;
        }
        if (issues.size()>=1){
            issue=issues.get(issues.size()-1);;
        }
        //显示操作人
        iusseText = ruzhangOperations.getSendText(updateAccount, accounts, rate, num, newList, newIssueList, issues, issue,status);
        accountBot.sendMessage(sendMessage,iusseText);
    }
    //true 是0 显示账单
    public boolean isEmptyMoney(String text) {
        if (text.startsWith("+0")){
            return true;
        } else if (text.startsWith("-0")) {
            return true;
        } else if (text.startsWith("+0u")) {
            return true;
        } else if (text.startsWith("-0u")) {
            return true;
        } else if (text.startsWith("+0U")) {
            return true;
        } else if (text.startsWith("-0U")) {
            return true;
        }else {
            return false;
        }
    }
}
