package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.botConfig.AccountBot;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.NotificationMapper;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.Notification;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.pojo.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Service
@Slf4j
public class StatusService {
    @Autowired
    StatusMapper mapper;

    public Status selectGroupId(String groupId) {
        QueryWrapper<Status> wrapper = new QueryWrapper();
        wrapper.eq("group_id", groupId);
        return mapper.selectOne(wrapper);
    }
    public void insertStatus(Status status) {
        mapper.insert(status);
    }

    public void update(Status status){
        mapper.updateById(status);
    }

    public void updateStatus(String nameStatus,int detailStatus,String groupId) {
        UpdateWrapper<Status> wrapper = new UpdateWrapper();
        wrapper.eq("group_id", groupId);
        wrapper.set(nameStatus, detailStatus);
        mapper.update(null, wrapper);
    }
    public void updateMoneyStatus(String nameStatus, BigDecimal detailStatus, String groupId) {
        UpdateWrapper<Status> wrapper = new UpdateWrapper();
        wrapper.eq("group_id", groupId);
        wrapper.set(nameStatus, detailStatus);
        mapper.update(null, wrapper);
    }
    public Status getInitStatus(String groupId) {
        Status status = selectGroupId(groupId);
        if (status==null) {
            status = new Status();
            //初始化状态信息
            status.setGroupId(groupId);
            status.setGroupTitle(groupId);
            status.setHandleStatus(1);
            status.setCallBackStatus(1);
            status.setDetailStatus(1);
            status.setRiqie(false);
            insertStatus(status);
        }
        return status;
    }

}
