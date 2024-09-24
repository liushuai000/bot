package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息
 */
@ApiModel("user")
@Accessors(chain = true)
@Data
public class User {
    @TableId(type= IdType.AUTO)
    private String id;
    //主键id
    private int user_id;
    //用户名
    private String username;
    //昵称
    private String firstname;
}
