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
        accountDTO.setCalcU(rate.isCalcU());
        accountDTO.setTotal(account.getTotal());
        if (rate.getRate().compareTo(BigDecimal.ZERO) != 0 && !accountDTO.getCalcU()){
            BigDecimal feePercent = rate.getRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
            BigDecimal feeAmount = account.getTotal().multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
            accountDTO.setTotalUSDT(account.getTotal().subtract(feeAmount).setScale(2, RoundingMode.HALF_UP)); // 实际到账金额
        }else {
            accountDTO.setTotalUSDT(account.getTotal());
        }
        accountDTO.setDowning(account.getDowning());//.divide(rate.getExchange(), 2, RoundingMode.HALF_UP)
        accountDTO.setRate(rate.getRate());
        accountDTO.setExchange(rate.getExchange());
        accountDTO.setAddTime(account.getAddTime());
        if (callBackUser!=null){
            accountDTO.setCallBackUserId(callBackUser.getUserId());
            accountDTO.setCallBackName(callBackUser.getUsername());
            accountDTO.setCallBackFirstName(callBackUser.getFirstName()+(callBackUser.getLastName()==null?"":callBackUser.getLastName()));
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
        issueDTO.setCalcU(rate.isCalcU());
        if (rate.isMatcher()){
            issueDTO.setRate(rate.getRate());
        }else{
            issueDTO.setRate(new BigDecimal(0));
        }
        issueDTO.setExchange(rate.getExchange());
        issueDTO.setDownRate(issue.getDownRate());
        issueDTO.setDownExchange(issue.getDownExchange());
        issueDTO.setAddTime(issue.getAddTime());
        issueDTO.setPm(issue.getPm());
        if (callBackUser!=null){
            issueDTO.setCallBackUserId(callBackUser.getUserId());
            issueDTO.setCallBackName(callBackUser.getUsername());
            issueDTO.setCallBackFirstName(callBackUser.getFirstName()+(callBackUser.getLastName()==null?"":callBackUser.getLastName()));
        }
        BigDecimal downExchange = issue.getDownExchange();
        BigDecimal downRate = issue.getDownRate(); // 下发费率，如 12 表示 12%
        // 使用自定义汇率或默认汇率
        BigDecimal exchange = downExchange.compareTo(BigDecimal.ZERO) != 0 ? downExchange : rate.getExchange();
        BigDecimal uValue = issue.getDowned().divide(exchange, 2, RoundingMode.HALF_UP);
        // 如果有费率，扣除对应比例
        if (downRate.compareTo(BigDecimal.ZERO) != 0 && !rate.isMatcher()) {
            if (!issueDTO.getCalcU()){
                BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
                BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
                uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP); // 实际到账金额
            }
        }else if (rate.isMatcher() && rate.getRate().compareTo(BigDecimal.ZERO) != 0){
            if (!issueDTO.getCalcU()){
                BigDecimal feePercent = rate.getRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
                BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
                uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP); // 实际到账金额
            }
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
        BigDecimal downedPmAdd=new BigDecimal(0);//手动添加的
        BigDecimal downedPmDelete=new BigDecimal(0);//手动添加的 减去
        if (accountDTOList!= null){
            // 计算 accountHandlerMoney 的总和
            AccountMoney = accountDTOList.stream().filter(c-> !c.getPm()).map(AccountDTO::getAccountHandlerMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            total = accountDTOList.stream().filter(c-> !c.getPm()).map(AccountDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalUSDT = accountDTOList.stream().filter(c-> !c.getPm())
                    .map(accountDTO -> accountDTO.getTotal().divide(accountDTO.getExchange(), 2, RoundingMode.HALF_UP))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            downedPmAdd= accountDTOList.stream().filter(c-> c.getPm()).map(AccountDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            downing = accountDTOList.stream().filter(c -> !c.getPm()).map(dto -> {
                BigDecimal total0 = dto.getTotal();
                BigDecimal rate0 = dto.getRate();
                if (rate0.compareTo(BigDecimal.ZERO) != 0) {
                    if (!dto.getCalcU()){
                        BigDecimal feePercent = rate0.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                        BigDecimal feeAmount = total0.multiply(feePercent).setScale(2, RoundingMode.HALF_UP);
                        total0 = total0.subtract(feeAmount);
                    }
                }
                return total0;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
            downingUSDT = accountDTOList.stream().filter(c -> !c.getPm()).map(dto -> {
                BigDecimal total0 = dto.getTotal();
                BigDecimal rate0 = dto.getRate();
                BigDecimal exchange = dto.getExchange();
                if (rate0.compareTo(BigDecimal.ZERO) != 0) {
                    if (!dto.getCalcU()){
                        BigDecimal feePercent = rate0.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                        BigDecimal feeAmount = total0.multiply(feePercent).setScale(2, RoundingMode.HALF_UP);
                        total0 = total0.subtract(feeAmount);
                    }
                }
                return total0.divide(exchange, 2, RoundingMode.HALF_UP);
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (issueDTOList!= null){
            IssueMoney = issueDTOList.stream().filter(c-> !c.getPm()).map(IssueDTO::getIssueHandlerMoney)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            downedPmDelete= issueDTOList.stream().filter(c-> c.getPm()).map(IssueDTO::getDowned)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            for (IssueDTO issue1T : issueDTOList) {
                if (issue1T != null && !issue1T.getPm()) {
                    BigDecimal downRate = issue1T.getDownRate(); // 下发费率
                    BigDecimal uValue = issue1T.getDowned();
                    // 如果有公式费率 先计算公式，扣除对应比例
                    if (issue1T.getRate().compareTo(BigDecimal.ZERO) != 0 && issue1T.isMatcher()){
                        if (!issue1T.getCalcU()){
                            BigDecimal feePercent = issue1T.getRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).stripTrailingZeros(); // 转换为小数
                            BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros(); // 扣除的手续费
                            uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros(); // 实际到账金额
                        }
                    } else  if (downRate.compareTo(BigDecimal.ZERO) != 0 && !issue1T.isMatcher()) {
                        if (!issue1T.getCalcU()) {
                            BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                            BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP);
                            uValue = uValue.subtract(feeAmount);
                        }
                    }
                    downed = downed.add(uValue);
                }
            }
            downedUSDT = issueDTOList.stream().filter(c-> !c.getPm()).map(issue -> {
                BigDecimal downExchange = issue.getDownExchange();
                BigDecimal downRate = issue.getDownRate(); // 下发费率，如 12 表示 12%
                // 使用自定义汇率或默认汇率
                BigDecimal exchange = downExchange.compareTo(BigDecimal.ZERO) != 0 ? downExchange : issue.getExchange();
                BigDecimal uValue = issue.getDowned().divide(exchange, 2, RoundingMode.HALF_UP);
                if (issue.getRate().compareTo(BigDecimal.ZERO) != 0 && issue.isMatcher()){
                    // 如果有费率，扣除对应比例
                    if (!issue.getCalcU()){
                        BigDecimal feePercent = issue.getRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
                        BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
                        uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP); // 实际到账金额
                    }
                } else if (downRate.compareTo(BigDecimal.ZERO) != 0 && !issue.isMatcher()) {
                    if (!issue.getCalcU()){
                        BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
                        BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP); // 扣除的手续费
                        uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP); // 实际到账金额
                    }
                }
                return uValue;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return new RateDTO()
                .setCount(total)//.subtract(total.multiply(rate.getRate())).multiply(BigDecimal.valueOf(0.01))
                .setDown(downing.subtract(downed).add(downedPmAdd.add(downedPmDelete)))
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
