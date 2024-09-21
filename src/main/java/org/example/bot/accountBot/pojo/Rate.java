package org.example.bot.accountBot.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Rate {
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
