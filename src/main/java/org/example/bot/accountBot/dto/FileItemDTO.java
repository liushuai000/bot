package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@ApiModel("FileItemDTO")
@AllArgsConstructor
@NoArgsConstructor
public class FileItemDTO {
    @ApiModelProperty("name")
    private String name;
    @ApiModelProperty("url")
    private String url;
    @ApiModelProperty("size")
    private long size;
    @ApiModelProperty("type")
    private String type;

}
