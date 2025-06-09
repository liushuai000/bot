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
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("rate_id")
    private int rateId;//外键id rate的因为是多个账单 一对多 (+1000/7*0.05)单笔的就是一对一汇率
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("user_id")
    private String userId;//操作人id
    @TableField("call_back_user_id")
    private String callBackUserId;//回复人id
    @TableField("downed")
    private BigDecimal downed;    //已出帐
    @TableField("down")
    private BigDecimal down;    //未下发
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @TableField("set_time")
//    private Date setTime;//日切时间
    @TableField("add_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @TableField("riqie")
    private boolean riqie=false;   //是否设置了日切  用status里的 set_riqie 查询status里的日切时间
    @TableField("issue_handler_money")
    private BigDecimal issueHandlerMoney=BigDecimal.ZERO;//全局下方手续费

    @TableField("message_id")
    private Integer messageId;//用户发送消息的id用于取消用

    @TableField("down_exchange")
    private BigDecimal downExchange=BigDecimal.ZERO;    //下发汇率取status里的下发汇率
    @TableField("down_rate")
    private BigDecimal downRate=BigDecimal.ZERO;    //下发费率取status
    @TableField("pm")
    private Boolean pm=false;//是否手动添加

}
