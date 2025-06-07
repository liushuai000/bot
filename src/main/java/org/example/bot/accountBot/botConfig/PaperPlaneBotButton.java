package org.example.bot.accountBot.botConfig;

import org.example.bot.accountBot.pojo.GroupInfoSetting;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * 此类设置机器人按钮
 */
public class PaperPlaneBotButton {
    /**
     * 设置底部按钮
     * @return
     */
    protected ReplyKeyboardMarkup sendReplyKeyboard(GroupInfoSetting groupInfoSetting) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false); // 可选：一旦用户选择了按钮，键盘会消失
        // 创建按钮行
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        if (groupInfoSetting.getEnglish()){
            row1.add(new KeyboardButton("获取个人信息"));
            row2.add(new KeyboardButton("监听列表"));
            row2.add(new KeyboardButton("使用说明"));
        }else {
            row1.add(new KeyboardButton("personal information"));
            row2.add(new KeyboardButton("listening address"));
            row2.add(new KeyboardButton("illustrate"));
        }
        // 将按钮行添加到键盘列表中
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        // 设置键盘
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

}
