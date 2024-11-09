package org.example.bot.accountBot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.pojo.WalletListener;

@Mapper
public interface WalletListenerMapper extends BaseMapper<WalletListener> {

}
