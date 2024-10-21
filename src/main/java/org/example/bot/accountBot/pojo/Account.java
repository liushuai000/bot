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
 * 用户操作记账 账户信息
 */

@ApiModel("accounts")
@Accessors(chain = true)
@Data
@TableName(value = "accounts", schema = "bot", autoResultMap = true)
public class Account {
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("rate_id")
    private int rateId;//外键id rate的因为是多个账单 一对多 (+1000/7*0.05)单笔的就是一对一汇率
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("user_id")
    private String userId;//这个是发送消息的那个人的id
    @TableField("call_back_user_id")
    private String callBackUserId;//回复人id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("add_time")
    private Date addTime;
    @TableField("total")
    private BigDecimal total;    //总入账 这个是每笔要入款的金额
    @TableField("downing")
    private BigDecimal downing;    //应下发
    @TableField("down")
    private BigDecimal down;    //未下发
    @TableField("data_status")
    private int dataStatus; //清理今天数据也需要用状态改过期 时间状态:1表示过期，0表示未过期
    @TableField("set_time")
    private Date setTime;   //账单 设置的过期时间
    @TableField("account_handler_money")
    private BigDecimal accountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account issue 里的手续费是记录单笔历史的

}
