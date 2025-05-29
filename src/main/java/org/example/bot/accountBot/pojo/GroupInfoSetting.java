package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 此群包含 群组和用户信息 公用属性
 */
@Accessors(chain = true)
@Data
@TableName(value = "group_info_setting", autoResultMap = true)
public class GroupInfoSetting {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("english")
    public Boolean english; //是否切换英文显示 false 是因为 true是中文
    @TableField("group_id")
    private Long groupId;//群组id 或者userid
//    @TableField("type")
//    private String type;//是群组还是 用户 还是超级群组 或者频道类型

}
