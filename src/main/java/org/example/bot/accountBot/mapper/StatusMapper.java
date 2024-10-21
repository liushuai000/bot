package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Status;

@Mapper
public interface StatusMapper  extends BaseMapper<Status> {
}
