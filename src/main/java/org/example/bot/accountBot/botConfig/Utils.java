package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.pojo.Issue;
import org.example.bot.accountBot.pojo.Rate;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 入账操作2.0，匹配公式
 * 加法  用于matchFound 0 1
 * 加法2 用于matchFound 2 3
 * 减法
 * 是否匹配公式入账
 * 应下发计算公式：d=(total-(total*rate1))/exchange
 * 计算器的判断是否符合
 *
 **/
@Slf4j
@Service
public class Utils{
    @Autowired
    private AccountService accountService; //匹配+1000*0.05/7
    @Autowired
    private RateService rateService;
    @Autowired
    private IssueService issueService;
    @Autowired
    AccountBot accountBot;
    //匹配+1000/7*0.05
    public static final Pattern pattern = Pattern.compile("([+-]?\\d+)[/\\*]([+-]?\\d+)[/\\*]([+-]?(\\d*\\.\\d+|\\d+))");
    //匹配+1000*0.05/7
    public static final Pattern pattern1 = Pattern.compile("([+-]?\\d+(\\.\\d+)?)\\*(\\d+(\\.\\d+)?)/(\\d+)");
    //匹配+1000*0.05
    public static final Pattern pattern2 = Pattern.compile("([+-]?\\d+(\\.\\d+)?)\\*(\\d+(\\.\\d+)?)");
    //匹配+1000/7
    public static final Pattern pattern3 = Pattern.compile("([+-]?\\d+)/([+-]?\\d+)");

    public static void main(String[] args) {
        String input = "-1000/7*0.05"; // 示例输入

        // 正则表达式
        String regex = "([+-]?\\d+)[/\\*]([+-]?\\d+)[/\\*]([+-]?(\\d*\\.\\d+|\\d+))";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            // 输出提取的数字
            System.out.println("第一位 (整数): " + matcher.group(1).replaceAll("[+]", "")); // 去掉符号，获取的数：1000
            System.out.println("第二位 (整数): " + matcher.group(2)); // 获取的数：7
            System.out.println("第三位 (小数或整数): " + new BigDecimal(matcher.group(3))); // 获取的数：0.05或5
        }
    }
    //入账操作2.0，匹配公式
    public  boolean calcRecorded(String text1, String userName, Account updateAccount, BigDecimal total, BigDecimal down, Issue issue,
                                 BigDecimal downed, BigDecimal downing,Rate rate) {
        String text = text1.substring(1);
        if (text1.charAt(0)!='+'&&text1.charAt(0)!='-'){
            return false;
        }
//        Rate rate=rateService.selectRate();
        Matcher matcher = pattern.matcher(text);//+1000/7*0.05
        Matcher matcher1 = pattern1.matcher(text);//+1000*0.05/7  *的是费率
        Matcher matcher2 = pattern2.matcher(text);//+1000*0.05
        Matcher matcher3 = pattern3.matcher(text);//+1000/7
        boolean matchFound = matcher.find();
        boolean matchFound1 = matcher1.find();
        boolean matchFound2 = matcher2.find();
        boolean matchFound3 = matcher3.find();
        BigDecimal t = new BigDecimal(0);//金额 tatol     1000
        BigDecimal r = new BigDecimal(0);//费率 rate      0.05
        BigDecimal e = new BigDecimal(0);//汇率 exchange  7
        log.info("matchFound:{},matchFound1:{},matchFound2:{}",matchFound,matchFound1,matchFound2);
        //获取匹配到的数字    0:   : +1000/7*0.05   1 +1000*0.05/7  2: +1000/7   3: +1000*0.05
        if (matchFound){
            t = BigDecimal.valueOf(Long.parseLong(matcher.group(1).replaceAll("[+-]", "")));
            e = new BigDecimal(matcher.group(2));
            r = new BigDecimal(matcher.group(3));
        }else if (matchFound1){
            t = BigDecimal.valueOf(Long.parseLong(matcher1.group(1).replaceAll("[+-]", "")));
            r = new BigDecimal(matcher1.group(3));
            e = new BigDecimal(matcher1.group(5));
        }else if (matchFound2){
            t = BigDecimal.valueOf(Long.parseLong(matcher2.group(1).replaceAll("[+-]", "")));
            r = new BigDecimal(matcher2.group(3));
        }else if (matchFound3){
            t = BigDecimal.valueOf(Long.parseLong(matcher3.group(1)));
            e = new BigDecimal(matcher3.group(2));
        }
        log.info(".....t:{},r:{},e:{}", t, r, e);
        //按照公式进行计算
        if (matchFound&&text1.charAt(0)=='+') {
            return calcAdd(userName, updateAccount, total, down, downing, rate, t, r, e,downed);
        } else if (matchFound&&text1.charAt(0)=='-'){
            // 解析数字
            rate.setRate(r);
            rate.setExchange(e);
            rateService.updateRate(String.valueOf(r));
            rateService.updateExchange(e);
            return calcSubtraction(userName, down, issue, downed, t);
        }else if (matchFound1&&text1.charAt(0)=='+'){
            return calcAdd(userName, updateAccount, total, down, downing, rate, t, r, e,downed);
        }else if (matchFound1&&text1.charAt(0)=='-'){
            rate.setRate(r);
            rate.setExchange(e);
            rateService.updateRate(String.valueOf(r));
            rateService.updateExchange(e);
            return calcSubtraction(userName, down, issue, downed, t);
        }else if (matchFound2&&text1.charAt(0)=='+'){
            rate.setRate(r);
            rateService.updateRate(String.valueOf(r));
            return calcAdd2(userName, updateAccount, total, down, downing, rate, t,downed);
        }else if (matchFound2&&text1.charAt(0)=='-'){
            rate.setRate(r);
            rateService.updateRate(String.valueOf(r));
            return calcSubtraction(userName, down, issue, downed, t);
        } else if (matchFound3&&text1.charAt(0)=='+') {
            rate.setExchange(e);
            rateService.updateExchange(e);
            return calcAdd2(userName, updateAccount, total, down, downing, rate, t,downed);
        }else if (matchFound3&&text1.charAt(0)=='-'){
            rate.setExchange(e);
            rateService.updateExchange(e);
            return calcSubtraction(userName, down, issue, downed, t);
        }
        return false;
    }
    //加法  用于matchFound 0 1
    private  boolean calcAdd(String userName, Account updateAccount, BigDecimal total, BigDecimal down, BigDecimal downing,
                             Rate rate, BigDecimal t, BigDecimal r, BigDecimal e,BigDecimal downed) {
        rate.setRate(r);
        rate.setExchange(e);
        //要不要加个id 然后根据id更新rate
        rateService.updateRate(String.valueOf(r));
        rateService.updateExchange(e);

        downing =dowingAccount(t,rate,downing) ;
//        total = total.add(t);
        updateAccount.setTotal(t);
        updateAccount.setHandle(userName);
        updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
        updateAccount.setDown(downing.subtract(downed));
        log.info("应下发:{},总入账:{},account:{}", downing, total, updateAccount);
        accountService.insertAccount(updateAccount);
        issueService.updateIssueDown(down.add(t));
        return true;
    }
    //加法2 用于matchFound 2 3
    private  boolean calcAdd2(String userName, Account updateAccount, BigDecimal total, BigDecimal down, BigDecimal downing,
                              Rate rate, BigDecimal t,BigDecimal downed) {
        downing =dowingAccount(t,rate,downing) ;
//        total = total.add(t);
        updateAccount.setTotal(t);
        updateAccount.setHandle(userName);//操作人
        //计算应下发
        downing=dowingAccount(total,rate,downing);
        updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
        updateAccount.setDown(downing.subtract(downed));
        accountService.insertAccount(updateAccount);
        issueService.updateIssueDown(down.add(t));
        return true;
    }
    //减法
    private  boolean calcSubtraction(String userName, BigDecimal down, Issue issue, BigDecimal downed, BigDecimal t) {
        issue.setHandle(userName);
        issue.setDown(down.subtract(t));
        issue.setDowned(downed.add(t));
        if (issue.getHandle()!=null){
            issueService.insertIssue(issue);
            accountService.updateDown(down.subtract(t));
            log.info("执行了1================");
        }
        return true;
    }

    //是否匹配公式入账
    public  boolean isMatcher(String text) {
        Matcher matcher = pattern.matcher(text);
        Matcher matcher1 = pattern1.matcher(text);
        Matcher matcher2 = pattern2.matcher(text);
        Matcher matcher3 = pattern3.matcher(text);
        boolean matchFound = matcher.find();
        boolean matchFound1 = matcher1.find();
        boolean matchFound2 = matcher2.find();
        boolean matchFound3 = matcher3.find();
        return matchFound || matchFound1 || matchFound2 || matchFound3;
    }
    //应下发计算公式：d=(total-(total*rate1))/exchange  用加已下发吗downed  因为未下发减去已下发=应下发
    public  BigDecimal dowingAccount(BigDecimal total, Rate rate, BigDecimal downing) {
        BigDecimal rate1 = rate.getRate();//费率
        rate1=rate1.multiply(BigDecimal.valueOf(0.01));
        BigDecimal exchange = rate.getExchange();//汇率 不知道用不用
        BigDecimal totalTimesRate1 = total.multiply(rate1);
        // 计算 total - (total * rate1)
        BigDecimal totalMinusTotalTimesRate1 = total.subtract(totalTimesRate1);
        // 计算最终结果 d = (total - (total * rate1)) / exchange
        BigDecimal d = totalMinusTotalTimesRate1.divide(exchange, 2, RoundingMode.HALF_UP);
//        BigDecimal d = totalMinusTotalTimesRate1.setScale(2, RoundingMode.HALF_UP); // 保留两位小数
//        log.info("计算最终结果d:{},总入帐*费率: {},应下发历史金额:{} ", d,totalTimesRate1,downing);
        System.err.println(totalMinusTotalTimesRate1);
        return totalMinusTotalTimesRate1;
    }

    //计算器功能
    public void counter(Message message, SendMessage sendMessage) {
        try {
            String calculate = calculate(message.getText());
            if (StringUtils.isEmpty(calculate)) return;
            accountBot.sendMessage(sendMessage,calculate);
        }catch (Exception e){
            log.info("计算器功能异常");
        }
    }
    //计算器的判断是否符合+-*/
    public  String calculate(String text) {
        // 正则表达式匹配形如 "数字 运算符 数字" 的模式
        Pattern pattern = Pattern.compile("^(\\d+)([+\\-*/])(\\d+)$");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            // 解析数字
            int number1 = Integer.parseInt(matcher.group(1));
            int number2 = Integer.parseInt(matcher.group(3));
            char operator = matcher.group(2).charAt(0);
            // 计算结果
            int result = 0;
            switch (operator) {
                case '+':
                    result = number1 + number2;
                    break;
                case '-':
                    result = number1 - number2;
                    break;
                case '*':
                    result = number1 * number2;
                    break;
                case '/':
                    if (number2 != 0) {
                        result = number1 / number2;
                    } else {
                        return "Error: Division by zero";
                    }
                    break;
                default:
                    return "Invalid operator";
            }

            // 返回结果字符串
            return text + "=" + result;
        } else {
            // 如果不匹配，返回 null
            return null;
        }
    }


}
