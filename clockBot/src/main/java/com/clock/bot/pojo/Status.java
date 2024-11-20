package com.clock.bot.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Accessors(chain = true)
@Data
@TableName(value = "status", autoResultMap = true)
public class Status {
        @TableId(type= IdType.AUTO,value = "id")
        private int id;
        @TableField("group_id")
        private String groupId;//群组id  根据群组id设置全局是 否显示明细这些
        @TableField("group_title")
        private String groupTitle;//群组名称
        @TableField("set_time")
        private Date setTime=this.parseDate();   //账单 设置的过期时间 日切时间

        @TableField("set_start_time")
        private Date setStartTime=new Date();
        @TableField("riqie")
        private boolean riqie;   //是否设置了日切 true 1 false 0
        @TableField("set_cut_off_time")
        private Date setCutOffTime; // 用户设置的日切时间
        public Date parseDate() {
                // 默认是每天晚上12点结束
                LocalDateTime tomorrow = LocalDateTime.now().plusDays(0).withHour(23).withMinute(59).withSecond(59);
                return Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());
        }

        public Date getCurrentCutOffTime() {
                // 获取当前时间
                LocalDateTime now = LocalDateTime.now();
                // 获取用户设置的日切时间
                LocalDateTime userCutOffTime = setCutOffTime != null ? setCutOffTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
                // 获取默认的日切时间
                LocalDateTime defaultCutOffTime = setTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                // 如果用户设置了日切时间并且当前时间已经超过了设置的日切时间，则使用用户设置的日切时间
                if (userCutOffTime != null && now.isAfter(defaultCutOffTime)) {//now.isAfter(defaultCutOffTime)
                        return setCutOffTime;
                } else {
                        // 否则继续使用默认的日切时间
                        return setTime;
                }
        }


}