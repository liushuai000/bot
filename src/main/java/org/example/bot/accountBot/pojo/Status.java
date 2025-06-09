package org.example.bot.accountBot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

//此表为根据群组id设置操作人显示 明细显示 回复人显示
//群组状态表 群里显示 隐藏设置
@Accessors(chain = true)
@Data
@TableName(value = "status", schema = "bot", autoResultMap = true)
public class Status {
    @TableId(type= IdType.AUTO,value = "id")
    private int id;
    @TableField("group_id")
    private String groupId;//群组id  根据群组id设置全局是 否显示明细这些
    @TableField("group_title")
    private String groupTitle;//群组名称
    @TableField("handle_status")
    private int handleStatus;    //操作人的显示状态，1表示不显示，0表示显示
    @TableField("call_back_status")
    private int callBackStatus;    //回复人的显示状态，1表示不显示，0表示显示
    @TableField("detail_status")
    private int detailStatus;    //明细显示状态：1表示不显示，0表示显示
    @TableField("show_money_status")
    private int showMoneyStatus=2;    //0显示余额：1表示显示USDT||显示usdt， 2表示显示全部
    @TableField("show_few")
    private int showFew=3;    //显示1条 3条  5条：默认3条
    @TableField("display_sort")
    private int displaySort=1;    //显示分类 1不显示 0表示显示
    @TableField("account_handler_money")
    private BigDecimal accountHandlerMoney=BigDecimal.ZERO;//全局入款手续费 account issue 里的手续费是记录单笔历史的
    @TableField("issue_handler_money")
    private BigDecimal issueHandlerMoney=BigDecimal.ZERO;//全局下方手续费
    @TableField("old_money")
    private boolean oldMoney;//老旧手续费  如果是老旧手续费 显示已修改用
    @TableField("old_money_user_id")
    private String oldMoneyUserId;//老旧手续费 的修改人
    @TableField("old_money_person_time")
    private Date oldMoneyPersonTime;//老旧手续费 的修改时间
    @TableField("show_handler_money_status")
    private int showHandlerMoneyStatus=1;//手续费显示状态：1表示不显示，0表示显示
    @TableField("set_time")
    private Date setTime=this.parseDate();   //账单 设置的过期时间 日切时间
    @TableField("set_start_time")
    private Date setStartTime=new Date();   //账单 设置的过期时间 日切时间
    @TableField("riqie")
    private boolean riqie;   //是否设置了日切 true 1 false 0
    @TableField("down_exchange")
    private BigDecimal downExchange=BigDecimal.ZERO;    //下发汇率
    @TableField("down_rate")
    private BigDecimal downRate=BigDecimal.ZERO;    //下发费率
    @TableField("pmoney")
    private BigDecimal pmoney=BigDecimal.ZERO;    //p手动添加   P100{增加余额，只显示在独立分类} P+100和p100都是+100 而且只加未下发上


    /****** 以下是下发的信息 暂时没有用 ******/
    @TableField("d_address")
    private String dAddress;//下发地址
    @TableField("d_time")
    private Date dTime;//下发地址 修改时间
    @TableField("d_user_id")
    private String dUserId;//下发地址 修改人id



    public Date parseDate(){
        Date date = new Date();
        LocalDateTime tomorrow;
        if (date.getHours()>12){
            tomorrow=LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);
        }else {
            tomorrow=LocalDateTime.now().plusDays(0).withHour(12).withMinute(0).withSecond(0).withNano(0);
        }
        return Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
    }



}
