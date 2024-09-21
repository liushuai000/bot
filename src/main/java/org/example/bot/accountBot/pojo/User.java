package org.example.bot.accountBot.pojo;

import lombok.Data;

/**
 * 用户信息
 */
@Data
public class User {
    //主键id
    private int user_id;
    //用户名
    private String username;
    //昵称
    private String firstname;
}
