package org.example.bot.accountBot.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * 此类封装发送消息的用户信息 回复的信息
 */
@Accessors(chain = true)
@Data
public class UserDTO {

    public int id; //主键id

    public String userId; //主键id update.getMessage().getReplyToMessage().getFrom().id 是纸飞机生成的id

    public String username;  //用户名 liuxiaolon

    public String firstName;    //昵称 刘

    public String lastName;    //昵称 小帅

    public boolean isNormal=true;    //是否是普通用户  默认是
    //-----------以下是回复人的信息-----------
    public String callBackUserId;

    public String callBackName;

    public String callBackFirstName;

    public String callBackLastName;

    public String text;

    public  void  setInfo(Message message){
        this.firstName=message.getFrom().getFirstName()+"";
        this.lastName=message.getFrom().getLastName()+"";
        this.username=message.getFrom().getUserName()+"";
        this.userId=message.getFrom().getId()+"";
        this.text=message.getText();
    }


}