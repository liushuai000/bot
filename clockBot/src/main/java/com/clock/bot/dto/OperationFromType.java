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
@ApiModel(value = "OperationFromType",description = "返回给前端的数据")
public class OperationFromType {
    @ApiModelProperty(value = "用户id")
    public String userId;
    @ApiModelProperty(value = "用户名")
    public String username;
    @ApiModelProperty(value = "用户昵称")
    public String firstname;
    @ApiModelProperty("吃饭")
    private String eat;//2022-11-18 13:30:00-14:30:00  操作开始和结束时间
    @ApiModelProperty("上厕所")
    private String toilet;//0无活动: 1:吃饭 2:上厕所 3:抽烟 4:其他
    @ApiModelProperty("抽烟时间")
    private String smoking;//0无活动: 1:吃饭 2:上厕所 3:抽烟 4:其他
    @ApiModelProperty("其他")
    private String other;//0无活动: 1:吃饭 2:上厕所 3:抽烟 4:其他
    @ApiModelProperty(value = "吃饭时间")
    public String eatDateText;//2分30秒
    @ApiModelProperty(value = "上厕所时间")
    public String toiletDateText;
    @ApiModelProperty(value = "抽烟时间")
    public String smokingDateText;
    @ApiModelProperty(value = "其他时间")
    public String otherDateText;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "上班时间")
    public Date workTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "下班时间")
    public Date workDownTime;




}
