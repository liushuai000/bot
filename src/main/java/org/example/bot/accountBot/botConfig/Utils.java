package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.service.IssueService;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
    private UserService userService;
    @Autowired
    AccountBot accountBot;
    //匹配+1000/7*0.05
    public static final Pattern pattern = Pattern.compile("([+-]?\\d+)[/]([+-]?\\d+)[/\\*]([+-]?(\\d*\\.\\d+|\\d+))");
    //匹配+1000*0.05/7
    public static final Pattern pattern1 = Pattern.compile("([+-]?\\d+(\\.\\d+)?)\\*(\\d+(\\.\\d+)?)/(\\d+)");
    //匹配+1000*0.05
    public static final Pattern pattern2 = Pattern.compile("([+-]?\\d+(\\.\\d+)?)\\*(\\d+(\\.\\d+)?)");
    //匹配+1000/7
    public static final Pattern pattern3 = Pattern.compile("([+-]?\\d+)/([+-]?\\d+)");
    public static void main(String[] args) {
//        String input = "+1000*5/7"; // 示例输入
//        // 正则表达式
//        String regex = "([+-]?\\d+)[/]([+-]?\\d+)[/\\*]([+-]?(\\d*\\.\\d+|\\d+))";
//        Pattern pattern = Pattern.compile(regex);
//        Matcher matcher = pattern.matcher(input);
//        while (matcher.find()) {
//            // 输出提取的数字
//            System.out.println("第一位 (整数): " + matcher.group(1).replaceAll("[+]", "")); // 去掉符号，获取的数：1000
//            System.out.println("第二位 (整数): " + matcher.group(2)); // 获取的数：7
//            System.out.println("第三位 (小数或整数): " + new BigDecimal(matcher.group(3))); // 获取的数：0.05或5
//        }

        Date date = new Date();
        Date from = Date.from(LocalDateTime.now().plusDays(20).atZone(ZoneId.systemDefault()).toInstant());
        System.err.println(from.compareTo(date));//等于true
    }
    //入账操作2.0，匹配公式
    public  boolean calcRecorded(String text1, String messageUserId, String userName, String groupId, Account updateAccount, BigDecimal total, BigDecimal down, Issue issue,
                                 BigDecimal downed, BigDecimal downing, Status status) {
        Rate rate = new Rate();
        rate.setGroupId(groupId);
        rateService.setInitRate(rate);
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
        BigDecimal e = new BigDecimal(1);//汇率 exchange  7
        log.info("matchFound:{},matchFound1:{},matchFound2:{}",matchFound,matchFound1,matchFound2);
        //获取匹配到的数字    0:   : +1000/7*0.05   1 +1000*0.05/7  2:+1000*0.05   3: +1000/7
        if (matchFound){
            t = BigDecimal.valueOf(Long.parseLong(matcher.group(1).replaceAll("[+-]", "")));
            e = new BigDecimal(matcher.group(2));
            r = new BigDecimal(matcher.group(3));
        }else if (matchFound1){
            t = BigDecimal.valueOf(Long.parseLong(matcher1.group(1).replaceAll("[+-]", "")));
            r = new BigDecimal(matcher1.group(3));//0.05
            e = new BigDecimal(matcher1.group(5));//7
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
            return calcAdd(messageUserId,userName, updateAccount, total, down, downing, rate, t, r, e,downed,status);
        } else if (matchFound&&text1.charAt(0)=='-'){
            // 解析数字
            rate.setRate(r);
            rate.setExchange(e);//不应该是updateRate insert
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status);
        }else if (matchFound1&&text1.charAt(0)=='+'){
            return calcAdd(messageUserId,userName, updateAccount, total, down, downing, rate, t, r, e,downed,status);
        }else if (matchFound1&&text1.charAt(0)=='-'){
            rate.setRate(r);
            rate.setExchange(e);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status);
        }else if (matchFound2&&text1.charAt(0)=='+'){
            rate.setRate(r);//如果setExchange 默认为null  应该设置为0
            return calcAdd2(messageUserId,userName, updateAccount,  down, downing, rate, t,downed,status);
        }else if (matchFound2&&text1.charAt(0)=='-'){
            rate.setRate(r);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status);
        } else if (matchFound3&&text1.charAt(0)=='+') {
            rate.setExchange(e);
            return calcAdd2(messageUserId,userName, updateAccount, down, downing, rate, t,downed,status);
        }else if (matchFound3&&text1.charAt(0)=='-'){
            rate.setExchange(e);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status);
        }
        return false;
    }
    //加法  用于matchFound 0 1
    private  boolean calcAdd(String messageUserId,String userName, Account updateAccount, BigDecimal total, BigDecimal down, BigDecimal downing,
                             Rate rate, BigDecimal t, BigDecimal r, BigDecimal e,BigDecimal downed,Status status) {
        rate.setRate(r);
        rate.setExchange(e);
        rateService.insertRate(rate);
        downing =dowingAccount(t,rate,downing) ;
//        total = total.add(t);
        updateAccount.setRateId(rate.getId());
        updateAccount.setTotal(t);
        updateAccount.setUserId(messageUserId);
        updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
        updateAccount.setDown(downing.subtract(downed));
        updateAccount.setRiqie(status.isRiqie());
        log.info("应下发:{},总入账:{},account:{}", downing, total, updateAccount);
        accountService.insertAccount(updateAccount);
        issueService.updateIssueDown(down.add(t),updateAccount.getGroupId());
        return true;
    }
    //加法2 用于matchFound 2 3
    private  boolean calcAdd2(String messageUserId,String userName, Account updateAccount, BigDecimal down, BigDecimal downing,
                              Rate rate, BigDecimal t,BigDecimal downed,Status status) {
        rateService.insertRate(rate);
        downing =dowingAccount(t,rate,downing) ;
//        total = total.add(t);
        updateAccount.setTotal(t);
        updateAccount.setUserId(messageUserId);//操作人id
        //计算应下发
        downing=dowingAccount(t,rate,downing);
        updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
        updateAccount.setDown(downing.subtract(downed));
        updateAccount.setRateId(rate.getId());
        updateAccount.setRiqie(status.isRiqie());
        accountService.insertAccount(updateAccount);
        //应该是新增加一条 已出帐记录吧!issueService.insert();
        issueService.updateIssueDown(down.add(t),updateAccount.getGroupId());
        return true;
    }
    //减法
    private  boolean calcSubtraction(Rate rate,String messageUserId,String userName,BigDecimal down, Issue issue, BigDecimal downed,
                                     BigDecimal t,Status status) {
        rateService.insertRate(rate);
        issue.setRateId(rate.getId());
        issue.setUserId(messageUserId);
        //应该减去未下发-issue里的已下发
        downed=dowingAccount(t,rate,downed);
        issue.setDown(down.subtract(downed));
        issue.setDowned(downed);
        issue.setRiqie(status.isRiqie());
        User byUserId = userService.findByUserId(messageUserId);
        if (byUserId!=null){
            issueService.insertIssue(issue);
            accountService.updateDown(down.subtract(t),rate.getGroupId());
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
        BigDecimal totalTimesRate1=new BigDecimal(0);
        if (!rate.isCalcU()){//只有在+-30U u结尾的这种情况下不计算费率
            totalTimesRate1 = total.multiply(rate1);
        }
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
        try {
            String result = evaluateExpression(text);
            System.out.println("Result: " + result);
            if (StringUtils.isNotBlank(result))  return text+"="+result;
        } catch (Exception e) {
            System.out.println("Error evaluating expression: " + e.getMessage());
        }
        return null;
    }
    public static String evaluateExpression(String expression) {
        // 移除空格
        expression = expression.replaceAll("\\s+", "");

        // 检查表达式是否符合要求
        if (isValidExpression(expression)) {
            try {
                // 创建ScriptEngine
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("JavaScript");
                return engine.eval(expression)+"";
            } catch (ScriptException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    // 检查表达式是否只有两个数值和一个运算符
    private static boolean isValidExpression(String expression) {
        String[] tokens = expression.split("(?<=[-+*/])|(?=[-+*/])"); // 按运算符分割

        // 检查是否只有三个token：一个数值、一个运算符、另一个数值
        return tokens.length == 3 &&
                isNumber(tokens[0]) &&
                isOperator(tokens[1]) &&
                isNumber(tokens[2]);
    }

    // 检查是否为数字
    private static boolean isNumber(String token) {
        return token.matches("-?\\d+(\\.\\d+)?"); // 匹配整数或小数
    }

    // 检查是否为运算符
    private static boolean isOperator(String token) {
        return "+-*/".contains(token);
    }
}
