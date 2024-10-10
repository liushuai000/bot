package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息
 */
@ApiModel("user")
@Accessors(chain = true)
@Data
@TableName(value = "user", schema = "bot", autoResultMap = true)
public class User {
    @TableId(type= IdType.AUTO,value = "user_id")
    public int userId; //主键id
    @TableField("username")
    public String username;  //用户名
    @TableField("first_name")
    public String firstName;    //昵称
}
