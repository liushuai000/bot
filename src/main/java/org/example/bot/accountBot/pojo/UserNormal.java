package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 记录管理员用户信息  拉进群时  添加管理员  权限为false
 */
@ApiModel("user_normal")
@Accessors(chain = true)
@Data
@TableName(value = "user_normal", schema = "bot", autoResultMap = true)
public class UserNormal {
    @TableId(type= IdType.AUTO,value = "id")
    public int id; //主键id
    @TableField("user_id")
    public String userId; //主键id
    @TableField("username")
    public String username;  //用户名 liuxiaolon
    @TableField("is_admin")
    public boolean isAdmin=false;//是否为本群管理员  只有在user表中的is_super_admin 是true拉进来的机器人才可设置
    @TableField("group_id")
    private String groupId;//群组id

    @TableField("create_time")
    private Date createTime;//创建时间

}
