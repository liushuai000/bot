package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import javax.annotation.Resource;
import java.time.LocalDate;
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
    AccountBot  accountBot;
    @Autowired
    UserService userService;
    @Value("${adminUserId}")
    protected String adminUserId;

    //设置机器人在群组内的有效时间 默认免费使用日期6小时. 机器人底部按钮 获取个人信息 获取最新用户名 获取个人id 使用日期;
    protected void handleNonGroupMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        if (!userDTO.getUserId().equals(adminUserId)){
            accountBot.sendMessage(sendMessage,"您不是超级管理!无权限设置管理员!");
            return;
        }
        //授权-123456789-30  用户id -30
        PaperPlaneBotButton buttonList = new PaperPlaneBotButton();
        ReplyKeyboardMarkup replyKeyboardMarkup = buttonList.sendReplyKeyboard();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);//是否在onUpdateReceived设置
        String text = userDTO.getText();
        String regex = "^授权-[a-zA-Z0-9]+-[a-zA-Z0-9]+$";
        String regex1 = "^授权-[a-zA-Z0-9]+$";
        String[] split3 = text.split("-");
        if (text.equals("获取个人信息")){
            this.getUserInfoMessage(message,sendMessage,userDTO);
            return;
        }
        if (text.contains("授权")){
            boolean matches = text.matches(regex);
            boolean matches1 = text.matches(regex1);
            String validTimeText = "";
            String userId=split3[1];
            User user=userService.findByUserId(userId);
            if (matches) {
                validTimeText=split3[2];
            }else if (matches1) {
                if (user.isValidFree()){
                    accountBot.sendMessage(sendMessage,"您已经使用过6小时免费时长!");
                    return;
                }
            }else {
                accountBot.sendMessage(sendMessage,"格式不匹配!");
                return;
            }

            LocalDateTime tomorrow;
            if (StringUtils.isBlank(validTimeText)){
                tomorrow= LocalDateTime.now().plusHours(6);//默认6小时体验
            }else {
                tomorrow = LocalDateTime.now().plusDays(Long.parseLong(validTimeText));
            }
            Date validTime = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());

            if (user == null) {
                user = new User();
                user.setUserId(userId);
                user.setUsername(userDTO.getUsername());
                user.setFirstName(userDTO.getFirstName());
                user.setLastName(userDTO.getLastName());
                user.setNormal(false);//默认操作权限管理员
                user.setOperation(true);
                user.setCreateTime(new Date());
                user.setValidTime(validTime);
                user.setValidFree(true);//是否使用过免费6小时
                userService.insertUser(user);
            }else {
//                    user.setUsername(userDTO.getUsername());
//                    user.setFirstName(userDTO.getFirstName());
//                    user.setLastName(userDTO.getLastName());
                user.setNormal(false);//默认操作权限管理员
                user.setOperation(true);
                user.setValidTime(validTime);
                user.setSuperiorsUserId(userId);
                user.setValidFree(true);
                userService.updateUserValidTime(user,validTime);
            }
            accountBot.sendMessage(sendMessage,"用户ID: "+userId+" 有效期:"+tomorrow.getYear()+"年"+tomorrow.getMonthValue()+ "月"+
                    tomorrow.getDayOfMonth()+"日"+ tomorrow.getHour()+"时"+tomorrow.getMinute()+"分" +tomorrow.getSecond()+"秒");
            return;
        }
        if (text.equals("/start")) accountBot.sendMessage(sendMessage,"欢迎使用!");
    }
    //获取用户信息
    private void getUserInfoMessage(Message message, SendMessage sendMessage, UserDTO userDTO) {
        User byUserId = userService.findByUserId(userDTO.getUserId());
        String  firstName=byUserId.getFirstName()==null?"":byUserId.getFirstName();
        String  lastName=byUserId.getLastName()==null?"":byUserId.getLastName();
        String time = "";
        if(byUserId.getValidTime()!=null){
            LocalDateTime t= LocalDateTime.ofInstant(byUserId.getValidTime().toInstant(), ZoneId.systemDefault());
            time=" 有效期:"+t.getYear()+"年"+t.getMonthValue()+ "月"+
                    t.getDayOfMonth()+"日"+ t.getHour()+"时"+t.getMinute()+"分" +t.getSecond()+"秒";
        }
        String replayMessage="用户名:"+byUserId.getUsername()+
                " 用户ID:"+byUserId.getUserId()+
                " 用户昵称:"+firstName+lastName+"\n"+ time;

        accountBot.sendMessage(sendMessage,replayMessage);
    }


}
