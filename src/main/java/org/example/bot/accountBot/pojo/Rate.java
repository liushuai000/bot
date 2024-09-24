package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 操作汇率和操作人的动作 时间
 */
@ApiModel("rate")
@Accessors(chain = true)
@Data
public class Rate {
    @TableId(type=IdType.AUTO)
    private String id;
    //汇率
    private BigDecimal exchange=BigDecimal.ONE;
    //费率
    private BigDecimal rate=BigDecimal.ZERO;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;

    private Long overDue;
    //操作人的显示状态，1表示不显示，0表示显示
    private int handlestatus;
    //回复人的显示状态，1表示不显示，0表示显示
    private int callBackStatus;
    //明细显示状态：1表示不显示，0表示显示
    private int detailStatus;

}
