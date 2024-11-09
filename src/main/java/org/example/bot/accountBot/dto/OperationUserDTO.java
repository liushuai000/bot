package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Accessors(chain = true)
@Data
@ApiModel("OperationUserDTO")
public class OperationUserDTO {
    @ApiModelProperty("group_id")
    private String groupId;
    @ApiModelProperty("count")
    private AtomicReference<BigDecimal> count = new AtomicReference<>(BigDecimal.ZERO);//总入款
    @ApiModelProperty("countDowned")
    private AtomicReference<BigDecimal> countDowned = new AtomicReference<>(BigDecimal.ZERO);//总入款
    @ApiModelProperty("countCishu")
    private int countCishu;//总入款次数
    @ApiModelProperty("countDownedCishu")
    private int countDownedCishu;//总下发次数
    @ApiModelProperty("callBackName")
    private String operationName;//操作人账户
    @ApiModelProperty("callBackFirstName")
    private String operationFirstName;//操作人名称
    @ApiModelProperty("down")
    private BigDecimal down;//未下发

    public void addTotal(BigDecimal amount) {
        if (amount != null) {
            BigDecimal currentTotal = this.count.get();
            BigDecimal newTotal = currentTotal.add(amount);
            this.count.set(newTotal);
        } else {
            // 处理 null 值，可以根据业务需求进行调整
            System.out.println("Amount cannot be null");
        }
    }
    public void incrementCount() {
        this.countCishu++;

    }
    public void addIssueTotal(BigDecimal amount) {
        if (amount != null) {
            BigDecimal currentTotal = this.countDowned.get();
            BigDecimal newTotal = currentTotal.add(amount);
            this.countDowned.set(newTotal);
        } else {
            // 处理 null 值，可以根据业务需求进行调整
            System.out.println("Amount cannot be null");
        }
    }
    public void IssueIncrementCount() {
        this.countDownedCishu++;
    }



}
