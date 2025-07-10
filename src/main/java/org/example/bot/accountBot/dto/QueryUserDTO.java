package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@ApiModel(value = "QueryUserDTO",description = "查询")
public class QueryUserDTO {
    @ApiModelProperty("nickname")
    private String nickname;//用户昵称
    @ApiModelProperty("startTime")
    private String startTime;//注册开始时间
    @ApiModelProperty("endTime")
    private String endTime;//注册
    @ApiModelProperty("pageNum")
    private Integer pageNum;
    @ApiModelProperty("pageSize")
    private Integer pageSize;
    @ApiModelProperty("isRepeatUser")
    private Boolean isRepeatUser;//是否去重用户id


}
