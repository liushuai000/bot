package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel(value = "QueryDTO",description = "查询")
public class QueryGroupDTO {
    @ApiModelProperty("groupId")
    private String groupId;
    @ApiModelProperty("groupName")
    private String groupName;
    @ApiModelProperty("startTime")
    private Date startTime;//注册开始时间
    @ApiModelProperty("endTime")
    private Date endTime;//注册
    @ApiModelProperty("pageNum")
    private Integer pageNum;
    @ApiModelProperty("pageSize")
    private Integer pageSize;

}
