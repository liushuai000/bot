package org.example.bot.accountBot.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.WalletListenerMapper;
import org.example.bot.accountBot.pojo.WalletListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Service
@Slf4j
public class WalletListenerService {
    @Autowired
    public WalletListenerMapper walletListenerMapper;


    public List<WalletListener> queryAll(String userId) {
        QueryWrapper<WalletListener> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return walletListenerMapper.selectList(queryWrapper);
    }
    public List<WalletListener> queryAll() {
        return walletListenerMapper.selectList(null);
    }

    public void createWalletListener(WalletListener walletListener) {
        walletListenerMapper.insert(walletListener);

    }

    public void updateWalletListener(String userId,String address,String nickName) {
        UpdateWrapper<WalletListener> wrapper = new UpdateWrapper<>();
        wrapper.set("nickname",nickName);
        wrapper.eq("user_id",userId);
        wrapper.eq("address",address);
        walletListenerMapper.update(null,wrapper);
    }

    public WalletListener findByAddress(String address, Long userId) {
        QueryWrapper<WalletListener> wrapper = new QueryWrapper<>();
        wrapper.eq("address",address);
        wrapper.eq("user_id",userId);
        return walletListenerMapper.selectOne(wrapper);
    }

    public void deleteWalletListener(WalletListener wallet) {
        walletListenerMapper.deleteById(wallet.getId());
    }
}
