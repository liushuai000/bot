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
@ApiModel("RateDTO")
public class RateDTO {

    @ApiModelProperty("group_id")
    private String groupId;//群组id
    @ApiModelProperty("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @ApiModelProperty("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("addTime")
    private Date addTime;//添加的时间
    @ApiModelProperty("matcher")
    private boolean matcher=false;    //是否是公式入账的：true 1 表示是， false 0表示不是
    @ApiModelProperty("calc_u")//如果是+30u 带U就不计算费率 默认false不带U计算
    private boolean calcU=false;// true 是计算 false是不计算 直接增加 +30u 的时候会计算费率这个点，不应该计算费率，只需要根据当前设置的固定汇率计算除金额，跟+30u


}
