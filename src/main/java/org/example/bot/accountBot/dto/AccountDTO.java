package org.example.bot.accountBot.dto;


import com.baomidou.mybatisplus.annotation.TableField;
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
public class AccountDTO{
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @ApiModelProperty("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @ApiModelProperty("groupId")
    private String groupId;//群组id
    @ApiModelProperty("userId")
    private String userId;
    @ApiModelProperty("username")
    private String username;//操作账户
    @ApiModelProperty("firstName")
    private String firstName;//操作人名称  是firstName+lastName
    @ApiModelProperty("callBackUserId")
    private String callBackUserId;
    @ApiModelProperty("callBackName")
    private String callBackName;//账户
    @ApiModelProperty("callBackFirstName")
    private String callBackFirstName;//名称  是firstName+lastName
    @TableField("is_matcher")
    private boolean isMatcher=false;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("addTime")
    private Date addTime;
    @ApiModelProperty("total")
    private BigDecimal total;//金额
    @ApiModelProperty("totalUSDT")
    private BigDecimal totalUSDT;//金额
    @ApiModelProperty("accountHandlerMoney")
    private BigDecimal accountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account issue 里的手续费是记录单笔历史的
    @ApiModelProperty("downing")
    private BigDecimal downing=BigDecimal.ZERO;  //应下发
    @ApiModelProperty("pm")
    private Boolean pm=false;//是否手动添加
    @ApiModelProperty("calcU")
    private Boolean calcU=false;//是否U添加

}
