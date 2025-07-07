package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 群组里的用户  类型不是机器人的用户
 */

@Accessors(chain = true)
@Data
@TableName(value = "group_inner_user",   autoResultMap = true)
public class GroupInnerUser {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("user_id")
    public String userId; //主键id
    @TableField("username")
    public String username;
    @TableField("first_name")
    public String firstName;
    @TableField("last_name")
    public String lastName;
    @TableField("group_id")
    private String groupId;//群组id
    @TableField("type")
    private String type;//群类型 群名称 私聊 退群 三种
    @TableField("create_time")
    private Date createTime;//入群时间
    @TableField("status")
    private boolean status=false;//状态  已踢出 已退群 true是已退群
    @TableField("last_time")
    private Date lastTime;//最后发言时间


    public String getFirstLastName(){
        String fname=firstName==null?"":firstName;
        String lname=lastName==null?"":lastName;
        return fname+lname;
    }


}
