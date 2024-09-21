package org.example.bot.startup;

import org.example.bot.accountBot.botConfig.accountBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class StartUp implements ApplicationRunner {

    @Autowired
    private org.example.bot.accountBot.service.accService accService;

    @Autowired
    private org.example.bot.accountBot.mapper.mapper mapper;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("StartUp" + accService);
        System.out.println("StartUp" + mapper);
        String proxyHost = "127.0.0.2";

        int proxyPort = 8080;

        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setProxyHost(proxyHost);
        botOptions.setProxyPort(proxyPort);
        //注意一下这里，ProxyType是个枚举，看源码你就知道有NO_PROXY,HTTP,SOCKS4,SOCKS5;
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        DefaultBotSession defaultBotSession = new DefaultBotSession();
        defaultBotSession.setOptions(botOptions);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(defaultBotSession.getClass());


            //需要代理
//            ExecBot bot = new ExecBot(botOptions);
//            telegramBotsApi.registerBot(bot);
            //不需代理
            accountBot bot2 = new accountBot();
            bot2.setService(accService);
            telegramBotsApi.registerBot(bot2);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
