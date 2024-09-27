//package org.example.bot.accountBot;
//
//import org.example.bot.accountBot.botConfig.AccountBot;
//import org.springframework.context.annotation.ComponentScan;
//import org.telegram.telegrambots.bots.DefaultBotOptions;
//import org.telegram.telegrambots.meta.TelegramBotsApi;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
//@ComponentScan(basePackages = {"org.example.bot.accountBot.service"})
//public class Main {
//
//    public static void main(String[] args) {
//
//
//        String proxyHost = "127.0.0.2";
//
//        int proxyPort = 8080;
//
//        DefaultBotOptions botOptions = new DefaultBotOptions();
//        botOptions.setProxyHost(proxyHost);
//        botOptions.setProxyPort(proxyPort);
//        //注意一下这里，ProxyType是个枚举，看源码你就知道有NO_PROXY,HTTP,SOCKS4,SOCKS5;
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
//
//        DefaultBotSession defaultBotSession = new DefaultBotSession();
//        defaultBotSession.setOptions(botOptions);
//        try {
//            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());
//
//
//            //需要代理
////            ExecBot bot = new ExecBot(botOptions);
////            telegramBotsApi.registerBot(bot);
//            //不需代理
////            AccountBot bot2 = new AccountBot();
////            telegramBotsApi.registerBot(bot2);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
