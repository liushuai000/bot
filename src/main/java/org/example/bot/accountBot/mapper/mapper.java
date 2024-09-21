package org.example.bot.accountBot.mapper;

import org.apache.ibatis.annotations.*;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface mapper {
    @Select("select username, firstname from user")
    List<User> selectAll();
    @Select("select handle, handleFirstName,handlestatus,call_back,callBackFirstName,callBackStatus, total, downing, down, addTime, dataStatus,setTime from accounts where dataStatus = 0")
    List<Account> selectAccount();
    @Insert("insert into user(username,firstname) values(#{username},#{firstname})")
    void insertUser(User username);


    @Insert("insert into accounts(handle,handleFirstName,handlestatus,call_back,callBackFirstName,callBackStatus,total,downing,down,addTime,dataStatus,setTime)" +
            "values (#{handle},#{handleFirstName},#{handlestatus},#{call_back},#{callBackFirstName},#{callBackStatus},#{total},#{downing},#{down},#{addTime},#{dataStatus},#{setTime})")
    void insertAccount(Account updateAccount);
    @Update("update rate set rate=#{rate}")
    void updateRate(String updateAccount);
    @Update("update rate set exchange=#{exchange}")
    void updateExchange(BigDecimal updateAccount);
    @Select("select *FROM rate")
    Rate selectRate();
    @Insert("insert into rate(exchange,rate,addTime,overDue,handlestatus,callBackStatus,detailStatus)" +
            "values (#{exchange},#{rate},#{addTime},#{overDue},#{handlestatus},#{callBackStatus},#{detailStatus})")
    void insertRate(Rate rates);
    @Delete("delete from user where username=#{deleteName}")
    void deleteHandle(String deleteName);
    @Update("update accounts set dataStatus=1")
    void updateDataStatus();
    @Update("update accounts set setTime=#{setTime}")
    void updateSetTime(Date setTime);
    @Update("delete from accounts where dataStatus =0")
    void deleteTedayData();
    @Update("update rate set overDue=#{overdue}")
    void updateOverDue(Long overdue);
    @Delete("delete from accounts where addTime=#{addTime}")
    void deleteInData(Date addTime);
    @Delete("delete from accounts where addTime=#{matchTime}")
    void deleteMatchTime(Date matchTime);
    @Select("select handle,handleFirstName,addTime, call_back, downed, down, dataStatus,setTime from issue where dataStatus=0")
    List<Issue> selectIssue();
    @Update("update issue set dataStatus=1")
    void updateIssueDataStatus();
    @Update("update issue set setTime=#{setTime}")
    void updateIssueSetTime(Date setTime);

    @Insert("INSERT INTO issue(handle, handleFirstName, call_back, callBackFirstName, downed, down, addTime, dataStatus, setTime) " +
            "VALUES (#{handle}, #{handleFirstName}, #{call_back}, #{callBackFirstName}, #{downed}, #{down}, #{addTime}, #{dataStatus}, #{setTime})")
    void insertIssue(Issue issue);
    @Update("update accounts set down=#{add}")
    void updateDown(BigDecimal add);
    @Update("update issue set down=#{add}")
    void uodateIssueDown(BigDecimal add);
    @Delete("delete from issue where dataStatus=0")
    void deleteTedayIusseData();
    @Update("update issue set down=#{down}")
    void updateissueDown(BigDecimal down);
    @Delete("delete from issue where addTime=#{addTime}")
    void deleteNewestIssue(Date addTime);
    @Update("update accounts set down=#{down}")
    void updateNewestData(BigDecimal down);
    @Select("SELECT handle FROM Accounts WHERE addTime >= DATE_SUB(NOW(), INTERVAL 48 HOUR);")
    List<String> inform(Date date);
    @Update("update rate set handlestatus=#{i}")
    void updateHandleStatus(int i);
    @Update("update rate set callBackStatus=#{i}")
    void updateCallBackStatus(int i);
    @Update("update rate set detailStatus=#{i}")
    void updateDatilStatus(int i);
}
