package com.clock.bot.pojo;

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

@Accessors(chain = true)
@Data
@TableName(value = "user", schema = "clockbot", autoResultMap = true)
public class User {
    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    public String userId; //主键id
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("first_name")
    public String firstName;    //昵称 刘
    @TableField("last_name")
    public String lastName;    //昵称 小帅
    @TableField("create_time")
    private Date createTime;//创建时间


    public String getFirstLastName(){
        String fname=firstName==null?"":firstName;
        String lname=lastName==null?"":lastName;
        return fname+lname;
    }
}
