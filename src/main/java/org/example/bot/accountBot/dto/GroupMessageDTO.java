package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@Data
@ApiModel("GroupMessageDTO")
public class GroupMessageDTO {
    @ApiModelProperty("groupId")
    private String groupId;
    @ApiModelProperty("content")
    private String content;
    @ApiModelProperty("fileList")
    private List<FileItemDTO> fileList;
}
