package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.NonNull;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.pojo.GroupInfoSetting;
import org.example.bot.accountBot.utils.TranslationExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
public class GroupInfoSettingBotMessage {
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;//中英文切换
    @Autowired
    private AccountBot accountBot;
    public GroupInfoSetting getGroupOrCreate(String text,Long chatId){
        GroupInfoSetting groupInfoSetting=groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", chatId));
        if (groupInfoSetting==null){// false是英语 true是中文
            groupInfoSetting=new GroupInfoSetting();
            if (!text.equals("切换中文") || !text.equals("切换英文")){
                groupInfoSetting.setEnglish(true);
            }else if (text.equals("切换中文") ){
                groupInfoSetting.setEnglish(true);
            } else if (text.equals("切换英文")) {
                groupInfoSetting.setEnglish(false);
            }else {
                groupInfoSetting.setEnglish(true);
            }
            groupInfoSetting.setGroupId(Long.valueOf(chatId));
            groupInfoSettingMapper.insert(groupInfoSetting);
        }else if (text.equals("切换中文") || text.equals("切换英文")){
            if (text.equals("切换中文") ){
                groupInfoSetting.setEnglish(true);
            } else if (text.equals("切换英文")) {
                groupInfoSetting.setEnglish(false);
            }else {
                groupInfoSetting.setEnglish(true);
            }
            groupInfoSettingMapper.updateById(groupInfoSetting);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("切换成功"+("Switching successful"));
            accountBot.sendMessage(sendMessage);
        }
        return groupInfoSetting;
    }
    //处理中英文切换
    public String handler(@NonNull String chatId, String text) {
        //false 表示中文 true中文
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", chatId));
        try {
            if (groupInfoSetting == null){
                groupInfoSetting = this.getGroupOrCreate(text,Long.valueOf(chatId));
            }
            String string = TranslationExample.translateText(text, groupInfoSetting.getEnglish());
            return string;
        }catch (Exception e){
            return e.getMessage();
        }
    }
}
