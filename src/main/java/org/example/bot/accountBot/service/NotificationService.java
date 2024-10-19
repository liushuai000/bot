package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.botConfig.AccountBot;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.NotificationMapper;
import org.example.bot.accountBot.mapper.UserMapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Notification;
import org.example.bot.accountBot.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Service
@Slf4j
public class NotificationService {
    @Autowired
    NotificationMapper mapper;
    @Resource
    AccountBot accountBot;

    //通知功能实现/48 小时内在群组发言过的所有人
    public void inform(String text, SendMessage sendMessage) {
        if (text.equals("通知")){
            // 计算 48 小时之前的时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -48);
            Date dateThreshold = calendar.getTime();
            QueryWrapper<Notification> queryWrapper = new QueryWrapper<>();
            queryWrapper.gt("add_time",dateThreshold);
            queryWrapper.lt("add_time",new Date());
            List<Notification> notifications = mapper.selectList(queryWrapper);
            StringBuilder sb = new StringBuilder();
            sb.append("48 小时内在群组发言过的所有人: ");
            for (int i = 0; i < notifications.size(); i++) {
                String firstName=notifications.get(i).getFirstName()==null?"":notifications.get(i).getFirstName();
                String lastName=notifications.get(i).getLastName()==null?"":notifications.get(i).getLastName();
                sb.append(String.format("<a href=\"tg://user?id=%d\">%s</a>",
                        Long.parseLong(notifications.get(i).getUserId()),firstName +lastName)+"  ");
            }
            accountBot.sendMessage(sendMessage, sb.toString());
        }
    }
    //
    public void initNotification(UserDTO userDTO) {
        //需要加群组id
        Notification notification1 = mapper.selectById(userDTO.getUserId());
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("username", userDTO.getUsername());
        Notification notification2 = mapper.selectOne(wrapper);
        if (notification1!=null){
            notification1.setAddTime(new Date());
            UpdateWrapper<Notification> updateWrapper = new UpdateWrapper<Notification>()
                    .eq("user_id", userDTO.getUserId())
                    .set("add_time", notification1.getAddTime());
            mapper.update(notification1,updateWrapper);
        }else if (notification2 != null){
            notification2.setAddTime(new Date());
            UpdateWrapper<Notification> updateWrapper = new UpdateWrapper<Notification>()
                    .eq("username", userDTO.getUsername())
                    .set("add_time", notification2.getAddTime());
            mapper.update(notification2,updateWrapper);
        }else {
            Notification notification = new Notification();
            notification.setUserId(userDTO.getUserId());
            notification.setUsername(userDTO.getUsername());
            notification.setFirstName(userDTO.getFirstName());
            notification.setLastName(userDTO.getLastName());
            notification.setAddTime(new Date());
            mapper.insert(notification);
        }

    }
}
