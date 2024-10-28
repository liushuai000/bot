package org.example.bot.accountBot.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.RateMapper;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@Service
public class RateService {

    @Autowired
    RateMapper mapper;
    @Autowired
    StatusService statusService;

    public void updateRate(String rate,String groupId) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.eq("group_id", groupId);
        wrapper.set("rate", rate);
        mapper.update(null, wrapper);
    }

    public void updateExchange(BigDecimal exchange,String groupId) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.eq("group_id", groupId);
        wrapper.set("exchange", exchange);
        mapper.update(null, wrapper);

    }
    public Rate selectRateByID(int rateId) {
        return mapper.selectById(rateId);//需要order by addTime 吗
    }
    public List<Rate> selectRateList(String groupId) {
        QueryWrapper<Rate> queryWrapper = new QueryWrapper();
        //只查询不是公式入账的的rate 因为要获取最新的并且不是公式入账的汇率和费率计算
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("is_matcher", false);
        queryWrapper.orderByDesc("add_time");
        queryWrapper.last("LIMIT 1");
        return mapper.selectList(queryWrapper);
    }
//    public List<Rate> selectNewTimeRateList() {
//        QueryWrapper<Rate> queryWrapper = new QueryWrapper();
//        //只查询不是公式入账的的rate 因为要获取最新的并且不是公式入账的汇率和费率计算
////        queryWrapper.eq("is_matcher", false);
//        queryWrapper.orderByDesc("add_time");
//        queryWrapper.last("LIMIT 1");
//        return mapper.selectList(queryWrapper);
//    }

    public void insertRate(Rate rate) {
        mapper.insert(rate);
    }

    public void updateOverDue(Date overdue,String groupId) {
        UpdateWrapper<Rate> wrapper = new UpdateWrapper();
        wrapper.eq("group_id", groupId);
        wrapper.le("add_time", overdue);//添加时间小于设置的日切时间
        wrapper.set("over_due", overdue);
        mapper.update(null, wrapper);
    }

    public Rate getInitRate(String groupId) {
        Rate rate=new Rate();
        //-1秒因为设置汇率的时候此时rate表是空 会插入两条rate  一条初始化一条设置汇率的记录
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime localDateTime = now.plusSeconds(-1);
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        Date date = Date.from(zdt.toInstant());
        rate.setAddTime(date);
        List<Rate> rates = selectRateList(groupId);
        //查询Rate 不是公式入账的
        if (!rates.isEmpty()){
            rate=rates.get(0);
            log.info("rates:{}",rate);
        }else {
            rate.setExchange(new BigDecimal(1));
            rate.setGroupId(groupId);
            this.insertRate(rate);
        }
        return rate;
    }
    public void setInitRate(Rate rate) {
        //是公式入账
        rate.setMatcher(true);
        rate.setAddTime(new Date());
    }


}
