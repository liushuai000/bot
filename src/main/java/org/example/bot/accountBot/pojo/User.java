package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息
 */
@ApiModel("user")
@Accessors(chain = true)
@Data
public class User {
    @TableId(type= IdType.AUTO)
    private String id;
    @TableField("user_id")
    private int userId; //主键id
    @TableField("username")
    private String username;  //用户名
    @TableField("first_name")
    private String firstName;    //昵称
}
