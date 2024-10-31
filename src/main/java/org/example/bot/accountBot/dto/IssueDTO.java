package org.example.bot.accountBot.dto;

import com.baomidou.mybatisplus.annotation.TableField;
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
public class IssueDTO{
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("exchange")
    private BigDecimal exchange=BigDecimal.ONE;    //汇率
    @ApiModelProperty("rate")
    private BigDecimal rate=BigDecimal.ZERO;    //费率
    @ApiModelProperty("groupId")
    private String groupId;//群组id
    @ApiModelProperty("userId")
    private String userId;
    @ApiModelProperty("username")
    private String username;//操作账户
    @ApiModelProperty("firstName")
    private String firstName;//操作人名称  是firstName+lastName
    @ApiModelProperty("callBackName")
    private String callBackName;//操作账户
    @ApiModelProperty("callBackFirstName")
    private String callBackFirstName;//操作人名称  是firstName+lastName
    @TableField("is_matcher")
    private boolean isMatcher=false;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT+8")
    @ApiModelProperty("addTime")
    private Date addTime;
    @ApiModelProperty("downed")
    private BigDecimal downed;//金额
    @ApiModelProperty("down")
    private BigDecimal down;//未下发
    @ApiModelProperty("issueHandlerMoney")
    private BigDecimal issueHandlerMoney=BigDecimal.ZERO;//全局下方手续费


}
