package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.bot.accountBot.pojo.Account;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
//
//    @Select("select handle, handleFirstName,handleStatus,call_back,callBackFirstName,callBackStatus, total, downing, down, addTime, dataStatus,setTime from accounts where dataStatus = 0")
//    List<Account> selectAccount();

//    @Insert("insert into accounts(handle,handleFirstName,handleStatus,call_back,callBackFirstName,callBackStatus,total,downing,down,addTime,dataStatus,setTime)" +
//            "values (#{handle},#{handleFirstName},#{handleStatus},#{call_back},#{callBackFirstName},#{callBackStatus},#{total},#{downing},#{down},#{addTime},#{dataStatus},#{setTime})")
//    void insertAccount(Account updateAccount);
//
//    @Update("update accounts set dataStatus=1")
//    void updateDataStatus();
//    @Update("update accounts set setTime=#{setTime}")
//    void updateSetTime(Date setTime);
//    @Update("delete from accounts where dataStatus =0")
//    void deleteTedayData();
//
//    @Delete("delete from accounts where addTime=#{addTime}")
//    void deleteInData(Date addTime);
//    @Delete("delete from accounts where addTime=#{matchTime}")
//    void deleteMatchTime(Date matchTime);
//
//    @Update("update accounts set down=#{add}")
//    void updateDown(BigDecimal add);
//
//    @Update("update accounts set down=#{down}")
//    void updateNewestData(BigDecimal down);
//    @Select("SELECT handle FROM Accounts WHERE addTime >= DATE_SUB(NOW(), INTERVAL 48 HOUR);")
//    List<String> inform(Date date);
}
