package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.bot.accountBot.pojo.Account;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

}
