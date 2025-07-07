package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel(value = "ReturnUserDTO", description = "返回给前端的用户数据")
public class ReturnUserDTO {
    @ApiModelProperty(value = "用户ID")
    private String userId;
    @ApiModelProperty(value = "群ID")
    private String groupId;//如果群id为空表示是私聊的
    @ApiModelProperty(value = "用户名")
    private String username;
    @ApiModelProperty(value = "昵称")
    private String nickname;
    @ApiModelProperty(value = "来自群组")
    private String fromGroup;
    @ApiModelProperty(value = "是否使用")
    private Boolean status;
    @ApiModelProperty(value = "到期时间")
    private Date expireTime;
    @ApiModelProperty(value = "最后发言时间")
    private Date chatTime;




}
