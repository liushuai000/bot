package org.example.bot.accountBot.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("IssueDTO")
public class IssueDTO{
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @ApiModelProperty("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //公式费率
    @ApiModelProperty("downExchange")
    private BigDecimal downExchange=BigDecimal.ZERO;    //下发汇率
    @ApiModelProperty("downRate")
    private BigDecimal downRate=BigDecimal.ZERO;    //下发费率
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
    private String callBackName;//操作账户
    @ApiModelProperty("callBackFirstName")
    private String callBackFirstName;//操作人名称  是firstName+lastName
    @TableField("is_matcher")
    private boolean isMatcher=false;//是否公式入账
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("addTime")
    private Date addTime;
    @ApiModelProperty("downed")
    private BigDecimal downed;//金额
    @ApiModelProperty("downedUSDT")
    private BigDecimal downedUSDT=BigDecimal.ZERO;//下发的金额转USDT
    @ApiModelProperty("down")
    private BigDecimal down;//未下发
    @ApiModelProperty("issueHandlerMoney")
    private BigDecimal issueHandlerMoney=BigDecimal.ZERO;//全局下方手续费
    @ApiModelProperty("downing")
    private BigDecimal downing=BigDecimal.ZERO;  //已下发
    @ApiModelProperty("pm")
    private Boolean pm=false;//是否手动添加
    @ApiModelProperty("calcU")
    private Boolean calcU=false;//是否U添加
}
