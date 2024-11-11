package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.bot.accountBot.pojo.UserNormal;

@Mapper
public interface UserNormalMapper extends BaseMapper<UserNormal> {
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
