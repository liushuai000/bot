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
 * 用户权限表 记录用户对应群组的管理和操作员信息
 */

@Accessors(chain = true)
@Data
@TableName(value = "user_operation", autoResultMap = true)
public class UserOperation {

    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    private String userId;
    @TableField("username")
    public String username;
    @TableField("admin_user_id")
    public String adminUserId;//上级用户id UserNormal表里的userId  上级管理设置操作员  根据上级用户id获取管理员有效期
    @TableField("is_operation")
    public boolean isOperation;//默认不是操作员需要管理员设置操作员 是操作员(1) 否(0)
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("create_time")
    private Date createTime=new Date();






}
