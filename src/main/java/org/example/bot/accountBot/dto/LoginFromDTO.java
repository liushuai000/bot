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
    public String password;
    @ApiModelProperty("username")
    public String username;
    @ApiModelProperty("token")
    public String token;

}
