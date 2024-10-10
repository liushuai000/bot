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
    @TableId(type= IdType.AUTO,value = "account_id")
    private int accountId;
    @TableField("handle")
    private String handle;//操作人

    @TableField("handle_first_name")
    private String handleFirstName;  //操作人昵称e

    @TableField("handle_status")
    private int handleStatus; //操作人显示状态:1表示不显示，0表示显示
    @TableField("call_back")
    private String callBack;    //回复人
    @TableField("call_back_first_name")
    private String callBackFirstName;    //回复人昵称
    @TableField("call_back_status")
    private int callBackStatus; //回复人显示状态:1表示不显示，0表示显示
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("add_time")
    private Date addTime;
    @TableField("total")
    private BigDecimal total;    //总入账
    @TableField("downing")
    private BigDecimal downing;    //应下发
    @TableField("down")
    private BigDecimal down;    //未下发
    @TableField("data_status")
    private int dataStatus; //时间状态:1表示过期，0表示未过期
    @TableField("set_time")
    private Date setTime;   //设置的过期时间


}
