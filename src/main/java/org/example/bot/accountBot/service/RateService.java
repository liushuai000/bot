package org.example.bot.accountBot.service;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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

    public void updateRate(String rate) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.set("rate", rate);
        mapper.update(null, wrapper);
    }

    public void updateExchange(BigDecimal exchange) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.set("exchange", exchange);
        mapper.update(null, wrapper);

    }

    public Rate selectRate() {
        return mapper.selectOne(null);
    }

    public void insertRate(Rate rate) {
        mapper.insert(rate);
    }

    public void updateOverDue(Long overdue) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.set("over_due", overdue);
        mapper.update(null, wrapper);
    }

    public void updateHandleStatus(int handleStatus) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.set("handle_status", handleStatus);
        mapper.update(null, wrapper);
    }

    public void updateCallBackStatus(int callBackStatus) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.set("call_back_status", callBackStatus);
        mapper.update(null, wrapper);
    }

    public void updateDetailStatus(int detailStatus) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.set("detail_status", detailStatus);
        mapper.update(null, wrapper);
    }

    public Rate getInitRate() {
        Rate rate=new Rate();
        rate.setAddTime(new Date());
        //查询Rate
        if (mapper.selectOne(null)!=null){
            rate=mapper.selectOne(null);
            log.info("rates:{}",rate);
        }else {
            Long overdue=3153600000000l;
            rate.setOverDue(overdue);
            rate.setHandleStatus(1);
            rate.setCallBackStatus(1);
            rate.setDetailStatus(1);
            this.insertRate(rate);
        }
        return rate;
    }
}
