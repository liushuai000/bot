package org.example.bot.accountBot.assembler;

import org.example.bot.accountBot.dto.AccountDTO;
import org.example.bot.accountBot.dto.IssueDTO;
import org.example.bot.accountBot.dto.RateDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        if (rate.isCalcU()){
            accountDTO.setTotal(account.getTotal());
        }else {
            accountDTO.setTotal(account.getTotal().subtract(account.getTotal().multiply(rate.getRate().multiply(BigDecimal.valueOf(0.01)))));
        }
        accountDTO.setDowning(account.getDowning());//.divide(rate.getExchange(), 2, RoundingMode.HALF_UP)
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
        BigDecimal AccountMoney=new BigDecimal(0);
        BigDecimal total=new BigDecimal(0);
        BigDecimal downing=new BigDecimal(0);
        BigDecimal downed=new BigDecimal(0);
        BigDecimal IssueMoney=new BigDecimal(0);
        BigDecimal totalUSDT=new BigDecimal(0);
        BigDecimal downingUSDT=new BigDecimal(0);
        BigDecimal downedUSDT=new BigDecimal(0);
        if (accountDTOList!= null){
            // 计算 accountHandlerMoney 的总和
            AccountMoney = accountDTOList.stream().map(AccountDTO::getAccountHandlerMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            total = accountDTOList.stream().map(AccountDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalUSDT = accountDTOList.stream()
                    .map(accountDTO -> accountDTO.getTotal().divide(accountDTO.getExchange()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            downing = accountDTOList.stream().map(AccountDTO::getDowning)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            downingUSDT = accountDTOList.stream()
                    .map(accountDTO -> accountDTO.getDowning().divide(accountDTO.getExchange()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (issueDTOList!= null){
            IssueMoney = issueDTOList.stream().map(IssueDTO::getIssueHandlerMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            downed = issueDTOList.stream().map(IssueDTO::getDowned)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//总下发
            downedUSDT = issueDTOList.stream()
                    .map(issueDTO -> issueDTO.getDowned().divide(issueDTO.getExchange()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return new RateDTO()
                .setCount(total)//.subtract(total.multiply(rate.getRate())).multiply(BigDecimal.valueOf(0.01))
                .setDown(downing.subtract(downed))
                .setDowned(downed)
                .setGroupId(rate.getGroupId())
                .setExchange(rate.getExchange())
                .setCountUSDT(totalUSDT)
                .setDownedUSDT(downedUSDT)
                .setDownUSDT(downingUSDT.subtract(downedUSDT))
                .setDowningUSDT(downingUSDT)
                .setDowning(downing)//应下方
                .setRate(rate.getRate())
                .setCountHandlerMoney(IssueMoney.add(AccountMoney));
    }
}
