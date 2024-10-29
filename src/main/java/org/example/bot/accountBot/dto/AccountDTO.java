package org.example.bot.accountBot.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("AccountDTO")
public class AccountDTO extends RateDTO{
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("rateId")
    private int rateId;//外键id rate的因为是多个账单 一对多 (+1000/7*0.05)单笔的就是一对一汇率
    @ApiModelProperty("groupId")
    private String groupId;//群组id
    @ApiModelProperty("userId")
    private String userId;//这个是发送消息的那个人的id
    @ApiModelProperty("callBackUserId")
    private String callBackUserId;//回复人id
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    @ApiModelProperty("addTime")
    private Date addTime;
    @ApiModelProperty("total")
    private BigDecimal total;    //总入账 这个是每笔要入款的金额
    @ApiModelProperty("downing")
    private BigDecimal downing;    //应下发
    @ApiModelProperty("down")
    private BigDecimal down;    //未下发
    @ApiModelProperty("riqie")
    private boolean riqie=false;   //是否设置了日切  用status里的 set_riqie
    @ApiModelProperty("accountHandlerMoney")
    private BigDecimal accountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account issue 里的手续费是记录单笔历史的



}
