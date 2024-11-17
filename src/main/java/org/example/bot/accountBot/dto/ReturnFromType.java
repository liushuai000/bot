package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;


@Accessors(chain = true)
@Data
@ApiModel(value = "ReturnFromType",description = "返回给前端的数据")
public class ReturnFromType {
    @ApiModelProperty(value = "入账信息")
    private List<AccountDTO> accountData;
    @ApiModelProperty(value = "出账信息")
    private List<IssueDTO> issueData;
    @ApiModelProperty(value = "回复用户信息")
    private List<CallbackUserDTO> callbackData;
    @ApiModelProperty(value = "汇率信息")
    private RateDTO rateData;
    @ApiModelProperty(value = "操作用户")
    private List<OperationUserDTO> operationData;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("startTime")
    private Date startTime;//日切开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("startEndTime")
    private Date startEndTime;//日切开始时间

}
