package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Data
@ApiModel("OperationUserDTO")
public class OperationUserDTO {
    @ApiModelProperty("group_id")
    private String groupId;
    @ApiModelProperty("count")
    private BigDecimal count=BigDecimal.ZERO;//总入款
    @ApiModelProperty("countDowned")
    private BigDecimal countDowned;//总下发
    @ApiModelProperty("countCishu")
    private BigDecimal countCishu;//总入款次数
    @ApiModelProperty("countDownedCishu")
    private BigDecimal countDownedCishu;//总下发次数
    @ApiModelProperty("callBackName")
    private String callBackName;//操作人账户
    @ApiModelProperty("callBackFirstName")
    private String callBackFirstName;//操作人名称
    @ApiModelProperty("down")
    private BigDecimal down;//未下发


}
