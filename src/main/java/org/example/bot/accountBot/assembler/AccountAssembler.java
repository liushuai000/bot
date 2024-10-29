package org.example.bot.accountBot.assembler;

import org.example.bot.accountBot.dto.AccountDTO;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Rate;

/**
 * 入账组装
 */
public class AccountAssembler {


    public AccountDTO accountToDTO(Account account, Rate rate) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(String.valueOf(account.getId()));
        accountDTO.setDowning(account.getDowning());
        accountDTO.setAccountHandlerMoney(account.getAccountHandlerMoney());
        accountDTO.setDown(account.getDown());
        accountDTO.setTotal(account.getTotal());
        accountDTO.setRate(rate.getRate());
        accountDTO.setExchange(rate.getExchange());
        accountDTO.setAddTime(account.getAddTime());
        accountDTO.setRiqie(account.isRiqie());
        accountDTO.setGroupId(account.getGroupId());
        accountDTO.setUserId(account.getUserId());
        accountDTO.setRateId(account.getRateId());
        accountDTO.setCallBackUserId(account.getCallBackUserId());
        accountDTO.setGroupId(account.getGroupId());
        accountDTO.setCalcU(rate.isCalcU());
        accountDTO.setMatcher(rate.isMatcher());
        return accountDTO;
    }


}
