package com.clock.bot.botConfig;

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
    protected ReplyKeyboardMarkup sendSelfReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true); // 可选：一旦用户选择了按钮，键盘会消失
        // 创建按钮行
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("获取个人信息"));
        row1.add(new KeyboardButton("使用说明"));
//        KeyboardRow row2 = new KeyboardRow();
//        row2.add(new KeyboardButton("使用说明"));
        // 将按钮行添加到键盘列表中
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
//        keyboard.add(row2);
        // 设置键盘
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
    /**
     * 设置底部按钮
     * @return
     */
    protected ReplyKeyboardMarkup sendReplyKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true); // 可选：一旦用户选择了按钮，键盘会消失
        // 创建按钮行
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("上班"));
        row1.add(new KeyboardButton("下班"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("吃饭"));
        row2.add(new KeyboardButton("上厕所"));
        row2.add(new KeyboardButton("抽烟"));
        row2.add(new KeyboardButton("其它"));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("回座"));
        // 将按钮行添加到键盘列表中
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        // 设置键盘
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    /**
     * 发送内嵌键盘
     * @param buttonTextName 按钮名称
     */
    protected InlineKeyboardMarkup sendInlineKeyboard(String buttonTextName, String callbackData,String url) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setCallbackData(callbackData);
        inlineKeyboardButton.setText(buttonTextName);
        inlineKeyboardButton.setUrl(url);


        rowInline.add(inlineKeyboardButton);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }
}
