package org.example.bot.accountBot.assembler;

import org.example.bot.accountBot.dto.AccountDTO;
import org.example.bot.accountBot.dto.IssueDTO;
import org.example.bot.accountBot.dto.RateDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.User;

import java.math.BigDecimal;
import java.util.List;

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
        accountDTO.setDowning(account.getDowning());
        accountDTO.setRate(rate.getRate());
        accountDTO.setExchange(rate.getExchange());
        accountDTO.setAddTime(account.getAddTime());
        if (callBackUser!=null){
            accountDTO.setCallBackUserId(callBackUser.getUserId());
            accountDTO.setCallBackName(callBackUser.getUsername());
            accountDTO.setCallBackFirstName(callBackUser.getFirstName()+callBackUser.getLastName());
        }
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
        if (callBackUser!=null){
            issueDTO.setCallBackUserId(callBackUser.getUserId());
            issueDTO.setCallBackName(callBackUser.getUsername());
            issueDTO.setCallBackFirstName(callBackUser.getFirstName()+callBackUser.getLastName());
        }
        issueDTO.setGroupId(issue.getGroupId());
        issueDTO.setUserId(issue.getUserId());
        issueDTO.setMatcher(rate.isMatcher());
        return issueDTO;
    }

    public RateDTO rateToDTO(Rate rate,List<AccountDTO> accountDTOList, List<IssueDTO> issueDTOList) {
        // 计算 accountHandlerMoney 的总和
        BigDecimal AccountMoney = accountDTOList.stream().map(AccountDTO::getAccountHandlerMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal IssueMoney = issueDTOList.stream().map(IssueDTO::getIssueHandlerMoney)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal downed = issueDTOList.stream().map(IssueDTO::getDowned)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//总下发
        BigDecimal total = accountDTOList.stream().map(AccountDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal downing = accountDTOList.stream().map(AccountDTO::getDowning)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RateDTO()
                .setId(rate.getId()+"")
                .setCount(total)
                .setDown(total.subtract(downed))
                .setDowned(downed)
                .setGroupId(rate.getGroupId())
                .setExchange(rate.getExchange())
                .setDowning(downing)//应下方
                .setRate(rate.getRate())
                .setCountHandlerMoney(IssueMoney.add(AccountMoney));
    }
}
