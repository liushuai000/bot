package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
@ApiModel("ManagerGroupMessageDTO")
public class ManagerGroupMessageDTO {
    @ApiModelProperty("message")
    private String message;
    @ApiModelProperty("groupIds")
    private List<String> groupIds;
    @ApiModelProperty("userIds")
    private List<String> userIds;
    @ApiModelProperty("fileList")
    private List<FileItemDTO> fileList;


}
