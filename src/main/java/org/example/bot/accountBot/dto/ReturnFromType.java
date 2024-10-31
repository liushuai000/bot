package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

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



}
