package com.clock.bot.botConfig;


import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ButtonList {
    protected String url="http://www.yaoke.cc/";//43.128.113.117 http://www.yaoke.cc/ 本地需要换端口

//    protected String url="http://192.168.0.2:8080/";
    //map key:buttonText value:callbackData
    public void sendButton(SendMessage sendMessage, String groupId, Map<String,String> buttonText) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Map.Entry<String, String> entry : buttonText.entrySet()) {
            String buttonTextName = entry.getKey();
            String callbackData = entry.getValue();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonTextName);
            button.setCallbackData(callbackData);
            rowInline.add(button);
        }
        rowsInline.add(rowInline);
        sendMessage.setChatId(groupId);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }

    //实现list按钮
    public void implList(SendMessage sendMessage,String groupId,String groupTitle) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("网页账单");
        String encodedGroupTitle;
        try {
            encodedGroupTitle = URLEncoder.encode(groupTitle, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        button1.setUrl(url+"Account?groupId="+groupId+"&groupTitle="+encodedGroupTitle);

        rowInline.add(button1);
//        rowInline.add(button2);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }



}
