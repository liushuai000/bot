package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Accessors(chain = true)
@Data
@ApiModel(value = "QueryType",description = "此类前端查询类")
public class QueryType {
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
    @ApiModelProperty("findAll")
    private boolean findAll;
    @ApiModelProperty("operation")
    private Boolean operation;//是否是操作人  默认空是全部
    @ApiModelProperty("username")
    private String username;//用户名




}
