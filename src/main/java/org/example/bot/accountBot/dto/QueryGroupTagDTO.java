package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel(value = "QueryGroupTagDTO",description = "查询")
public class QueryGroupTagDTO {
    @ApiModelProperty("groupId")
    private String groupId;
    @ApiModelProperty("groupName")
    private String groupName;
    @ApiModelProperty("tagName")
    private String tagName;
    @ApiModelProperty("startTime")
    private String startTime;//注册开始时间
    @ApiModelProperty("endTime")
    private String endTime;//注册
    @ApiModelProperty("pageNum")
    private Integer pageNum;
    @ApiModelProperty("pageSize")
    private Integer pageSize;

}
