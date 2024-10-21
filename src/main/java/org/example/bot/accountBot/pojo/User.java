package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户信息
 */
@ApiModel("user")
@Accessors(chain = true)
@Data
@TableName(value = "user", schema = "bot", autoResultMap = true)
public class User {
    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    public String userId; //主键id update.getMessage().getReplyToMessage().getFrom().id 是纸飞机生成的id
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("first_name")
    public String firstName;    //昵称 刘
    @TableField("last_name")
    public String lastName;    //昵称 小帅
    @TableField("is_normal")
    public boolean isNormal=true; //是普通(1) 否(0)
    @TableField("old_username")
    public String oldUsername;  //这个是旧的用户名 liuxiaolon  防止修改用户名情况
    @TableField("old_first_name")
    public String oldFirstName;    //这个是旧的昵称 刘
    @TableField("old_last_name")
    public String oldLastName;    //这个是旧的昵称 小帅
    @TableField("valid_time")
    private Date validTime;//有效期
}
