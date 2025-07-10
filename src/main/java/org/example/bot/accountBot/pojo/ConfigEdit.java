package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@TableName(value = "config_edit",   autoResultMap = true)
public class ConfigEdit {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("pay_text")
    private String payText;
    @TableField("admin_user_name")
    private String adminUserName;
    @TableField("pay_image")
    private String payImage;
    @TableField("show_renewal")
    private Boolean showRenewal=false;//是否显示续费开关


}
