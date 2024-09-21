package org.example.bot.accountBot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j

public class accController {

    public void hand(String text, Long chatId) {
        SendMessage response = new SendMessage();
        response.setChatId(String.valueOf(chatId));
        response.setText(text);
        log.warn("输入："+text);

    }

}
