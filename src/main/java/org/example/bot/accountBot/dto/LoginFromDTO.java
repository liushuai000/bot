package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@ApiModel("LoginFromDTO")
public class LoginFromDTO {
    @ApiModelProperty("password")
    public String password;//使用SM4校验
    @ApiModelProperty("username")
    public String username;



}
