package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.mapper.UserMapper;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.pojo.UserNormal;
import org.example.bot.accountBot.pojo.UserOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 群内下发地址
 */
@Slf4j
@Service
public class DownAddress {
    @Autowired
    StatusMapper statusMapper;
    @Autowired
    AccountBot accountBot;
    @Autowired
    UserMapper userMapper;
    /**
     * 只有权限人跟操作员可以设置
     * 本群其他人发送地址的时候 机器人需要提醒
     * @param userDTO
     * @param status   userNormalTempAdmin (权限人)   userOperation(操作员)
     */
    public void downAddress(SendMessage sendMessage, UserDTO userDTO, Status status, UserNormal userNormalTempAdmin, UserOperation userOperation) {
        String text = userDTO.getText();
        String currentUserId = userDTO.getUserId();
        if (text.startsWith("设置下发地址") || text.startsWith("修改下发地址")){
            // 权限校验：只有权限人或操作员可以操作
            if (!currentUserId.equals(userNormalTempAdmin.getUserId()) &&
                    !currentUserId.equals(userOperation.getUserId()) && !userOperation.isOperation()) {
                accountBot.sendMessage(sendMessage, "⚠️ 你没有权限设置下发地址！");
                return;
            }
            String address =text.substring(6, text.length());
            if (address.length()!=34){
                accountBot.sendMessage(sendMessage,"地址格式错误!不是34位");
                return;
            }
            status.setDAddress(address);
            status.setDTime(new Date());
            status.setDUserId(userDTO.getUserId());
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage,"✅设置下发地址成功\n\n当前下发地址: <code>"+status.getDAddress()+"</code>");
            return;
            //TTyYfnPDvmVbLDZoPDLnVr8gkC88888888 34位
        }
        if (text.startsWith("查看下发地址")){
            if (status.getDAddress()==null){
                accountBot.sendMessage(sendMessage,"本群没有设置下发地址!");
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", status.getDUserId()));
            String firstLastName = user.getFirstLastName();
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(user.getUserId()), user.getUsername()==null?firstLastName:user.getUsername());
            String a="本群唯一下发地址:\n\n<code>"+status.getDAddress()+"</code>\n\n设置时间:"
                    +sdf.format(status.getDTime())+"\n\n设置人:"+format+"\n\n点击复制";
            accountBot.sendMessage(sendMessage,a);
        }
        if (text.startsWith("set the delivery address")){
            // 权限校验：只有权限人或操作员可以操作
            if (!currentUserId.equals(userNormalTempAdmin.getUserId()) &&
                    !currentUserId.equals(userOperation.getUserId()) && !userOperation.isOperation()) {
                accountBot.sendMessage(sendMessage, "⚠️ 你没有权限设置下发地址！");
                return;
            }
            String address =text.substring("set the delivery address".length(), text.length());
            if (address.length()!=34){
                accountBot.sendMessage(sendMessage,"地址格式错误!不是34位");
                return;
            }
            status.setDAddress(address);
            status.setDTime(new Date());
            status.setDUserId(userDTO.getUserId());
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage,"✅设置下发地址成功\n\n当前下发地址: "+status.getDAddress());
            return;
            //TTyYfnPDvmVbLDZoPDLnVr8gkC88888888 34位
        }
        int length = text.length();
        String substring = "";
        if (length>27 ){
            substring = text.substring(0, 27);
        } else if (length>24) {
            substring = text.substring(0, 24);
        }
        substring=substring.toLowerCase();
        if (substring.startsWith("modify the delivery address")){
            // 权限校验：只有权限人或操作员可以操作
            if (!currentUserId.equals(userNormalTempAdmin.getUserId()) &&
                    !currentUserId.equals(userOperation.getUserId()) && !userOperation.isOperation()) {
                accountBot.sendMessage(sendMessage, "⚠️ 你没有权限设置下发地址！");
                return;
            }
            String address =text.substring("modify the delivery address".length(), text.length());
            if (address.length()!=34){
                accountBot.sendMessage(sendMessage,"地址格式错误!不是34位");
                return;
            }
            status.setDAddress(address);
            status.setDTime(new Date());
            status.setDUserId(userDTO.getUserId());
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage,"✅设置下发地址成功\n\n当前下发地址: "+status.getDAddress());
            return;
        }
        if (substring.startsWith("view the sending address")){
            if (status.getDAddress()==null){
                accountBot.sendMessage(sendMessage,"本群没有设置下发地址!");
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", status.getDUserId()));
            String firstLastName = user.getFirstLastName();
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(user.getUserId()), user.getUsername()==null?firstLastName:user.getUsername());
            String a="本群唯一下发地址:\n\n<code>"+status.getDAddress()+"</code>\n\n设置时间:"
                    +sdf.format(status.getDTime())+"\n\n设置人:"+format+"\n\n点击复制";
            accountBot.sendMessage(sendMessage,a);
        }
    }
}
