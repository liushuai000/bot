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
public interface IssueMapper extends BaseMapper<Issue> {


//    @Select("select handle,handleFirstName,addTime, call_back, downed, down, dataStatus,setTime from issue where dataStatus=0")
//    List<Issue> selectIssue();
//    @Update("update issue set dataStatus=1")
//    void updateIssueDataStatus();
//    @Update("update issue set setTime=#{setTime}")
//    void updateIssueSetTime(Date setTime);
//
//    @Insert("INSERT INTO issue(handle, handleFirstName, call_back, callBackFirstName, downed, down, addTime, dataStatus, setTime) " +
//            "VALUES (#{handle}, #{handleFirstName}, #{call_back}, #{callBackFirstName}, #{downed}, #{down}, #{addTime}, #{dataStatus}, #{setTime})")
//    void insertIssue(Issue issue);
//
//    @Update("update issue set down=#{add}")
//    void uodateIssueDown(BigDecimal add);
//    @Delete("delete from issue where dataStatus=0")
//    void deleteTodayIusseData();
//    @Update("update issue set down=#{down}")
//    void updateissueDown(BigDecimal down);
//    @Delete("delete from issue where addTime=#{addTime}")
//    void deleteNewestIssue(Date addTime);


}
