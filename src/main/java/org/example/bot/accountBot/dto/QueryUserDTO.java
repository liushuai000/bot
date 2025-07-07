package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel(value = "QueryUserDTO",description = "查询")
public class QueryUserDTO {
    @ApiModelProperty("nickname")
    private String nickname;//用户昵称
    @ApiModelProperty("startTime")
    private Date startTime;//注册开始时间
    @ApiModelProperty("endTime")
    private Date endTime;//注册
    @ApiModelProperty("pageNum")
    private Integer pageNum;
    @ApiModelProperty("pageSize")
    private Integer pageSize;



}
