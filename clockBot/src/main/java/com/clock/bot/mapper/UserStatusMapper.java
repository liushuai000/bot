package com.clock.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserStatusMapper extends BaseMapper<UserStatus> {
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
