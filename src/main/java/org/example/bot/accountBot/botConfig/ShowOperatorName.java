package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.*;
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
    @Autowired
    ButtonList buttonList;

    //显示操作人名字  显示账单用
    public void  replay(SendMessage sendMessage, UserDTO userDTO, Account updateAccount, Rate rate, List<Issue> issuesList, Issue issue,
                        String text, Status status, GroupInfoSetting groupInfoSetting) {
        if (!this.isEmptyMoney(text) && !BaseConstant.showReplay(text) && !BaseConstant.showReplayEnglish(text)&& !BaseConstant.showReplayEnglish2(text)
        && !text.equals("撤销下发") && !text.equals("撤销入款")&& ! text.equals("undo delivery")&& !text.equals("cancel deposit")) return;
        buttonList.implList(sendMessage,userDTO.getGroupId(),userDTO.getGroupTitle(),groupInfoSetting);
        String iusseText="";
        //重新获取最新的数据 +00000
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
        if(new Utils().isMatcher(text)){
            return false;
        }
        if (text.equals("+0")){
            return true;
        } else if (text.equals("-0")) {
            return true;
        } else if (text.equals("+0u")) {
            return true;
        } else if (text.equals("-0u")) {
            return true;
        } else if (text.equals("+0U")) {
            return true;
        } else if (text.equals("-0U")) {
            return true;
        }else if (text.startsWith("+")|| text.startsWith("-")){
            if (text.length()<=1)return false;
            if (text.endsWith("u")){
                if (text.length()<=2)return false;
                else if (text.substring(1, text.length()).endsWith("U")) {
                    String substring = text.substring(1, text.length() - 1);
                    BigDecimal num=new BigDecimal(substring);
                    if (num.compareTo(BigDecimal.ZERO)==0){
                        return true;
                    }else {
                        return false;
                    }
                }
            }else {
                BigDecimal num=new BigDecimal(text.substring(1, text.length()));
                if (num.compareTo(BigDecimal.ZERO)==0){
                    return true;
                }else {
                    return false;
                }
            }
        }else{
            return false;
        }
        return false;
    }
}
