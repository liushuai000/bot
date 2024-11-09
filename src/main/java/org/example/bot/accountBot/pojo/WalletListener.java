package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

/**
 * 表示钱包监听器的类。
 */
@ApiModel("WalletListener")
@Accessors(chain = true)
@Data
@TableName(value = "wallet_listener", schema = "bot", autoResultMap = true)
public class WalletListener {
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("user_id")
    private String userId;//谁创建的就是谁的id
    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Date createTime;
    /**
     * 账户地址。TWxokzzX2Y68iVdhZg8LbJWp9bjsj8w9Pc
     */
    @TableField("address")
    private String address;
    /**
     * 账户昵称。
     */
    @TableField("nickname")
    private String nickname;

}
