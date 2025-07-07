package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 此类查询用于查询所有群
 */
@Accessors(chain = true)
@Data
@ApiModel("MangerAllGroupQueryDTO")
public class MangerAllGroupQueryDTO {

    @ApiModelProperty("groupIds")
    private List<String> groupIds;//群组id


}
