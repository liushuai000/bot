package org.example.bot.accountBot.botConfig;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.pojo.GroupInfoSetting;
import org.example.bot.accountBot.utils.TranslationExample;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
public class ButtonList {
    @Value("${vueUrl}")
    protected String url;//43.128.113.117 http://www.yaoke.cc/ 本地需要换端口

//    protected String url="http://192.168.0.2:8080/";
    //map key:buttonText value:callbackData
    public void sendButton(SendMessage sendMessage, String groupId, Map<String,String> buttonText,GroupInfoSetting groupInfoSetting) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (Map.Entry<String, String> entry : buttonText.entrySet()) {
            String buttonTextName = entry.getKey();
            String callbackData = entry.getValue();
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (groupInfoSetting.getEnglish()){
                button.setText(buttonTextName);
            }else {
                button.setText(TranslationExample.translateText(buttonTextName,groupInfoSetting.getEnglish()));
            }
            button.setCallbackData(callbackData);
            rowInline.add(button);
        }
        rowsInline.add(rowInline);
        sendMessage.setChatId(groupId);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }


    public void exchangeList(SendMessage sendMessage, String groupId, GroupInfoSetting groupInfoSetting) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setCallbackData("所有");
//        button1.setUrl(url+"Account?groupId="+groupId); ✅
        // 创建第二个按钮
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setCallbackData("银行卡");
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setCallbackData("支付宝");
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setCallbackData("微信");
        if (groupInfoSetting.getEnglish()){
            button1.setText("所有✅");
            button2.setText("银行卡");
            button3.setText("支付宝");
            button4.setText("微信");
        }else {
            button1.setText("All✅");
            button2.setText("Bank");
            button3.setText("Alipay");
            button4.setText("WeChat");
        }
        rowInline.add(button1);
        rowInline.add(button2);
        rowInline.add(button3);
        rowInline.add(button4);

        rowsInline.add(rowInline);
        sendMessage.setChatId(groupId);
        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }
    public void editText(EditMessageText editMessage,String groupId,String payMethod,GroupInfoSetting groupInfoSetting) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setCallbackData("所有");
//        button1.setUrl(url+"Account?groupId="+groupId);
        // 创建第二个按钮
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        String icon="✅";
        button2.setCallbackData("银行卡");
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setCallbackData("支付宝");
        InlineKeyboardButton button4 = new InlineKeyboardButton();
        button4.setCallbackData("微信");
        if (groupInfoSetting.getEnglish()){
            button1.setText("所有");
            button2.setText("银行卡");
            button3.setText("支付宝");
            button4.setText("微信");
        }else {
            button1.setText("All");
            button2.setText("Bank");
            button3.setText("Alipay");
            button4.setText("WeChat");
        }
        if (payMethod.equals("所有")) {
            button1.setText(button1.getText()+icon);
        } else if (payMethod.equals("银行卡")) {
            button2.setText(button2.getText()+icon);
        } else if (payMethod.equals("支付宝")) {
            button3.setText(button3.getText()+icon);
        } else if (payMethod.equals("微信")) {
            button4.setText(button4.getText()+icon);
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
    public void implList(SendMessage sendMessage,String groupId,String groupTitle,GroupInfoSetting groupInfoSetting) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        if (groupInfoSetting.getEnglish()){
            button1.setText("网页账单");
        }else {
            button1.setText("Web Bill");
        }
        String encodedGroupTitle;
        try {
            encodedGroupTitle = URLEncoder.encode(groupTitle, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        button1.setUrl(url+"Account?groupId="+groupId+"&groupTitle="+encodedGroupTitle);

        // 创建第二个按钮
//        InlineKeyboardButton button2 = new InlineKeyboardButton();
//        button2.setText("按钮二名称");
//        button2.setUrl("https://your-url-for-button-two.com");

        rowInline.add(button1);
//        rowInline.add(button2);
        rowsInline.add(rowInline);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);
    }


}
