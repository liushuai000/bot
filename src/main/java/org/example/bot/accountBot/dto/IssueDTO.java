package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("IssueDTO")
public class IssueDTO extends RateDTO{
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("rateId")
    private int rateId;//外键id rate的因为是多个账单 一对多 (+1000/7*0.05)单笔的就是一对一汇率
    @ApiModelProperty("groupId")
    private String groupId;//群组id
    @ApiModelProperty("userId")
    private String userId;//操作人id
    @ApiModelProperty("callBackUserId")
    private String callBackUserId;//回复人id
    @ApiModelProperty("downed")
    private BigDecimal downed;    //已下发 已出帐
    @ApiModelProperty("down")
    private BigDecimal down;    //未下发
    @ApiModelProperty("add_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @ApiModelProperty("riqie")
    private boolean riqie=false;   //是否设置了日切  用status里的 set_riqie 查询status里的日切时间
    @ApiModelProperty("issueHandlerMoney")
    private BigDecimal issueHandlerMoney=BigDecimal.ZERO;//全局下方手续费


}
