package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@TableName(value = "account_setting",   autoResultMap = true)
public class AccountSetting {
    @TableId(type= IdType.AUTO,value = "id")
    public Long id; //主键id

    @TableField("group_language")
    private boolean groupLanguage;//true中文 false 英文
    @TableField("expire_notice")
    private String expireNotice;
    @TableField("admin_expire_notice")
    private String adminExpireNotice;

    @TableField("english_expire_notice")
    private String englishExpireNotice;
    @TableField("english_admin_expire_notice")
    private String englishAdminExpireNotice;

    @TableField("none_notice")
    private Boolean noneNotice;//是否开启提醒
    @TableField("admin_notice")
    private Boolean adminNotice;//管理是否开启提醒


    @TableField("not_group_admin_notice")
    private Boolean notGroupAdminNotice;//本群不是管理

    @TableField("not_group_admin_notice_html")
    private String  notGroupAdminNoticeHtml;//本群不是管理

    @TableField("english_not_group_admin_notice")
    private String englishNotGroupAdminNotice;//本群不是管理
    @TableField("start_message_notice_switch")
    private Boolean  startMessageNoticeSwitch;
    @TableField("start_message_notice")
    private String  startMessageNotice;
    @TableField("english_start_message_notice")
    private String englishStartMessageNotice;
    @TableField("private_message_language")
    private Boolean privateMessageLanguage;
}
