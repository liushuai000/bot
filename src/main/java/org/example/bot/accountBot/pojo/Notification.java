package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

//48小时内发送消息的人
@ApiModel("notification")
@Accessors(chain = true)
@Data
@TableName(value = "notification", schema = "bot", autoResultMap = true)
public class Notification {
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("user_id")
    private String userId;//这个是发送消息的那个人的id
    @TableField("add_time")
    private Date addTime;//发送消息的时间
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("first_name")
    public String firstName;    //昵称 刘
    @TableField("last_name")
    public String lastName;    //昵称 小帅


}
