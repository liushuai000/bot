package org.example.bot.accountBot.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("RateDTO")
public class RateDTO {
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("group_id")
    private String groupId;//群组id
    @ApiModelProperty("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @ApiModelProperty("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @ApiModelProperty("downing")
    private BigDecimal downing=BigDecimal.ZERO;    //应下发
    @ApiModelProperty("downed")
    private BigDecimal downed=BigDecimal.ZERO;    //已下发
    @ApiModelProperty("down")
    private BigDecimal down=BigDecimal.ZERO;    //未下发
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    @ApiModelProperty("addTime")
//    private Date addTime;//添加的时间
//    @ApiModelProperty("calc_u")//如果是+30u 带U就不计算费率 默认false不带U计算
//    private boolean calcU=false;// true 是计算 false是不计算 直接增加 +30u 的时候会计算费率这个点，不应该计算费率，只需要根据当前设置的固定汇率计算除金额，跟+30u
    @ApiModelProperty("count")
    private BigDecimal count=BigDecimal.ZERO;//总入款
    @ApiModelProperty("CountHandlerMoney")
    private BigDecimal CountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account+issue 里的手续费
    @ApiModelProperty("downing")
    private BigDecimal downingUSDT=BigDecimal.ZERO;    //应下发
    @ApiModelProperty("downed")
    private BigDecimal downedUSDT=BigDecimal.ZERO;    //已下发
    @ApiModelProperty("down")
    private BigDecimal downUSDT=BigDecimal.ZERO;    //未下发
    @ApiModelProperty("count")
    private BigDecimal countUSDT=BigDecimal.ZERO;//总入款
    @ApiModelProperty("downExchange")
    private BigDecimal downExchange=BigDecimal.ZERO;    //下发汇率
    @ApiModelProperty("downRate")
    private BigDecimal downRate=BigDecimal.ZERO;    //下发费率
    @ApiModelProperty("pmoney")
    private BigDecimal pmoney=BigDecimal.ZERO;   //手动添加费用 实体类用的




}
