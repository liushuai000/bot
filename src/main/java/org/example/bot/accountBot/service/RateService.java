package org.example.bot.accountBot.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public Rate selectRateByID(int rateId) {
        return mapper.selectById(rateId);//需要order by addTime 吗
    }
    public List<Rate> selectRateList() {
        QueryWrapper<Rate> queryWrapper = new QueryWrapper();
        //只查询不是公式入账的的rate 因为要获取最新的并且不是公式入账的汇率和费率计算
        queryWrapper.eq("is_matcher", false);
        queryWrapper.orderByDesc("add_time");
        queryWrapper.last("LIMIT 2");
        return mapper.selectList(queryWrapper);
    }
    public List<Rate> selectNewTimeRateList() {
        QueryWrapper<Rate> queryWrapper = new QueryWrapper();
        //只查询不是公式入账的的rate 因为要获取最新的并且不是公式入账的汇率和费率计算
//        queryWrapper.eq("is_matcher", false);
        queryWrapper.orderByDesc("add_time");
        queryWrapper.last("LIMIT 1");
        return mapper.selectList(queryWrapper);
    }

    public void insertRate(Rate rate) {
        mapper.insert(rate);
    }

    public void updateOverDue(Date overdue) {
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
        //查询Rate 不是公式入账的
        if (!selectRateList().isEmpty()){
            rate=selectRateList().get(0);
            log.info("rates:{}",rate);
        }else {
            Date overdue=new Date();
            rate.setOverDue(overdue);
            rate.setHandleStatus(1);
            rate.setCallBackStatus(1);
            rate.setDetailStatus(1);
            this.insertRate(rate);
        }
        return rate;
    }
    public void setInitRate(Rate rate) {
        rate.setAddTime(new Date());
        Date overdue=new Date();
        rate.setOverDue(overdue);
        rate.setHandleStatus(1);
        rate.setCallBackStatus(1);
        rate.setDetailStatus(1);
    }


}
