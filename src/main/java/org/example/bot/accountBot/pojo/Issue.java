package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 操作人记录
 */
@ApiModel("issue")
@Accessors(chain = true)
@Data
@TableName(value = "issue", schema = "bot", autoResultMap = true)
public class Issue {
    //应该加userid ???然后时间倒序查
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("rate_id")
    private int rateId;//外键id rate的因为是多个账单 一对多 (+1000/7*0.05)单笔的就是一对一汇率
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("handle")
    private String handle;//操作人
    @TableField("user_id")
    private String userId;//操作人id
    @TableField("handle_first_name")
    private String handleFirstName;    //操作人昵称
    @TableField("handle_last_name")
    private String handleLastName;    //操作人昵称
    @TableField("call_back")
    private String callBack;    //回复人
    @TableField("call_back_first_name")
    private String callBackFirstName;    //回复人昵称
    @TableField("call_back_last_name")
    private String callBackLastName;    //回复人昵称
    @TableField("user_message_text")
    private String userMessageText;    //用户发的文本消息 记录操作
    @TableField("downed")
    private BigDecimal downed;    //已下发 已出帐
    @TableField("down")
    private BigDecimal down;    //未下发
    @TableField("add_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @TableField("data_status")
    private int dataStatus;    //时间状态:1表示过期，0表示未过期
    @TableField("set_time")
    private Date setTime;    //设置的过期时间
}
