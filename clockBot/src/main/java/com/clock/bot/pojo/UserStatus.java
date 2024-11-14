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
@TableName(value = "user_status", schema = "clockbot", autoResultMap = true)
public class UserStatus {
    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    public String userId; //主键id
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("group_id")
    private String groupId;
    @TableField("group_title")
    private String groupTitle;
    @TableField("user_operation_id")
    private String userOperationId;//这个表示最近一次执行的活动当前执行的活动
    @TableField("status")
    private boolean status;   //true上班 false下班
    @TableField("return_home")
    private boolean returnHome=false;//只有在执行活动的时候 回座才是true
    @TableField("work_time")
    private Date workTime;//上班时间
    @TableField("work_down_time")
    private Date workDownTime;//下班时间
    @TableField("create_time")
    private Date createTime;




}
