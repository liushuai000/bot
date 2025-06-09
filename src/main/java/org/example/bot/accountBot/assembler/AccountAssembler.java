package org.example.bot.accountBot.assembler;

import org.example.bot.accountBot.dto.AccountDTO;
import org.example.bot.accountBot.dto.IssueDTO;
import org.example.bot.accountBot.dto.RateDTO;
import org.example.bot.accountBot.pojo.*;

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
        accountDTO.setFirstName(user.getFirstLastName());
        accountDTO.setPm(account.getPm());
        if (rate.isCalcU()){
            accountDTO.setTotal(account.getTotal());
        }else {
            accountDTO.setTotal(account.getTotal());//.subtract(account.getTotal().multiply(rate.getRate().multiply(BigDecimal.valueOf(0.01))))
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
        issueDTO.setFirstName(user.getFirstLastName());
        issueDTO.setDowned(issue.getDowned());
        issueDTO.setRate(rate.getRate());
        issueDTO.setExchange(rate.getExchange());
        issueDTO.setDownRate(issue.getDownRate());
        issueDTO.setDownExchange(issue.getDownExchange());
        issueDTO.setAddTime(issue.getAddTime());
        issueDTO.setPm(issue.getPm());
        if (callBackUser!=null){
            issueDTO.setCallBackUserId(callBackUser.getUserId());
            issueDTO.setCallBackName(callBackUser.getUsername());
            issueDTO.setCallBackFirstName(callBackUser.getFirstName()+callBackUser.getLastName());
        }
        BigDecimal downExchange = issue.getDownExchange();
        BigDecimal downRate = issue.getDownRate(); // 下发费率，如 12 表示 12%
        // 使用自定义汇率或默认汇率
        BigDecimal exchange = downExchange.compareTo(BigDecimal.ZERO) != 0 ? downExchange : rate.getExchange();
//            exchange = exchange.setScale(2, RoundingMode.HALF_UP);
        BigDecimal uValue = issue.getDowned().divide(exchange, 2, RoundingMode.HALF_UP);
        // 如果有费率，扣除对应比例
        if (downRate.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
            BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
            uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP); // 实际到账金额
        }
        issueDTO.setDownedUSDT(uValue);
        issueDTO.setGroupId(issue.getGroupId());
        issueDTO.setUserId(issue.getUserId());
        issueDTO.setMatcher(rate.isMatcher());
        return issueDTO;
    }

    public RateDTO rateToDTO(Rate rate, List<AccountDTO> accountDTOList, List<IssueDTO> issueDTOList, Status status) {
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
                    .map(accountDTO -> accountDTO.getTotal().divide(accountDTO.getExchange(), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            downing = accountDTOList.stream().map(AccountDTO::getDowning)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            downingUSDT = accountDTOList.stream()
                    .map(accountDTO -> accountDTO.getDowning().divide(accountDTO.getExchange(),2,RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (issueDTOList!= null){
            IssueMoney = issueDTOList.stream().map(IssueDTO::getIssueHandlerMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            downed = issueDTOList.stream().map(IssueDTO::getDowned)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//总下发

            downedUSDT = issueDTOList.stream().map(issue -> {
                BigDecimal downExchange = issue.getDownExchange();
                BigDecimal downRate = issue.getDownRate(); // 下发费率，如 12 表示 12%
                // 使用自定义汇率或默认汇率
                BigDecimal exchange = downExchange.compareTo(BigDecimal.ZERO) != 0 ? downExchange : issue.getExchange();
                BigDecimal uValue = issue.getDowned().divide(exchange, 2, RoundingMode.HALF_UP);
                // 如果有费率，扣除对应比例
                if (downRate.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
                    BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
                    uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP); // 实际到账金额
                }
                return uValue;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return new RateDTO()
                .setCount(total)//.subtract(total.multiply(rate.getRate())).multiply(BigDecimal.valueOf(0.01))
                .setDown(downing.subtract(downed))
                .setDowned(downed)
                .setPmoney(status.getPmoney())
                .setDownRate(status.getDownRate())
                .setDownExchange(status.getDownExchange())
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
