package com.clock.bot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 打卡机器人
 */
@Accessors(chain = true)
@Data
@ApiModel(value = "ReturnClockFromType",description = "返回给前端的数据")
public class ReturnClockFromType {
    @ApiModelProperty(value = "操作1次 吃饭2次 上厕所这些")
    public String operationText;
    @ApiModelProperty(value = "操作记录时长 吃饭:2024/11/18 13:30:00- 14:30:00")
    public String operationHistoryTime;
    @ApiModelProperty(value = "操作时长 吃饭:5秒")
    public String operationTime;

    @ApiModelProperty(value = "操作开始时间")
    public Date startTime;//前端没有用到
    @ApiModelProperty(value = "操作结束时间")
    public Date endTime;//前端没有用到
    @ApiModelProperty(value = "用户id")
    public String userId;
    @ApiModelProperty(value = "用户名")
    public String username;
    @ApiModelProperty(value = "用户昵称")
    public String firstname;
    @ApiModelProperty(value = "群组id")
    public String groupId;
    @ApiModelProperty(value = "创建时间")
    public Date createTime;
    @ApiModelProperty(value = "群组标题")
    public String groupTitle;
    @ApiModelProperty(value = "true上班 false下班 ")
    public String status;//true上班 false下班 是在idea里的备注
    @ApiModelProperty(value = "是否执行活动 false没有执行 true执行中")
    public Boolean returnHome=false;
    @ApiModelProperty(value = "上班时间")
    public Date workTime;
    @ApiModelProperty(value = "下班时间")
    public Date workDownTime;



}
