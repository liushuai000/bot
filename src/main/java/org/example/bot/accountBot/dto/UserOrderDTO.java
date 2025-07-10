package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("UserOrderDTO")
public class UserOrderDTO {
    @ApiModelProperty(value = "用户ID")
    private String userId;
    @ApiModelProperty(value = "用户名")
    private String username;
    @ApiModelProperty(value = "昵称")
    private String nickname;
    @ApiModelProperty(value = "订单金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "订单类型（已购买/未购买）")
    private String type;
    @ApiModelProperty(value = "按钮名称")
    private String configEditButtonName;
    @ApiModelProperty(value = "续费天数")
    private String month;
    @ApiModelProperty(value = "订单号")
    private String orderNumber;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "订单结束时间")
    private Date endTime;



}
