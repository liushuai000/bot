package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.bot.accountBot.pojo.User;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
//    @Select("select username, firstname from user")
//    List<User> selectAll();
//
//    @Insert("insert into user(username,firstname) values(#{username},#{firstname})")
//    void insertUser(User username);
//
//
//    @Delete("delete from user where username=#{deleteName}")
//    void deleteHandle(String deleteName);

}
