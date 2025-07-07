package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@ApiModel("AdTimeButtonDTO")
public class AdTimeButtonDTO {
    @ApiModelProperty("type")
    private String type;//新加type  有的可能用不上 用来区分内置按钮用 因为内置按钮有的可以修改text 所有用type来区分 是否内置按钮
    @ApiModelProperty("text")
    private String text;
    @ApiModelProperty("link")
    private String link;
    @ApiModelProperty("rowIndex")
    private Integer rowIndex;
    @ApiModelProperty("buttonIndex")
    private Integer buttonIndex;
    @ApiModelProperty("amount")
    private String amount;//续费时长


}
