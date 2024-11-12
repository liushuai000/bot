package org.example.bot.accountBot;

import org.example.bot.accountBot.config.RestTemplateConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ComponentScan(basePackages = {"org.example.bot.accountBot.service"})
public class Main {

    @Value("${getExchangeUrl}")
    private static String url;

    public static void main(String[] args) {
        // 创建 Spring 应用上下文
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);

        // 从上下文中获取 RestTemplateConfig 实例
        RestTemplateConfig restTemplateConfig = context.getBean(RestTemplateConfig.class);

        // 确保 url 不为 null
        if (url == null) {
            throw new IllegalStateException("URL is not configured in the properties file.");
        }

        // 使用 RestTemplate 发送请求
        RestTemplate restTemplate = restTemplateConfig.restTemplate();
        ResponseEntity<?> forEntity = restTemplate.getForEntity(url + "?coinId=2&currency=172&tradeType=sell&currPage=1&payMethod=0&acceptOrder=0&country=" +
                "&blockType=general&online=1&range=0&amount=&isThumbsUp=false&isMerchant=false" +
                "&isTraded=false&onlyTradable=false&isFollowed=false&makerCompleteRate=0", Object.class);

        Object body = forEntity.getBody();
        System.err.println(body);
    }
}
