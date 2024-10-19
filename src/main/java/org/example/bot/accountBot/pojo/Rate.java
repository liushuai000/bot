package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Indexed;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 操作汇率||费率||账单过期时间显示
 */
@ApiModel("rate")
@Accessors(chain = true)
@Data
@TableName(value = "rate", schema = "bot", autoResultMap = true)
public class Rate {
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @TableField("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("add_time")
    private Date addTime;//添加的时间
    @TableField("over_due")
    private Date overDue;    //逾期  超时
    @TableField("handle_status")
    private int handleStatus;    //操作人的显示状态，1表示不显示，0表示显示
    @TableField("call_back_status")
    private int callBackStatus;    //回复人的显示状态，1表示不显示，0表示显示
    @TableField("detail_status")
    private int detailStatus;    //明细显示状态：1表示不显示，0表示显示
    @TableField("is_matcher")
    private boolean isMatcher=false;    //是否是公式入账的：true 1 表示是， false 0表示不是
    @TableField("calc_u")//如果是+30u 带U就不计算费率 默认false不带U计算
    private boolean calcU=false;//直接增加 +30u 的时候会计算费率这个点，不应该计算费率，只需要根据当前设置的固定汇率计算除金额，跟+30u

}
