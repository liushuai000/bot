package org.example.bot.accountBot.botConfig;

public interface BotButtonInfo {
//    String getType();//用来区分内置按钮用

    String getText();

    String getLink();

    Integer getRowIndex();

    Integer getButtonIndex();

}
