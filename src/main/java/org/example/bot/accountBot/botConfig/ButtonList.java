package org.example.bot.accountBot.botConfig;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
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
    //protected String url="http://www.yaoke.cc/";//43.128.113.117 http://www.yaoke.cc/ 本地需要换端口

    protected String url="http://192.168.0.2:8080/";
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


    public void exchangeList(SendMessage sendMessage,String groupId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("所有✅");
        button1.setCallbackData("所有");
//        button1.setUrl(url+"Account?groupId="+groupId); ✅

        // 创建第二个按钮
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("银行卡");
        button2.setCallbackData("银行卡");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("支付宝");
        button3.setCallbackData("支付宝");

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("微信");
        button4.setCallbackData("微信");

        rowInline.add(button1);
        rowInline.add(button2);
        rowInline.add(button3);
        rowInline.add(button4);

        rowsInline.add(rowInline);
        sendMessage.setChatId(groupId);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }
    public void editText(EditMessageText editMessage,String groupId,String payMethod) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("所有");
        button1.setCallbackData("所有");
//        button1.setUrl(url+"Account?groupId="+groupId);
        // 创建第二个按钮
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        String icon="✅";
        button2.setText("银行卡");
        button2.setCallbackData("银行卡");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("支付宝");
        button3.setCallbackData("支付宝");

        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setText("微信");
        button4.setCallbackData("微信");


        if (payMethod.equals("0")) {
            button1.setText(button1.getText()+icon);
        } else if (payMethod.equals("1")) {
            button2.setText(button2.getText()+icon);
        } else if (payMethod.equals("3")) {
            button4.setText(button4.getText()+icon);
        } else if (payMethod.equals("2")) {
            button3.setText(button3.getText()+icon);
        }
        rowInline.add(button1);
        rowInline.add(button2);
        rowInline.add(button3);
        rowInline.add(button4);

        rowsInline.add(rowInline);
        editMessage.setChatId(groupId);
        markup.setKeyboard(rowsInline);
        editMessage.setReplyMarkup(markup);
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
