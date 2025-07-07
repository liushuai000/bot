package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Data
@ApiModel("ConfigDTO")
public class ConfigDTO {

    /**
     * 支付文本内容（可选）
     * 例如：支付说明、付款方式等文字信息
     */
    @ApiModelProperty("payText")
    private String payText;
    /**
     * 管理员用户名（可选）
     * 用于指定或识别管理员账号名称
     */
    private String adminUserName;
    /**
     * 支付图片链接或标识（可选）
     * 例如：二维码图片地址或其他支付相关图片
     */
    @ApiModelProperty("payImage")
    private String payImage;

    /**说明频道地址
     */
    @ApiModelProperty("about")
    private String about;

    /**
     * 包装完成文本内容（可选）
     * 例如：包装完成提示、发货通知等
     */
    @ApiModelProperty("packagingCompleteText")
    private String packagingCompleteText;

    @ApiModelProperty("buttonRows")
    private List<List<AdTimeButtonDTO>> buttonRows=new ArrayList<>();//这里直接用AdTimeButtonDTO 不新建类了属性都一样

}
