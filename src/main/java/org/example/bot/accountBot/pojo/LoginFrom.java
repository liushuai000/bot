package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@TableName(value = "login_from",   autoResultMap = true)
public class LoginFrom {

    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id
    @TableField("password")
    public String password;
    @TableField("username")
    public String username;

}
