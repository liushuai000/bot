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
    public String userId; //主键id
    @TableField("superiors_user_id")
    public String superiorsUserId;//上级用户id   上级管理设置操作员  根据上级用户id获取管理员有效期
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("first_name")
    public String firstName;    //昵称 刘
    @TableField("last_name")
    public String lastName;    //昵称 小帅
    @TableField("is_normal")
    public boolean isNormal=true; //是普通(1) 否(0)
    @TableField("valid_free")
    private boolean validFree=false;//是否已经使用过免费时长6小时
    @TableField("valid_time")
    private Date validTime;//有效期  默认+6个小时有效期
    @TableField("create_time")
    private Date createTime;//创建时间

}
