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

@ApiModel("UserAuthority")
@Accessors(chain = true)
@Data
@TableName(value = "user_authority", schema = "bot", autoResultMap = true)
public class UserAuthority {

    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    private String userId;
    @TableField("username")
    public String username;
    @TableField("is_operation")
    public boolean isOperation;//默认不是操作员需要管理员设置操作员 是操作员(1) 否(0)  超级管理  管理(isNormal) 操作员(isOperation)  群里普通用户
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("create_time")
    private Date createTime=new Date();




}
