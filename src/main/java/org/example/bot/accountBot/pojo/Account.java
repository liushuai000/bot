package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Account {
    @TableId(type= IdType.AUTO)
    private String id;
    //操作人
    private String handle;
    //操作人昵称
    private String handleFirstName;
    //操作人显示状态:1表示不显示，0表示显示
    private int handlestatus;
    //回复人
    private String call_back;
    //回复人昵称
    private String callBackFirstName;
    //回复人显示状态:1表示不显示，0表示显示
    private int callBackStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    //总入账
    private BigDecimal total;
    //应下发
    private BigDecimal downing;
    //未下发
    private BigDecimal down;
    //时间状态:1表示过期，0表示未过期
    private int dataStatus;
    //设置的过期时间
    private Date setTime;


}
