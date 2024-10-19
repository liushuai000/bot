package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.bot.accountBot.pojo.Notification;
import org.example.bot.accountBot.pojo.User;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

}
