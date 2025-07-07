package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 群发标签
 */

@Accessors(chain = true)
@Data
@TableName(value = "group_tag",   autoResultMap = true)
public class GroupTag {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("tag_name")
    private String tagName;
    @TableField("create_time")
    private Date createTime=new Date();//创建 时间

}
