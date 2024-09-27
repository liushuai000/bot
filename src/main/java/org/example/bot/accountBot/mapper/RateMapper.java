package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface RateMapper extends BaseMapper<Rate> {
//
//    @Update("update rate set rate=#{rate}")
//    void updateRate(String updateAccount);
//    @Update("update rate set exchange=#{exchange}")
//    void updateExchange(BigDecimal updateAccount);
//    @Select("select *FROM rate")
//    Rate selectRate();
//    @Insert("insert into rate(exchange,rate,addTime,overDue,handlestatus,callBackStatus,detailStatus)" +
//            "values (#{exchange},#{rate},#{addTime},#{overDue},#{handlestatus},#{callBackStatus},#{detailStatus})")
//    void insertRate(Rate rates);
//
//    @Update("update rate set overDue=#{overdue}")
//    void updateOverDue(Long overdue);
//
//    @Update("update rate set handlestatus=#{i}")
//    void updateHandleStatus(int i);
//    @Update("update rate set callBackStatus=#{i}")
//    void updateCallBackStatus(int i);
//    @Update("update rate set detailStatus=#{i}")
//    void updateDatilStatus(int i);
}
