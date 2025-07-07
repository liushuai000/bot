package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.GroupInnerUserMapper;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.GroupInnerUser;
import org.example.bot.accountBot.pojo.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;

@Component
@Service
@Slf4j
public class StatusService {
    @Autowired
    StatusMapper mapper;
    @Autowired
    GroupInnerUserMapper groupInnerUserMapper;
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
    public Status getInitStatus(Update update, String groupId, String groupTitle) {
        Status status = selectGroupId(groupId);
        if (status==null) {
            status = new Status();
            //初始化状态信息
            status.setGroupId(groupId);
            status.setHandleStatus(1);
            status.setGroupTitle(groupTitle);
            status.setCallBackStatus(1);
            status.setDetailStatus(1);
            status.setRiqie(true);
            insertStatus(status);
        }
        Message messageTemp = update.getMessage();
        if (update.hasMessage() ) {
            Chat chat = messageTemp.getChat();
            if ("group".equals(chat.getType()) && messageTemp.getMigrateToChatId() != null) {
                String oldGroupId = chat.getId().toString();//getMigrateFromChatId 是否用这个id
                mapper.delete(new QueryWrapper<Status>().eq("group_id", oldGroupId));
                groupInnerUserMapper.delete(new QueryWrapper<GroupInnerUser>().eq("group_id", oldGroupId));
            }
            // 判断是否为群组或超级群
            if ("group".equals(chat.getType()) || "supergroup".equals(chat.getType())) {
                String chatId = chat.getId().toString();
                // 检查是否是群组标题变更事件
                if (messageTemp.getNewChatTitle()!=null) {
                    String newTitle = chat.getTitle(); // 获取新标题
                    mapper.update(null,  new UpdateWrapper<Status>().eq("group_id", chatId).set("group_name", newTitle));
                    GroupInnerUser groupInnerUser = groupInnerUserMapper.selectOne(new QueryWrapper<GroupInnerUser>().eq("group_id", chatId));
                    if (groupInnerUser!=null){
                        groupInnerUserMapper.update(null, new UpdateWrapper<GroupInnerUser>().eq("group_id", chatId).set("group_name", newTitle));
                    }
                }
            }
        }
        return status;
    }

}
