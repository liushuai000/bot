package org.example.bot.accountBot.service;


import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.RateMapper;
import org.example.bot.accountBot.pojo.Rate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@Service
public class RateService {

    @Autowired
    RateMapper mapper;

    public void updateRate(String updateAccount) {
        mapper.updateRate(updateAccount);
    }

    public void updateExchange(BigDecimal updateAccount) {
        mapper.updateExchange(updateAccount);
    }

    public Rate selectRate() {
        return mapper.selectRate();
    }

    public void insertRate(Rate rates) {
        mapper.insertRate(rates);
    }

    public void updateOverDue(Long overdue) {
        mapper.updateOverDue(overdue);
    }

    public void updateHandleStatus(int i) {
        mapper.updateHandleStatus(i);
    }

    public void updateCallBackStatus(int i) {
        mapper.updateCallBackStatus(i);
    }

    public void updateDatilStatus(int i) {
        mapper.updateDatilStatus(i);
    }

    public Rate getInitRate() {
        Rate rate=new Rate();
        rate.setAddTime(new Date());
        //查询Rate
        if (mapper.selectRate()!=null){
            rate=mapper.selectRate();
            log.info("rates:{}",rate);
        }else {
            Long overdue=3153600000000l;
            rate.setOverDue(overdue);
            rate.setHandlestatus(1);
            rate.setCallBackStatus(1);
            rate.setDetailStatus(1);
            mapper.insertRate(rate);
        }
        return rate;
    }
}
