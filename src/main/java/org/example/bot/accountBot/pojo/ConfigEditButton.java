package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.bot.accountBot.botConfig.BotButtonInfo;


@Accessors(chain = true)
@Data
@TableName(value = "config_edit_button",   autoResultMap = true)
public class ConfigEditButton implements BotButtonInfo {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("config_edit_id")
    private String configEditId;
    @TableField("month")
    private String month;//续费时长 1天 实际表示天
    @TableField("text")
    private String text;//按钮文本
    @TableField("link")
    private String link;//这里表示金额
    @TableField("row_index")
    private int rowIndex;//行下标
    @TableField("button_index")
    private int buttonIndex;//按钮下标

    @Override
    public String getText() {
        return text;
    }
    @Override
    public String getLink() {
        return link;
    }
    @Override
    public Integer getRowIndex() {
        return rowIndex;
    }
    @Override
    public Integer getButtonIndex() {
        return buttonIndex;
    }
}
