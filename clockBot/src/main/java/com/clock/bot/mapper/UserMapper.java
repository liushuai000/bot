package com.clock.bot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clock.bot.pojo.User;
import org.apache.ibatis.annotations.Mapper;


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