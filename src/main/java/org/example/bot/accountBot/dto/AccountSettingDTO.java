package org.example.bot.accountBot.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@ApiModel("AccountSettingDTO")
public class AccountSettingDTO {
    @ApiModelProperty("groupLanguage")
    private String groupLanguage;
    @ApiModelProperty("expireNotice")
    private String expireNotice;
    @ApiModelProperty("adminExpireNotice")
    private String adminExpireNotice;//本群权限人到期提醒
    @ApiModelProperty("noneNotice")
    private Boolean noneNotice;//是否开启提醒
    @ApiModelProperty("adminNotice")
    private Boolean adminNotice;//管理是否开启提醒


    @ApiModelProperty("englishExpireNotice")
    private String englishExpireNotice;
    @ApiModelProperty("englishAdminExpireNotice")
    private String englishAdminExpireNotice;


    @ApiModelProperty("notGroupAdminNotice")
    private Boolean notGroupAdminNotice;//本群不是管理
    @ApiModelProperty("notGroupAdminNoticeHtml")
    private String  notGroupAdminNoticeHtml;//本群不是管理
    @ApiModelProperty("englishNotGroupAdminNotice")
    private String englishNotGroupAdminNotice;//本群不是管理

    @ApiModelProperty("startMessageNoticeSwitch")
    private Boolean startMessageNoticeSwitch;
    @ApiModelProperty("startMessageNotice")
    private String  startMessageNotice;
    @ApiModelProperty("englishStartMessageNotice")
    private String englishStartMessageNotice;
    @ApiModelProperty("privateMessageLanguage")
    private String privateMessageLanguage;
    @ApiModelProperty("trialDurationHours")
    private Integer trialDurationHours;//初始化试用时长

}
