package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
@Accessors(chain = true)
@Data
@TableName(value = "user_order",  autoResultMap = true)
public class UserOrder {

    public static final String STATUS_YIGOUMAI = "已购买";
    public static final String STATUS_WGM = "未购买";
    public static final String STATUS_YGP = "已过期";
    public static final String STATUS_YQX = "已取消";


    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("amount")
    private BigDecimal amount;//购买金额
    @TableField("type")
    private String type;//类型  已购买  购买数量  价格
    @TableField("config_edit_button_name")
    private String configEditButtonName;//configEditButton里的属性: 按钮 就是用户购买的哪个 为了防止删除掉按钮后而创建的这个
    @TableField("month")
    private String month;//configEditButton里的属性:  续费时长 1天 实际表示天
    @TableField("order_number")
    private String orderNumber;//订单号
    @TableField("create_time")
    private Date createTime=new Date();//创建时间
    @TableField("end_time")
    private Date endTime;//订单结束时间
    @TableField("user_id")
    public String userId; //用户id 是哪位用户的订单标识
}
