package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel(value = "AccountUserAdminDTO", description = "手动设置超级管理")
public class AccountUserAdminDTO {
    @ApiModelProperty(value = "用户ID")
    private String userId;
    @ApiModelProperty(value = "用户名")
    private String username;
    @ApiModelProperty(value = "昵称")
    private String nickname;
    @ApiModelProperty(value = "是否为超级管理员")
    private Boolean cjgl;
    @ApiModelProperty(value = "到期时间")
    private Date createTime;




}
