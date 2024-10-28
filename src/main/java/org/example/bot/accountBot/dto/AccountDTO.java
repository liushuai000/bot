package org.example.bot.accountBot.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("AccountDTO")
public class AccountDTO extends RateDTO{
    @ApiModelProperty(value="id")
    private int id;
    @ApiModelProperty("rate_id")
    private int rateId;//外键id rate的因为是多个账单 一对多 (+1000/7*0.05)单笔的就是一对一汇率
    @ApiModelProperty("group_id")
    private String groupId;//群组id
    @ApiModelProperty("user_id")
    private String userId;//这个是发送消息的那个人的id
    @ApiModelProperty("call_back_user_id")
    private String callBackUserId;//回复人id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("add_time")
    private Date addTime;
    @ApiModelProperty("total")
    private BigDecimal total;    //总入账 这个是每笔要入款的金额
    @ApiModelProperty("downing")
    private BigDecimal downing;    //应下发
    @ApiModelProperty("down")
    private BigDecimal down;    //未下发
    @ApiModelProperty("riqie")
    private boolean riqie=false;   //是否设置了日切  用status里的 set_riqie
    @ApiModelProperty("account_handler_money")
    private BigDecimal accountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account issue 里的手续费是记录单笔历史的



}
