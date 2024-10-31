package org.example.bot.accountBot.assembler;

import org.example.bot.accountBot.dto.AccountDTO;
import org.example.bot.accountBot.dto.IssueDTO;
import org.example.bot.accountBot.dto.RateDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.User;

/**
 * 入账组装
 */
public class AccountAssembler {


    public AccountDTO accountToDTO(Account account, Rate rate, User user,User callBackUser) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(String.valueOf(account.getId()));
        accountDTO.setUsername(user.getUsername());
        accountDTO.setAccountHandlerMoney(account.getAccountHandlerMoney());
        accountDTO.setFirstName(user.getFirstName()+user.getLastName());
        accountDTO.setTotal(account.getTotal());
        accountDTO.setRate(rate.getRate());
        accountDTO.setExchange(rate.getExchange());
        accountDTO.setAddTime(account.getAddTime());
        accountDTO.setCallBackName(callBackUser.getUsername());
        accountDTO.setCallBackFirstName(callBackUser.getFirstName()+callBackUser.getLastName());
        accountDTO.setGroupId(account.getGroupId());
        accountDTO.setUserId(account.getUserId());
        accountDTO.setMatcher(rate.isMatcher());
        return accountDTO;
    }
    public IssueDTO issueToDTO(Issue issue, Rate rate, User user, User callBackUser) {
        IssueDTO issueDTO = new IssueDTO();
        issueDTO.setId(String.valueOf(issue.getId()));
        issueDTO.setUsername(user.getUsername());
        issueDTO.setIssueHandlerMoney(issue.getIssueHandlerMoney());
        issueDTO.setFirstName(user.getFirstName()+user.getLastName());
        issueDTO.setDowned(issue.getDowned());
        issueDTO.setRate(rate.getRate());
        issueDTO.setExchange(rate.getExchange());
        issueDTO.setAddTime(issue.getAddTime());
        issueDTO.setCallBackName(callBackUser.getUsername());
        issueDTO.setCallBackFirstName(callBackUser.getFirstName()+callBackUser.getLastName());
        issueDTO.setGroupId(issue.getGroupId());
        issueDTO.setUserId(issue.getUserId());
        issueDTO.setMatcher(rate.isMatcher());
        return issueDTO;
    }

    public RateDTO rateToDTO(Rate rate) {
        return new RateDTO()
                .setId(rate.getId()+"")
                .setGroupId(rate.getGroupId())
                .setExchange(rate.getExchange())
                .setRate(rate.getRate())
                .setAddTime(rate.getAddTime())
                .setCalcU(rate.isCalcU());
    }
}
