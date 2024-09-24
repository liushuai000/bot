package org.example.bot.accountBot.botConfig;


import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ButtonList {
    //实现list按钮
    public void implList(Message message, SendMessage sendMessage) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        //11111111111
//        Long chatId = message.getChatId();
//
//        String text = message.getText();
//        sendMsg(text,chatId);
        // 创建第一个按钮
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("按钮一名称");
        button1.setUrl("https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_8595438888645751841%22%7D&n_type=-1&p_from=-1");

        // 创建第二个按钮
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("按钮二名称");
        button2.setUrl("https://your-url-for-button-two.com");

        rowInline.add(button1);
        rowInline.add(button2);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);

    }


}
