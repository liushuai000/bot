package com.clock.bot.botConfig;

import com.clock.bot.dto.UserDTO;
import com.clock.bot.pojo.User;
import com.clock.bot.pojo.UserNormal;
import com.clock.bot.service.UserNormalService;
import com.clock.bot.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 此类设置单人机器人聊天消息
 */
@Slf4j
@Service
public class PaperPlaneBotSinglePerson {
    @Resource
    ClockBot  clockBot;
    @Autowired
    UserService userService;
    @Value("${adminUserId}")
    protected String adminUserId;
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    UserNormalService userNormalService;
    //设置机器人在群组内的有效时间 默认免费使用日期6小时. 机器人底部按钮 获取个人信息 获取最新用户名 获取个人id 使用日期;
    protected void handleNonGroupMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        String text = userDTO.getText();
        //授权-123456789-30  用户id -30
        PaperPlaneBotButton buttonList = new PaperPlaneBotButton();
        ReplyKeyboardMarkup replyKeyboardMarkup = buttonList.sendSelfReplyKeyboard();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);//是否在onUpdateReceived设置
        String regex = "^授权-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
        String[] split3 = text.split("-");
        if (text.equals("获取个人信息")){
            this.getUserInfoMessage(message,sendMessage,userDTO);
            return;
        }
        if (text.contains("授权")){
            if (text.startsWith("授权-")&&!userDTO.getUserId().equals(adminUserId)){
                clockBot.sendMessage(sendMessage,"您不是超级管理!无权限设置管理员!");
                return;
            }
            boolean matches = text.matches(regex);
            String validTimeText = "";
            String userId=split3[1];
            User user=userService.findByUserId(userId);
            if (matches) {
                validTimeText=split3[2];
            }else {
                clockBot.sendMessage(sendMessage,"格式不匹配!");
                return;
            }
            LocalDateTime tomorrow= LocalDateTime.now().plusDays(Long.parseLong(validTimeText));
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            if (user == null) {
                user = new User();
                user.setUserId(userId);
                user.setUsername(userDTO.getUsername());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setCreateTime(new Date());
                user.setSuperAdmin(true);//是管理员
                user.setValidFree(true);
                user.setValidTime(validTime);
                userService.insertUser(user);
                UserNormal userNormal = new UserNormal();
                userNormal.setAdmin(true);
                userNormal.setGroupId(message.getChatId().toString());
                userNormal.setUserId(userDTO.getUserId());
                userNormal.setCreateTime(new Date());
                userNormal.setUsername(userDTO.getUsername());
                userNormalService.insertUserNormal(userNormal);
            }else {
                user.setSuperAdmin(true);//默认操作权限管理员
                user.setValidTime(validTime);
                user.setValidFree(true);
                userService.updateUserValidTime(user,validTime);
                UserNormal userNormal = new UserNormal();
                userNormal.setAdmin(true);
                userNormal.setGroupId(message.getChatId().toString());
                userNormal.setUserId(userDTO.getUserId());
                userNormal.setCreateTime(new Date());
                userNormal.setUsername(userDTO.getUsername());
                userNormalService.insertUserNormal(userNormal);
            }
            clockBot.sendMessage(sendMessage,"用户ID: "+userId+" 有效期:"+tomorrow.getYear()+"年"+tomorrow.getMonthValue()+ "月"+
                    tomorrow.getDayOfMonth()+"日"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
            return;
        }
        if (text.equals("/start")) clockBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),"<b>你好！欢迎使用本机器人：\n" +
                "\n" +
                "点击下方底部按钮：获取个人信息\n" +
                "（将我拉入群组可免费使用8小时）\n" +
                "➖➖➖➖➖➖➖➖➖➖➖\n" +
                "<b>本机器人用户名 ：</b> <code>@"+username+"</code>\n" +//（点击复制）
                "\n" +
                "<b>联系客服：</b>@vipkefu\n" +
                "<b>双向客服：</b>@yewuvipBot");
    }

    //获取用户信息
    private void getUserInfoMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        User user = userService.findByUserId(userDTO.getUserId());
        if (user==null){
            user = new User();
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(8);
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            user.setUserId(userDTO.getUserId());
            user.setUsername(userDTO.getUsername());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setCreateTime(new Date());
            user.setSuperAdmin(true);
            user.setValidTime(validTime);
            user.setValidFree(true);//已经体验过会员
            userService.insertUser(user);

        }else if (!user.isValidFree()) {//还没有体验过免费8小时
            LocalDateTime tomorrow = LocalDateTime.now().plusHours(8);
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
            user.setValidTime(validTime);
            user.setSuperAdmin(true);//默认操作权限管理员
            user.setValidTime(validTime);
            user.setValidFree(true);//是使用过免费6小时
            userService.updateUser(user);
        }
        LocalDateTime t= LocalDateTime.ofInstant(user.getValidTime().toInstant(), ZoneId.systemDefault());
        String   time=" 有效期:"+t.getYear()+"年"+t.getMonthValue()+ "月"+
                t.getDayOfMonth()+"日"+ t.getHour()+"时"+t.getMinute()+"分" +t.getSecond()+"秒";
        String firstName=userDTO.getFirstName()==null?"": userDTO.getFirstName();
        String lastName=userDTO.getLastName()==null?"":userDTO.getLastName();
        String message1="<b>账号个人信息</b>✅：\n" +
                "\n" +
                "<b>用户名：</b>@"+userDTO.getUsername()+" \n" +
                "<b>用户ID：</b><code>"+userDTO.getUserId()+"</code>\n" +
                "<b>用户昵称：</b>"+firstName+lastName+"\n" +
                "<b>有效期：</b>"+time;
        clockBot.tronAccountMessageTextHtml(sendMessage,userDTO.getUserId(),message1);
    }
}
