package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 操作汇率||费率
 */
@ApiModel("rate")
@Accessors(chain = true)
@Data
public class Rate {
    @TableId(type=IdType.AUTO)
    private String id;
    @TableField("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @TableField("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("add_time")
    private Date addTime;//添加的时间
    @TableField("over_due")
    private Long overDue;    //逾期  超时
    @TableField("handle_status")
    private int handleStatus;    //操作人的显示状态，1表示不显示，0表示显示
    @TableField("call_back_status")
    private int callBackStatus;    //回复人的显示状态，1表示不显示，0表示显示
    @TableField("detail_status")
    private int detailStatus;    //明细显示状态：1表示不显示，0表示显示

}
