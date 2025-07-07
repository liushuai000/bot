package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Accessors(chain = true)
@Data
@ApiModel(value = "ReturnGroupDTO", description = "返回给前端的数据")
public class ReturnGroupDTO {
    @ApiModelProperty(value = "群组ID")
    private String groupId;
    @ApiModelProperty(value = "群组名称")
    private String groupName;
    @ApiModelProperty(value = "邀请人ID")
    private String inviterId;
    @ApiModelProperty(value = "邀请人名称")
    private String inviterName;
    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRate;
    @ApiModelProperty(value = "费率")
    private BigDecimal feeRate;
    @ApiModelProperty(value = "操作人")
    private String operator;
    @ApiModelProperty(value = "日切时间")
    private String dailyCutTime;
    @ApiModelProperty(value = "显示账单数")
    private Integer billCount;
    @ApiModelProperty(value = "是否开启记账")
    private Boolean isAccountingEnabled;
    @ApiModelProperty(value = "是否消息置顶")
    private Boolean isPinned;
    @ApiModelProperty(value = "拉入时间")
    private Date joinTime;
    @ApiModelProperty
    private List<String> tags;


}
