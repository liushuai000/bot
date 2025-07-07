package org.example.bot.accountBot.botConfig;

import org.example.bot.accountBot.pojo.GroupInfoSetting;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        KeyboardRow row3 = new KeyboardRow();
        row1.add(new KeyboardButton("获取个人信息（personal information）"));
        row2.add(new KeyboardButton("监听列表（listening address）"));
        row2.add(new KeyboardButton("使用说明（illustrate）"));
        row3.add(new KeyboardButton("群发广播（Group Broadcast）"));
        row3.add(new KeyboardButton("自助续费（Self-service renewal）"));
        // 将按钮行添加到键盘列表中
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        // 设置键盘
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    public void sendButton(SendMessage sendMessage,  Map<String, String> buttonText) {
        if (buttonText == null || buttonText.isEmpty()) {
            return;
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Map.Entry<String, String> entry : buttonText.entrySet()) {
            String buttonTextName = entry.getKey();
            String url = entry.getValue();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonTextName);
            button.setUrl(url); // 使用 url 属性
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            rowsInline.add(rowInline);
        }
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }
}
