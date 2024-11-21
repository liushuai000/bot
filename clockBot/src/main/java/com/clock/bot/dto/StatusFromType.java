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
@ApiModel(value = "StatusFromType",description = "返回给前端的数据")
public class StatusFromType {
    @ApiModelProperty(value = "用户id")
    public String userId;
    @ApiModelProperty(value = "用户名")
    public String username;
    @ApiModelProperty(value = "用户昵称")
    public String firstname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "首次上班时间")
    public Date workFirstTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "最后下班时间")
    public Date workEndDownTime;
    @ApiModelProperty(value = "群组id")
    public String groupId;
    @ApiModelProperty(value = "吃饭时间")
    public String eatTime;//显示今天总吃饭多少时间  2分30秒

    @ApiModelProperty(value = "上厕所时间")
    public String toiletTime;

    @ApiModelProperty(value = "抽烟时间")
    public String smokingTime;

    @ApiModelProperty(value = "其它时间")
    public String otherTime;

    @ApiModelProperty(value = "吃饭次数")
    public Integer eat;
    @ApiModelProperty(value = "上厕所次数")
    public Integer toilet;
    @ApiModelProperty(value = "抽烟次数")
    public Integer smoking;
    @ApiModelProperty(value = "其它次数")
    public Integer other;
    @ApiModelProperty(value = "有效工作时长")
    public String pureWorkTimeString;
    @ApiModelProperty(value = "上班时长")
    public String shangbanTime;
    @ApiModelProperty(value = "总活动时长")
    public String  huoDongTime;
}
