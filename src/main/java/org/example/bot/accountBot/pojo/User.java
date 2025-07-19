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
@TableName(value = "user", autoResultMap = true)
public class User {
    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    public String userId; //主键id
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("history_username")
    public String historyUsername;  //历史用户名 为了解决重复插入user
    @TableField("first_name")
    public String firstName;    //昵称 刘
    @TableField("last_name")
    public String lastName;    //昵称 小帅
    @TableField("create_time")
    private Date createTime;//创建时间
    @TableField("is_super_admin")
    public boolean isSuperAdmin=false; //是否是管理员
    @TableField("valid_free")
    private boolean validFree=false;//是否已经使用过免费时长6小时  只有在获取个人信息的时候赋值
    @TableField("valid_time")
    private Date validTime;//有效期  默认+6个小时有效期 只有在获取个人信息的时候赋值  或管理设置授权的时候
    @TableField("cjgl")
    private boolean cjgl=false;//手动设置的超级管理 true 是超级 false 不是

    public String getFirstLastName(){
        String fname=firstName==null?"":firstName;
        String lname=lastName==null?"":lastName;
        return fname+lname;
    }
}
