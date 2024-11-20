package com.clock.bot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Accessors(chain = true)
@Data
@ApiModel(value = "OperationType",description = "此类前端查询类")
public class OperationType {
    @ApiModelProperty("groupId")
    private String groupId;//群组id
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("addTime")
    private Date addTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("addEndTime")
    private Date addEndTime;
    @ApiModelProperty("userId")
    private String userId;
    @ApiModelProperty("username")
    private String username;//用户名


}
