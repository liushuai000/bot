package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Accessors(chain = true)
@Data
@ApiModel("StatusDTO")
public class StatusDTO {
    @ApiModelProperty(value="id")
    public String id;
    @ApiModelProperty("group_id")
    private String groupId;//群组id  根据群组id设置全局是 否显示明细这些
    @ApiModelProperty("group_title")
    private String groupTitle;//群组名称
    @ApiModelProperty("handle_status")
    private int handleStatus;    //操作人的显示状态，1表示不显示，0表示显示
    @ApiModelProperty("call_back_status")
    private int callBackStatus;    //回复人的显示状态，1表示不显示，0表示显示
    @ApiModelProperty("detail_status")
    private int detailStatus;    //明细显示状态：1表示不显示，0表示显示
    @ApiModelProperty("show_money_status")
    private int showMoneyStatus=2;    //0显示余额：1表示显示USDT||显示usdt， 2表示显示全部
    @ApiModelProperty("show_few")
    private int showFew=3;    //显示1条 3条  5条：默认3条
    @ApiModelProperty("display_sort")
    private int displaySort=1;    //显示分类 1不显示 0表示显示
    @ApiModelProperty("account_handler_money")
    private BigDecimal accountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account issue 里的手续费是记录单笔历史的
    @ApiModelProperty("issue_handler_money")
    private BigDecimal issueHandlerMoney=BigDecimal.ZERO;//全局下方手续费
    @ApiModelProperty("show_handler_money_status")
    private int showHandlerMoneyStatus=1;//手续费显示状态：1表示不显示，0表示显示
    @ApiModelProperty("set_time")
    private Date setTime;   //账单 设置的过期时间 日切时间
    @ApiModelProperty("riqie")
    private boolean riqie;   //是否设置了日切 true 1 false 0


}
