package com.clock.bot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 记录用户操作
 */

@Accessors(chain = true)
@Data
@TableName(value = "user_operation", schema = "clockbot", autoResultMap = true)
public class UserOperation {
//    private String  status;   //上班 下班 吃饭 上厕所 抽烟 其他 回座
    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("operation")
    private String operation;//0无活动: 1:吃饭 2:上厕所 3:抽烟 4:其他
    @TableField("user_status_id")
    private String userStatusId;//UserStatus表里的 数据
    @TableField("start_time")
    private Date startTime;//操作开始时间
    @TableField("end_time")
    private Date endTime;//操作结束时间   回座的时候更新
    @TableField("create_time")
    private Date createTime;//创建时间






}
