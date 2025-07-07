package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.bot.accountBot.botConfig.BotFileItemInfo;

/**
 * 广播的文件
 */
@Accessors(chain = true)
@Data
@TableName(value = "file_item_guangbo",   autoResultMap = true)
public class FileItemGuangbo implements BotFileItemInfo {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("name")
    private String name;
    @TableField("url")
    private String url;
    @TableField("size")
    private long size;
    @TableField("type")
    private String type;



    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getUrl() {
        return url;
    }
    @Override
    public long getSize() {
        return size;
    }
    @Override
    public String getType() {
        return type;
    }


}
