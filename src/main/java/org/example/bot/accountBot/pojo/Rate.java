package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 操作汇率||费率||账单过期时间显示
 */
@Accessors(chain = true)
@Data
@TableName(value = "rate",  autoResultMap = true)
public class Rate {
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @TableField("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("add_time")
    private Date addTime;//添加的时间
    @TableField("is_matcher")
    private boolean isMatcher=false;    //是否是公式入账的：true 1 表示是， false 0表示不是
    @TableField("calc_u")//如果是+30u 带U就不计算费率 默认false不带U计算
    private boolean calcU=false;// true 是计算 false是不计算 直接增加 +30u 的时候会计算费率这个点，不应该计算费率，只需要根据当前设置的固定汇率计算除金额，跟+30u


}
