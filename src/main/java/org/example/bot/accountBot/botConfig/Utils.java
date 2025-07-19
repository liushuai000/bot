package org.example.bot.accountBot.botConfig;

import io.swagger.models.auth.In;
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
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;
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
    public static boolean checkFormula(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        formula = formula.trim();
        if (formula.startsWith("+-")||formula.startsWith("-+")){
            return false;
        }
        // 先去掉正负号
        if (formula.startsWith("+") || formula.startsWith("-")) {
            formula = formula.substring(1);
        }
        // 统计四种运算符的出现次数
        int plusCount = countOccurrences(formula, "+");
        int minusCount = countOccurrences(formula, "-");
        int mulCount = countOccurrences(formula, "*");
        int divCount = countOccurrences(formula, "/");
        // 判断是否有某个运算符出现两次以上
        if (plusCount > 1 || minusCount > 1 || mulCount > 1 || divCount > 1) {
            return false;
        }
        return true;
    }
    private static int countOccurrences(String str, String target) {
        return str.length() - str.replace(target, "").length();
    }
    //入账操作2.0，匹配公式
    public  boolean calcRecorded(String text1, String messageUserId, String userName, String groupId, Account updateAccount, BigDecimal total, BigDecimal down, Issue issue,
                                 BigDecimal downed, BigDecimal downing, Status status, Integer messageId) {
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
            rate.setMatcher(true);
            return calcAdd(messageUserId,userName, updateAccount, total, down, downing, rate, t, r, e,downed,status,messageId);
        } else if (matchFound&&text1.charAt(0)=='-'){
            // 解析数字
            rate.setRate(r);
            rate.setExchange(e);//不应该是updateRate insert
            rate.setMatcher(true);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status,messageId);
        }else if (matchFound1&&text1.charAt(0)=='+'){
            rate.setMatcher(true);
            return calcAdd(messageUserId,userName, updateAccount, total, down, downing, rate, t, r, e,downed,status,messageId);
        }else if (matchFound1&&text1.charAt(0)=='-'){
            rate.setRate(r);
            rate.setExchange(e);
            rate.setMatcher(true);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status,messageId);
        }else if (matchFound2&&text1.charAt(0)=='+'){
            rate.setMatcher(true);
            rate.setRate(r);//如果setExchange 默认为null  应该设置为0
            return calcAdd2(messageUserId,userName, updateAccount,  down, downing, rate, t,downed,status,messageId);
        }else if (matchFound2&&text1.charAt(0)=='-'){
            rate.setRate(r);
            rate.setMatcher(true);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status,messageId);
        } else if (matchFound3&&text1.charAt(0)=='+') {
            rate.setExchange(e);
            rate.setMatcher(true);
            return calcAdd2(messageUserId,userName, updateAccount, down, downing, rate, t,downed,status,messageId);
        }else if (matchFound3&&text1.charAt(0)=='-'){
            rate.setExchange(e);
            rate.setMatcher(true);
            return calcSubtraction(rate,messageUserId,userName, down, issue, downed, t,status,messageId);
        }
        return false;
    }
    //加法  用于matchFound 0 1
    private  boolean calcAdd(String messageUserId,String userName, Account updateAccount, BigDecimal total, BigDecimal down, BigDecimal downing,
                             Rate rate, BigDecimal t, BigDecimal r, BigDecimal e,BigDecimal downed,Status status,Integer messageId) {
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
        updateAccount.setMessageId(messageId);
        updateAccount.setPm(false);
        updateAccount.setRiqie(status.isRiqie());
        updateAccount.setAccountHandlerMoney(status.getAccountHandlerMoney());
        log.info("应下发:{},总入账:{},account:{}", downing, total, updateAccount);
        accountService.insertAccount(updateAccount);
        issueService.updateIssueDown(down.add(t),updateAccount.getGroupId());
        return true;
    }
    //加法2 用于matchFound 2 3
    private  boolean calcAdd2(String messageUserId,String userName, Account updateAccount, BigDecimal down, BigDecimal downing,
                              Rate rate, BigDecimal t,BigDecimal downed,Status status,Integer messageId) {
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
        updateAccount.setPm(false);
        updateAccount.setMessageId(messageId);
        updateAccount.setRiqie(status.isRiqie());
        updateAccount.setAccountHandlerMoney(status.getAccountHandlerMoney());
        accountService.insertAccount(updateAccount);
        //应该是新增加一条 已出帐记录吧!issueService.insert();
        issueService.updateIssueDown(down.add(t),updateAccount.getGroupId());
        return true;
    }
    //减法
    private  boolean calcSubtraction(Rate rate,String messageUserId,String userName,BigDecimal down, Issue issue, BigDecimal downed,
                                     BigDecimal t,Status status,Integer messageId) {
        rateService.insertRate(rate);
        issue.setRateId(rate.getId());
        issue.setUserId(messageUserId);
        //应该减去未下发-issue里的已下发
        downed=dowingAccount(t,rate,downed);
        issue.setDown(down.subtract(downed));
        issue.setDowned(downed);
        issue.setMessageId(messageId);
        issue.setPm(false);
        issue.setRiqie(status.isRiqie());
        issue.setIssueHandlerMoney(status.getIssueHandlerMoney());
//        issue.setSetTime(status.getSetTime());
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
        BigDecimal exchange = rate.getExchange();//汇率
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
        return total;
    }

    //计算器功能
    public void counter(String text, SendMessage sendMessage) {
        try {
            String calculate = calculate(text);
            if (StringUtils.isEmpty(calculate)) return;
            accountBot.sendMessage(sendMessage,calculate);
        }catch (Exception e){
            log.info("计算器功能异常");//加减乘除开头的排除 +100/5这样的
        }
    }

    //计算器的判断是否符合+-*/
//    public  String calculate(String text) {
//        try {
//            BigDecimal result = evaluateExpression(text);
//            System.out.println("Result: " + result);
//            if (result!=null){
//                String formattedResult = result.stripTrailingZeros().toPlainString();
//                return text + "=" + formattedResult;
//            }
//        } catch (Exception e) {
//            System.out.println("Error evaluating expression: " + e.getMessage());
//        }
//        return null;
//    }
    public String calculate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        if (!isValidExpression(text)) {
            return "";
        }
        // 排除以加减乘除开头的情况，如 "+100/5" 或 "-200*3"
        if (text.trim().startsWith("+") ||
                text.trim().startsWith("-") ||
                text.trim().startsWith("*") ||
                text.trim().startsWith("/")) {
            return "";
        }
        // 判断是否包含数学运算符
        boolean containsOperator = text.matches(".*[+\\-*/()].*");
        if (!containsOperator) {
            return ""; // 没有运算符，直接返回空字符串
        }
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        try {
            Object result = engine.eval(text);
            BigDecimal value = new BigDecimal(result.toString());
            // 四舍五入保留两位小数，并去除无效的 0
            String formattedValue = value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            return text + "=" + formattedValue;
        } catch (ScriptException | NumberFormatException e) {
            return "";
        }
    }

    public static BigDecimal evaluateExpression(String expression) {
        // 移除空格
        expression = expression.replaceAll("\\s+", "");

        // 检查表达式是否符合要求
        if (isValidExpression(expression)) {
            try {
                // 将中缀表达式转换为后缀表达式
                Deque<String> postfix = infixToPostfix(expression);

                // 计算后缀表达式
                Stack<BigDecimal> stack = new Stack<>();
                while (!postfix.isEmpty()) {
                    String token = postfix.pollFirst();
                    if (isNumber(token)) {
                        stack.push(new BigDecimal(token));
                    } else {
                        BigDecimal b = stack.pop();
                        BigDecimal a = stack.pop();
                        switch (token) {
                            case "+":
                                stack.push(a.add(b));
                                break;
                            case "-":
                                stack.push(a.subtract(b));
                                break;
                            case "*":
                                stack.push(a.multiply(b));
                                break;
                            case "/":
                                stack.push(a.divide(b, 2, RoundingMode.HALF_UP));
                                break;
                            default:
                                throw new IllegalArgumentException("未知的运算符: " + token);
                        }
                    }
                }
                // 四舍五入
                BigDecimal result = stack.pop().setScale(2, RoundingMode.HALF_UP);
                return result;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean isValidExpression(String expression) {
        return expression.matches("^\\s*" +  "[+-]?(?:\\d+\\.?\\d*|\\d*\\.\\d+)" + "(?:\\s*[+\\-*/]\\s*" +
                        "[+-]?(?:\\d+\\.?\\d*|\\d*\\.\\d+))+" +"\\s*$");
    }

    private static boolean isNumber(String token) {
        try {
            new BigDecimal(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Deque<String> infixToPostfix(String expression) {
        Deque<String> output = new LinkedList<>();
        Deque<Character> operators = new LinkedList<>();

        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder number = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    number.append(expression.charAt(i++));
                }
                output.offerLast(number.toString());
            } else if (c == '(') {
                operators.offerFirst(c);
                i++;
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peekFirst() != '(') {
                    output.offerLast(operators.pollFirst().toString());
                }
                operators.pollFirst(); // 移除 '('
                i++;
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && precedence(operators.peekFirst()) >= precedence(c)) {
                    output.offerLast(operators.pollFirst().toString());
                }
                operators.offerFirst(c);
                i++;
            } else {
                i++;
            }
        }

        while (!operators.isEmpty()) {
            output.offerLast(operators.pollFirst().toString());
        }

        return output;
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }
}
