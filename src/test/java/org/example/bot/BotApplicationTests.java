package org.example.bot;

import org.apache.commons.lang3.time.DateUtils;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class BotApplicationTests {
    @Autowired
    protected UserService userService;
    @Test
    void contextLoads() {
        //    public static void main(String[] args) {
//        String input = "+1000*0.05"; // 示例输入
//        if (Pattern.matches(regex1, input)) {
//            System.out.println("匹配情况 1");
//        } else if (Pattern.matches(regex2, input)) {
//            System.out.println("匹配情况 2");
//        } else if (Pattern.matches(regex3, input)) {
//            System.out.println("匹配情况 3");
//        } else if (Pattern.matches(regex4, input)) {
//            System.out.println("匹配情况 4");
//        } else {
//            System.out.println("没有匹配的情况");
//        }
//    }
        Date date = new Date();
        Date date1 = DateUtils.addDays(date, 1);
        System.err.println(date.compareTo(date1));
    }

}
