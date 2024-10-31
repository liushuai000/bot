package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;


@Accessors(chain = true)
@Data
@ApiModel(value = "QueryType",description = "此类前端查询类")
public class QueryType {
    @ApiModelProperty("groupId")
    private String groupId;//群组id
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    @ApiModelProperty("addTime")
    private Date addTime;
    @ApiModelProperty("findAll")
    private boolean findAll;
    @ApiModelProperty("isOperation")
    private boolean isOperation;//是否是操作人
    @ApiModelProperty("username")
    private String username;//用户名




}
