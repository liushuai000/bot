package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.AccountMapper;
import org.example.bot.accountBot.mapper.IssueMapper;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.*;
import org.example.bot.accountBot.utils.BaseConstant;
import org.example.bot.accountBot.utils.ConstantMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * 入账操作  ||  入账时发送的消息   isMatcher: 是否匹配公式入账 例:+1000*0.05/7 这种公式
 */

@Slf4j
@Service
public class RuzhangOperations {
    @Autowired
    Utils utils;
    //操作时间
    @Autowired
    DateOperator dateOperator;
    @Resource
    AccountBot accountBot;
    @Autowired
    protected RateService rateService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected IssueService issueService;
    @Autowired
    protected StatusService statusService;
    @Autowired
    protected AccountService accountService;
    @Autowired
    protected AccountMapper accountMapper;
    @Autowired
    protected IssueMapper issueMapper;
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    private ShowOperatorName showOperatorName;
    @Autowired
    ButtonList buttonList;
    Map<String, String> constantMap = ConstantMap.COMMAND_MAP_ENGLISH;//关键词的对应关系

    //设置费/汇率
    protected void setRate(String text, SendMessage sendMessage, Rate rates) {
        if (text.length() < 4) {
            return;
        }
        if (text.startsWith("设置费率") || text.startsWith(constantMap.get("设置费率"))) {
            String rate = text.substring(4);
            BigDecimal bigDecimal = new BigDecimal(rate);
//            bigDecimal=bigDecimal.multiply(BigDecimal.valueOf(0.01));
            rates.setRate(bigDecimal);
            rates.setAddTime(new Date());
            log.info("rates:{}", rates);
            rateService.insertRate(rates);
            accountBot.sendMessage(sendMessage, "设置成功,当前费率为：" + rate);
        } else if (text.startsWith("设置汇率") || text.startsWith(constantMap.get("设置汇率"))) {
            if (text.substring(4).startsWith("-")) {
                rates.setAddTime(new Date());
                rates.setExchange(new BigDecimal(1));
                rateService.insertRate(rates);
                accountBot.sendMessage(sendMessage, "当前汇率已设置为默认汇率：1");
            } else {
                //如果设置汇率为0 就设置成1
                if (text.substring(4).equals("0")) {
                    rates.setExchange(new BigDecimal(1));
                } else {
                    rates.setExchange(new BigDecimal(text.substring(4)));
                }
                rates.setAddTime(new Date());
                rateService.insertRate(rates);
                accountBot.sendMessage(sendMessage, "设置成功,当前汇率为：" + text.substring(4));
            }
        } else if (text.toLowerCase().startsWith("set rate")) {
            String rate = text.substring("set rate".length()).trim();
            BigDecimal bigDecimal = new BigDecimal(rate);
//            bigDecimal=bigDecimal.multiply(BigDecimal.valueOf(0.01));
            rates.setRate(bigDecimal);
            rates.setAddTime(new Date());
            log.info("rates:{}", rates);
            rateService.insertRate(rates);
            accountBot.sendMessage(sendMessage, "The setting is successful, the current rate is：" + rate);
        } else if (text.toLowerCase().startsWith("set exchange rate")) {
            if (text.substring("set exchange rate".length()).trim().startsWith("-")) {
                rates.setAddTime(new Date());
                rates.setExchange(new BigDecimal(1));
                rateService.insertRate(rates);
                accountBot.sendMessage(sendMessage, "The current exchange rate is set as the default exchange rate：1");
            } else {
                //如果设置汇率为0 就设置成1
                if (text.substring("set exchange rate".length()).trim().equals("0")) {
                    rates.setExchange(new BigDecimal(1));
                } else {
                    rates.setExchange(new BigDecimal(text.substring("set exchange rate".length()).trim()));
                }
                rates.setAddTime(new Date());
                rateService.insertRate(rates);
                accountBot.sendMessage(sendMessage, "The setting is successful, the current exchange rate is：" + text.substring("set exchange rate".length()));
            }
        }
    }

    //撤销入款
    public void repeal( SendMessage sendMessage, List<Account> accounts, String replyToText, Integer replayToMessageId,
                       UserDTO userDTO, List<Issue> issueList,Status status) {
        String text = userDTO.getText();
        if (text.equals("取消") || text.equals(constantMap.get("取消")) && replyToText != null) {
            log.info("replyToXXXTentacion:{}", replyToText);
            if (replyToText.charAt(0) == '+') {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.charAt(0) == '-') {
                Issue issue = issueMapper.selectOne(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                issueMapper.delete(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                accountService.updateNewestData(issue.getDown(), userDTO.getGroupId());
            } else if (replyToText.startsWith("下发")) {
                Issue issue = issueMapper.selectOne(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                issueMapper.delete(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                accountService.updateNewestData(issue.getDown(), userDTO.getGroupId());
            } else if (replyToText.startsWith("入款")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("issue")) {
                Issue issue = issueMapper.selectOne(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                issueMapper.delete(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                accountService.updateNewestData(issue.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("t")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("d")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("p")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else {
                return;
            }
            accountBot.sendMessage(sendMessage, "取消成功");
        }
        if (text.equals("撤销入款") || text.equals(constantMap.get("撤销入款"))) {
            if (accounts.isEmpty()) {
                accountBot.sendMessage(sendMessage, "撤销未成功! 账单为空");
            } else {
                List<Account> sortedUserList = accounts.stream().sorted(Comparator.comparing(Account::getAddTime)) // 按时间倒序排序
                        .collect(Collectors.toList());
                Account account = sortedUserList.get(sortedUserList.size() - 1);
                if (account.getPm()){
                    if (account.getTotal().compareTo(BigDecimal.ZERO)>0){
                        status.setPmoney(status.getPmoney().subtract(account.getTotal()));
                    }else{
                        status.setPmoney(status.getPmoney().add(account.getTotal()));
                    }
                    statusService.update( status);
                }
                accountService.deleteInData(String.valueOf(account.getId()), userDTO.getGroupId());
                issueService.updateIssueDown(sortedUserList.get(sortedUserList.size() - 1).getDown(), userDTO.getGroupId());
                accountBot.sendMessage(sendMessage, "撤销成功");
            }
        } else if (text.equals("撤销下发") || text.equals(constantMap.get("撤销下发"))) {
            if (issueList.isEmpty()) {
                accountBot.sendMessage(sendMessage, "撤销未成功! 账单为空");
            } else {
                List<Issue> sortedUserList = issueList.stream().sorted(Comparator.comparing(Issue::getAddTime)) // 按时间倒序排序
                        .collect(Collectors.toList());
                Issue issue = sortedUserList.get(sortedUserList.size() - 1);
                if (issue.getPm()){//是手动添加的需要撤销回来 status里的手动添加
                    if (issue.getDowned().compareTo(BigDecimal.ZERO)>0){
                        status.setPmoney(status.getPmoney().add(issue.getDowned()));
                    }else{
                        status.setPmoney(status.getPmoney().subtract(issue.getDowned()));
                    }
                    statusService.update( status);
                }
                issueService.deleteNewestIssue(String.valueOf(issue.getId()), userDTO.getGroupId());
                accountService.updateNewestData(sortedUserList.get(sortedUserList.size() - 1).getDown(), userDTO.getGroupId());
                accountBot.sendMessage(sendMessage, "撤销成功");
            }
        }

    }

    public void repealEn(SendMessage sendMessage, List<Account> accounts, String replyToText, Integer replayToMessageId,
                         UserDTO userDTO, List<Issue> issueList,Status status) {
        String text = userDTO.getText().toLowerCase();
        if (text.equals("cancel") && replyToText != null) {
            log.info("replyToXXXTentacion:{}", replyToText);
            if (replyToText.charAt(0) == '+') {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.charAt(0) == '-') {
                Issue issue = issueMapper.selectOne(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                issueMapper.delete(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                accountService.updateNewestData(issue.getDown(), userDTO.getGroupId());
            } else if (replyToText.startsWith("withdraw")) {
                Issue issue = issueMapper.selectOne(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                issueMapper.delete(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                accountService.updateNewestData(issue.getDown(), userDTO.getGroupId());
            } else if (replyToText.startsWith("deposit")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("issue")) {
                Issue issue = issueMapper.selectOne(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                issueMapper.delete(new QueryWrapper<Issue>().eq("message_id", replayToMessageId));
                accountService.updateNewestData(issue.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("t")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("d")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else if (replyToText.toLowerCase().startsWith("p")) {
                Account account = accountMapper.selectOne(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                accountMapper.delete(new QueryWrapper<Account>().eq("message_id", replayToMessageId));
                issueService.updateIssueDown(account.getDown(), userDTO.getGroupId());
            } else {
                return;
            }
            accountBot.sendMessage(sendMessage, "Cancel Success");
        }
        if (text.equals("cancel deposit")) {
            if (accounts.isEmpty()) {
                accountBot.sendMessage(sendMessage, "Cancellation failed! The bill is empty");
            } else {
                List<Account> sortedUserList = accounts.stream().sorted(Comparator.comparing(Account::getAddTime)) // 按时间倒序排序
                        .collect(Collectors.toList());
                Account account = sortedUserList.get(sortedUserList.size() - 1);
                if (account.getPm()){
                    if (account.getTotal().compareTo(BigDecimal.ZERO)>0){
                        status.setPmoney(status.getPmoney().subtract(account.getTotal()));
                    }else{
                        status.setPmoney(status.getPmoney().add(account.getTotal()));
                    }
                    statusService.update( status);
                }
                accountService.deleteInData(String.valueOf(account.getId()), userDTO.getGroupId());
                issueService.updateIssueDown(sortedUserList.get(sortedUserList.size() - 1).getDown(), userDTO.getGroupId());
                accountBot.sendMessage(sendMessage, "Cancellation successful");
            }
        } else if (text.equals("undo delivery")) {
            if (issueList.isEmpty()) {
                accountBot.sendMessage(sendMessage, "Cancellation failed! The bill is empty");
            } else {
                List<Issue> sortedUserList = issueList.stream().sorted(Comparator.comparing(Issue::getAddTime)) // 按时间倒序排序
                        .collect(Collectors.toList());
                Issue issue = sortedUserList.get(sortedUserList.size() - 1);
                if (issue.getPm()){//是手动添加的需要撤销回来 status里的手动添加
                    if (issue.getDowned().compareTo(BigDecimal.ZERO)>0){
                        status.setPmoney(status.getPmoney().add(issue.getDowned()));
                    }else{
                        status.setPmoney(status.getPmoney().subtract(issue.getDowned()));
                    }
                    statusService.update( status);
                }
                issueService.deleteNewestIssue(String.valueOf(issue.getId()), userDTO.getGroupId());
                accountService.updateNewestData(sortedUserList.get(sortedUserList.size() - 1).getDown(), userDTO.getGroupId());
                accountBot.sendMessage(sendMessage, "Cancellation successful");
            }
        }

    }
    public static boolean isIssueCommand(String text) {
        if (text == null) return false;
        return text.startsWith("下发") ||
                text.startsWith("withdraw") ||
                text.matches("^t[-+]?\\d+$") ||
                text.startsWith("issue");
    }
    public static boolean isAccountCommand(String text) {
        if (text == null) return false;
        return text.matches("^d[-+]?\\d+$") ||
                text.startsWith("入款") ||
                text.startsWith("deposit");
    }
    public  boolean isValidCommand(String text) {
        if (text == null || text.isEmpty()) return false;
        // 显示类命令
        if (text.startsWith("显示操作员") ||
                text.startsWith("显示操作人") ||
                text.trim().equals("show operator") ||
                text.startsWith("设置下发地址") ||
                text.startsWith("set the delivery address") ||
                text.startsWith("修改下发地址") ||
                text.startsWith("modify the delivery address") ||
                text.startsWith("查看下发地址") ||
                text.startsWith("view the sending address")) {
            return true;
        }
        // 入账/出账命令
        if (isIssueCommand(text) || isAccountCommand(text)) {
            return true;
        }
        // 特殊金额指令
        if (text.startsWith("+") || text.startsWith("-") || text.startsWith("p")) {
            return true;
        }
        if (text.equals("取消")||text.equals("cancel")||text.trim().equals("撤销入款")||text.trim().equals("撤销下发")
                ||text.equals("cancel deposit")||text.equals("undo delivery")){
            return false;
        }
        // 空金额显示
        if (showOperatorName.isEmptyMoney(text) ||
                BaseConstant.showReplay(text) ||
                BaseConstant.showReplayEnglish(text) ||
                BaseConstant.showReplayEnglish2(text)) {
            return true;
        }
        return false;
    }

    //入账操作 issue 这个和updateAccount 一样只不过没改名 updateIssue  应下方 不对 不应该计算费率
    public void inHandle(String[] split2, String text, Account updateAccount, SendMessage sendMessage,
                         List<Account> accountList, Message message, String[] split3, Rate rate,
                         Issue issue, List<Issue> issueList, UserDTO userDTO, Status status, GroupInfoSetting groupInfoSetting) {
        text = text.toLowerCase();
        BigDecimal total;
        BigDecimal down;
        //判断是否符合公式 true 是匹配
        boolean isMatcher = utils.isMatcher(text);
        if (!isValidCommand(text)&& !updateAccount.getPm() && !issue.getPm()) {
            //+0 -0显示账单
            if (showOperatorName.isEmptyMoney(text) || BaseConstant.showReplay(text) || BaseConstant.showReplayEnglish2(text) ||
                    text.equals("撤销下发") || text.equals("撤销入款") || text.equals("undo delivery")|| text.equals("cancel deposit")) {
                showOperatorName.replay(sendMessage, userDTO, updateAccount, rate, issueList, issue, text, status, groupInfoSetting);
                return;
            }
        }
        String xiFa = "";//下发入款的字符传
        if (isIssueCommand(text)) {
            if (text.length() <= 2) return;
            if (text.toLowerCase().startsWith("t")) {
                xiFa = BaseConstant.isXiFa(text.replace("t", "下发"));
                text = BaseConstant.isXiFa(text.replace("t", "下发"));
            } else if (text.toLowerCase().startsWith("issue")) {
                xiFa = BaseConstant.isXiFa(text.replace("issue", "下发"));
                text = BaseConstant.isXiFa(text.replace("issue", "下发"));
            } else {
                xiFa = BaseConstant.isXiFa(text);
                text = BaseConstant.isXiFa(text);
            }
        } else if (isAccountCommand(text)) {//|| text.startsWith("account")是否要加
            if (text.length() <= 2) return;
            if (text.toLowerCase().startsWith("d")) {
                xiFa = BaseConstant.isRuKuan(text.replace("d", "入款"));
                text = BaseConstant.isRuKuan(text.replace("d", "入款"));
            } else if (text.toLowerCase().startsWith("account")) {
                xiFa = BaseConstant.isRuKuan(text.replace("account", "入款"));
                text = BaseConstant.isRuKuan(text.replace("account", "入款"));
            } else {
                xiFa = BaseConstant.isRuKuan(text);
                text = BaseConstant.isRuKuan(text);
            }
        }
        if (text.charAt(0) != '+' && text.charAt(0) != '-'  && !isIssueCommand(text)&& !isAccountCommand(text)
                && !updateAccount.getPm() && !issue.getPm()) return;
        BigDecimal num = new BigDecimal(0);
        Rate rate1 = null;
        //当不是公式入账时才赋值
        if (!isMatcher) {
            if (text.substring(1).endsWith("u") || text.substring(1).endsWith("U")) {
                String numberPart = text.substring(0, text.length() - 1);
                if (status.getDownExchange().compareTo(BigDecimal.ZERO) == 0) {
                    num = new BigDecimal(numberPart.substring(1)).multiply(rate.getExchange());
                } else {
                    num = new BigDecimal(numberPart.substring(1)).multiply(status.getDownExchange());
                }
                rate1 = new Rate();
                rate.setCalcU(true);//是+30U 的不计算费率
                rate1.setCalcU(true);
                rate1.setMatcher(false);
                rate1.setGroupId(rate.getGroupId());
                rate1.setExchange(rate.getExchange());
                rate1.setRate(rate.getRate());
                rate1.setAddTime(new Date());
                rateService.insertRate(rate1);
            } else if (updateAccount.getPm()) {
                Pattern pattern = Pattern.compile("^p[+]?(\\d+)$");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {//手动添加  并且不是0
                    num = new BigDecimal(matcher.group(1));
                }
            } else if (issue.getPm()) {
                Pattern pattern = Pattern.compile("^p-(-?\\d+)$");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {//手动添加  并且不是0
                    num = new BigDecimal(text.substring(1,text.length()));
                }
            } else {
                System.err.println("这个转换了一个不正常的BigDecimal------------>>>>>>>>>"+text);
                num = new BigDecimal(text.substring(1));
            }
        }
        //判断是否是第一次入账
        if (accountList.size() > 0) {
            //获取最近一次入账记录，方便后续操作
            updateAccount = accountList.get(accountList.size() - 1);
        }
        //判断是否是第一次出账
        if (issueList.size() > 0) {
            //获取最近一次出账记录，方便后续操作
            issue = issueList.get(issueList.size() - 1);
        }
        if (status.getDownRate().compareTo(BigDecimal.ZERO) != 0) {
            issue.setDownRate(status.getDownRate());
        }
        //当前时间
        updateAccount.setAddTime(new Date());
        //数据状态默认是0
        updateAccount.setRiqie(status.isRiqie());
        issue.setAddTime(new Date());
        issue.setRiqie(status.isRiqie());
//        issue.setSetTime(status.getSetTime());
        down = updateAccount.getDown();
        BigDecimal downed = issue.getDowned();
        BigDecimal downing = updateAccount.getDowning();
        total = updateAccount.getTotal();
        //如果是第一次入账，初始化
        if (accountList.size() == 0) {
            down = new BigDecimal(0);
            downed = new BigDecimal(0);
            downing = new BigDecimal(0);
            total = new BigDecimal(0);
        }
        //如果是第一次出账，初始化
        if (issueList.size() == 0) {
            downed = new BigDecimal(0);
        }
        updateAccount.setTotal(total);
        updateAccount.setDowning(downing);
        issue.setDowned(downed);
        issue.setUserId(userDTO.getUserId());
        //如果是回复消息，设置回复人的相关消息 用不用判断空 然后给空字符串
        updateAccount.setCallBackUserId(userDTO.getCallBackUserId());
        char firstChar = text.charAt(0);
        if (isMatcher) {
            //公式入账 isMatch
            utils.calcRecorded(text, userDTO.getUserId(), userDTO.getUsername(), userDTO.getGroupId(), updateAccount,
                    total, down, issue, downed, downing, status,message.getMessageId());
        }
        if (isMatcher == false && !showOperatorName.isEmptyMoney(text)) {
            if (firstChar == '+') {
                //+10u
                updateAccount.setTotal(num);
                updateAccount.setUserId(userDTO.getUserId());
                //计算应下发   num是当前的total  total里包括了以前的金额 所以用num要计算本次的下发
                downing = utils.dowingAccount(num, rate, downing);
                updateAccount.setDowning(downing.setScale(2, RoundingMode.HALF_UP));
                updateAccount.setDown(downing.subtract(downed));//总入帐-(总入帐*费率)/汇率=应下发- 已下发= 未下发
                if (rate1 != null) {
                    updateAccount.setRateId(rate1.getId());
                } else {
                    updateAccount.setRateId(rate.getId());
                }
                updateAccount.setAccountHandlerMoney(status.getAccountHandlerMoney());
                updateAccount.setRiqie(status.isRiqie());
                updateAccount.setMessageId(message.getMessageId());
                updateAccount.setPm(false);
                accountService.insertAccount(updateAccount);
            } else if (firstChar == '-') {
                issue.setUserId(userDTO.getUserId());
                issue.setRateId(rate.getId());
                issue.setDown(updateAccount.getDowning().subtract(num));//issue里的num如果是-30 updateAccount应该是＋
                issue.setDowned(num);
                issue.setCallBackUserId(userDTO.getCallBackUserId());
                issue.setIssueHandlerMoney(status.getIssueHandlerMoney());
                issue.setRiqie(status.isRiqie());
//                issue.setSetTime(status.getSetTime());
                issue.setMessageId(message.getMessageId());
                issue.setDownExchange(status.getDownExchange());
                issue.setDownRate(status.getDownRate());
                User byUserId = userService.findByUserId(userDTO.getUserId());
                issueService.insertIssue(issue);
                if (byUserId != null) {
                    accountService.updateDown(updateAccount.getDowning().subtract(num), userDTO.getGroupId());
                    log.info("执行了issue.getHandle()!=null issue--:{}", issue);
                }
            } else if (firstChar == 'p') {
                // 匹配 p100 或 p+100
                Pattern addPattern = Pattern.compile("^p\\+?(\\d+)$");
                Matcher addMatcher = addPattern.matcher(text);
                // 匹配 p-100
                Pattern subPattern = Pattern.compile("^p-(-?\\d+)$");
                Matcher subMatcher = subPattern.matcher(text);
                if (addMatcher.find()) {
                    //表示是手动添加
                    updateAccount.setTotal(num);
                    updateAccount.setUserId(userDTO.getUserId());
                    updateAccount.setPm(true);
                    //计算应下发   num是当前的total  total里包括了以前的金额 所以用num要计算本次的下发
                    downing = utils.dowingAccount(num, rate, downing);
                    updateAccount.setDowning(num);
                    updateAccount.setDown(downing.subtract(downed));//总入帐-(总入帐*费率)/汇率=应下发- 已下发= 未下发
                    if (rate1 != null) {
                        updateAccount.setRateId(rate1.getId());
                    } else {
                        updateAccount.setRateId(rate.getId());
                    }
                    updateAccount.setAccountHandlerMoney(BigDecimal.ZERO);
                    updateAccount.setRiqie(status.isRiqie());
                    updateAccount.setMessageId(message.getMessageId());
                    accountService.insertAccount(updateAccount);
                }
                if (subMatcher.find()) {
                    issue.setUserId(userDTO.getUserId());
                    issue.setRateId(rate.getId());
                    issue.setPm(true);
                    issue.setDown(num);//issue里的num如果是-30 updateAccount应该是＋
                    issue.setDowned(num);
                    issue.setCallBackUserId(userDTO.getCallBackUserId());
                    issue.setIssueHandlerMoney(BigDecimal.ZERO);
                    issue.setRiqie(status.isRiqie());
//                issue.setSetTime(status.getSetTime());
                    issue.setMessageId(message.getMessageId());
                    issue.setDownExchange(status.getDownExchange());
                    issue.setDownRate(status.getDownRate());
//                    User byUserId = userService.findByUserId(userDTO.getUserId());
                    issueService.insertIssue(issue);
//                    if (byUserId!=null){
//                        accountService.updateDown(updateAccount.getDowning().subtract(num),userDTO.getGroupId());
//                        log.info("执行了issue.getHandle()!=null issue--:{}",issue);
//                    }
                }
            }
        }
        //重新获取最新的数据
        List<Account> accounts = dateOperator.selectIsRiqie(sendMessage,status,userDTO.getGroupId());
        List<Issue> issues = dateOperator.selectIsIssueRiqie(sendMessage,status,userDTO.getGroupId());
        log.info("issues,,{}",issues);
        //获取时间数据方便后续操作
        List<String> newAccountList = new ArrayList<>();
        List<String> newIssueList=new ArrayList<>();
        for (Account account : accounts) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newAccountList.add(sdf.format(account.getAddTime()));
        }
        for (Issue issue1 : issues) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            newIssueList.add(sdf.format(issue1.getAddTime()));
        }
        String sendText1 = null;
        if (split2.length > 1 || split3.length > 1 || isMatcher || StringUtils.isNotBlank(xiFa) ||updateAccount.getPm() || issue.getPm()) {
            if (!accounts.isEmpty()) {
                updateAccount = accounts.get(accounts.size() - 1);
            }
            //发送要显示的消息
            sendText1 = getSendText(updateAccount, accounts, rate, num, newAccountList, newIssueList, issues, issue, status,groupInfoSetting);
            sendMessage.setText(sendText1);
            buttonList.implList(sendMessage, userDTO.getGroupId(), userDTO.getGroupTitle(), groupInfoSetting);
        }
        accountBot.sendMessage(sendMessage, sendText1);
    }


    //入账时发送的消息  显示操作人也用这个
    public String getSendText(Account updateAccount, List<Account> accounts, Rate rate, BigDecimal num, List<String> newList, List<String> newIssueList,
                              List<Issue> issuesList, Issue issue, Status status,GroupInfoSetting groupInfoSetting) {
        String iusseText;
        int issueHandleStatus = 0;
        int issueCallBackStatus = 0;
        int issueDetailStatus = 0;
        if (!issuesList.isEmpty()) {
            issueHandleStatus = status.getHandleStatus();
            issueCallBackStatus = status.getCallBackStatus();
            issueDetailStatus = status.getDetailStatus();
        }
        List<String> operatorNameList = this.forOperatorName(issuesList, issueHandleStatus);
        List<String> showDetailList = this.forShowDetail(issuesList, issueDetailStatus);
        List<String> callBackNames = this.forCallBackName(issuesList, issueCallBackStatus);
        StringBuilder issuesStringBuilder = new StringBuilder();
        int startIndex = Math.max(0, issuesList.size() - status.getShowFew());
        for (int i = startIndex; i < issuesList.size(); i++) {
            if (issuesList.size() > i) {
                String chatId = issuesList.get(i).getGroupId();
                int messageId = issuesList.get(i).getMessageId();
                String link;
                if (chatId.startsWith("-100")) {//表示超级群组
                    chatId = chatId.substring(4); // 去掉 -100
                    String url = String.format("https://t.me/c/%s/%d", chatId, messageId);
                    link = String.format("<a href=\"%s\">%s</a>", url, issuesList.get(i).getDowned().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                } else {//普通群组 没有跳转链接
                    link = issuesList.get(i).getDowned().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                }
                issuesStringBuilder.append("<code>"+newIssueList.get(i) + "</code>    " + link + (showDetailList.isEmpty() ? ""
                        : showDetailList.get(i))+"  " + operatorNameList.get(i));
                if (!callBackNames.isEmpty() && callBackNames.size() > i) {
                    issuesStringBuilder.append(callBackNames.get(i));
                }
                issuesStringBuilder.append("\n");
            }
        }
        //显示分类 只查询有回复人的
        if (status.getDisplaySort() == 0) {
            List<Issue> collect = issuesList.stream().filter(Objects::nonNull).filter(issue1 -> isNotBlank(issue1.getCallBackUserId())).collect(Collectors.toList());
            Map<String, List<Issue>> assembleIssueMap = collect.stream().collect(Collectors.groupingBy(Issue::getCallBackUserId, Collectors.toList()));
            if(groupInfoSetting.getEnglish()){
                issuesStringBuilder.append("出账分类：\n");
            }else {
                issuesStringBuilder.append("Outgoing payment category：\n");
            }
            assembleIssueMap.forEach((userId, issueList) -> {
                BigDecimal tot = new BigDecimal(0);
                BigDecimal down = new BigDecimal(0);
                String format = "";
                for (int i = 0; i < issueList.size(); i++) {
                    Rate rate1 = rateService.selectRateByID(issueList.get(i).getRateId());
                    tot = tot.add(issueList.get(i).getDowned());
                    down = down.add(issueList.get(i).getDowned().divide(rate1.getExchange(), 2, RoundingMode.HALF_UP));
                    User byUserId = userService.findByUserId(issueList.get(i).getCallBackUserId());
                    String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
                    String callBackLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
                    String name = callBackFirstName + callBackLastName;
                    format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(issueList.get(i).getCallBackUserId()), name);
                }
                String xf;
                if (status.getShowMoneyStatus() == 0) {
                    xf = tot + "\n";
                } else if (status.getShowMoneyStatus() == 1) {
                    xf = down + "U\n";
                } else {
                    xf = tot + " | " + down + "U\n";
                }
                issuesStringBuilder.append(format + ": " + xf);
            });
        }
        BigDecimal sxfCount2 = new BigDecimal(0);
        BigDecimal bigDecimal0 = new BigDecimal(0);//用于比较0
        if (!issuesList.isEmpty()) {
            sxfCount2 = issuesList.stream().map(Issue::getIssueHandlerMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).filter(i->!i.getPm()).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (groupInfoSetting.getEnglish()){
//                iusseText = "\n共" + (issuesList.size()) + "笔:\n" + issuesStringBuilder;
                iusseText = "\n今日下发：（" + (issuesList.size()) + "笔）\n" + issuesStringBuilder;
            }else {
                iusseText = "\nIssued  ：" + (issuesList.size()) + " \n" + issuesStringBuilder;
            }
//            iusseText = "\n已出账: <strong>" + downed + "</strong>，:共" + (issuesList.size()) + "笔:\n" + issuesStringBuilder; 暂时不显示已入账出账
        } else {
            if (updateAccount.getDown() != null) {
                issue.setDown(updateAccount.getDown());
            }
            issue.setDown(BigDecimal.ZERO);
            issue.setDowned(BigDecimal.ZERO);
            if (groupInfoSetting.getEnglish()){
                iusseText = "\n\n" + "今日下发：（0笔）\n" + "无记录";
            }else{
                iusseText = "\n\n" + "Issued  ：\n" + "No records";
            }
        }
        int accountHandleStatus = 0;
        int accountCallBackStatus = 0;
        int accountDetailStatus = 0;
        if (!accounts.isEmpty()) {
            accountHandleStatus = status.getHandleStatus();
            accountCallBackStatus = status.getCallBackStatus();
            accountDetailStatus = status.getDetailStatus();
        }
        List<String> accountOperatorNames = this.forAccountOperatorName(accounts, accountHandleStatus);
        List<String> accountCallBackNames = this.forAccountCallBackName(accounts, accountCallBackStatus);
        List<String> accountDetails = this.forAccountShowDetail(accounts, accountDetailStatus);
        BigDecimal yingxiafa = this.forYingxiafa(accounts);//应下方
        BigDecimal yixiafa = this.forYixiafa(issuesList);//已下发
        if (!accounts.isEmpty()) {
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).filter(c -> !c.getPm()).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downed2 = issuesList.stream().filter(Objects::nonNull).filter(c -> c.getPm()).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).filter(c -> !c.getPm()).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downing2 = accounts.stream().filter(Objects::nonNull).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = accounts.stream().filter(Objects::nonNull).filter(c -> !c.getPm()).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            String yxf;//应下发
            String yixf;//已下发
            String wxf;//未下发
            if (status.getShowMoneyStatus() == 0) {
                yxf = downing.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                yixf = downed.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                BigDecimal subtract = downing2.subtract(downed);
                wxf = subtract.add(downed2).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
            } else if (status.getShowMoneyStatus() == 1) {
                yxf = yingxiafa.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U";// \n换行加不加
                yixf = yixiafa.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U";
                wxf = yingxiafa.subtract(yixiafa).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U";
            } else {
                // 只有当 exchange > 1 时才显示双格式
                if (rate.getExchange().compareTo(BigDecimal.ONE) > 0) {
                    yxf = downing.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "   |    " + yingxiafa.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U";
                    yixf = downed.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "   |    " + yixiafa.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U";
                    BigDecimal subtract = downing2.subtract(downed);
                    wxf = subtract.add(downed2).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "   |    " + yingxiafa.subtract(yixiafa).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U";
                } else {
                    // exchange <= 1 时不显示 U 部分
                    yxf = downing.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                    yixf = downed.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                    BigDecimal subtract = downing2.subtract(downed);
                    wxf = subtract.add(downed2).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            int startIndex2 = Math.max(0, accounts.size() - status.getShowFew());
            for (int i = startIndex2; i < accounts.size(); i++) {//stringBuilder 我要直接截取最后status.getShowFew()条数据显示 修改
                if (accounts.size() > i) {
                    Integer messageId = accounts.get(i).getMessageId();
                    String chatId = accounts.get(i).getGroupId(); // 确保这个字段存在且是字符串类型
                    String link;
                    if (chatId.startsWith("-100")) {//表示超级群组
                        chatId = chatId.substring(4); // 去掉 -100
                        String url = String.format("https://t.me/c/%s/%d", chatId, messageId);
                        link = String.format("<a href=\"%s\">%s</a>", url, accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                    } else {//普通群组 没有跳转链接
                        link = accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                    }
                    stringBuilder.append("<code>"+newList.get(i) + "</code>    " + link + " " +
                            accountDetails.get(i) + "  " + accountOperatorNames.get(i) + " ");
                    if (!accountCallBackNames.isEmpty() && accountCallBackNames.size() > i) {
                        stringBuilder.append(accountCallBackNames.get(i));
                    }
                    stringBuilder.append("\n");
                }
            }
            //显示分类
            if (status.getDisplaySort() == 0) {
                List<Account> collect = accounts.stream().filter(Objects::nonNull).filter(account -> isNotBlank(account.getCallBackUserId())).collect(Collectors.toList());
                if (groupInfoSetting.getEnglish()){
                    stringBuilder.append("入款分类：" + "\n");
                }else{
                    stringBuilder.append("Deposit Category：" + "\n");
                }
                Map<String, List<Account>> assembleAccountMap = collect.stream().collect(Collectors.groupingBy(Account::getCallBackUserId, Collectors.toList()));
                assembleAccountMap.forEach((userId, accountList) -> {
                    BigDecimal tot = new BigDecimal(0);
                    BigDecimal down = new BigDecimal(0);
                    String format = "";
                    for (int i = 0; i < accountList.size(); i++) {
                        Rate rate1 = rateService.selectRateByID(accountList.get(i).getRateId());
                        tot = tot.add(accountList.get(i).getTotal());
                        down = down.add(accountList.get(i).getDowning().divide(rate1.getExchange(), 2, RoundingMode.HALF_UP));
                        User byUserId = userService.findByUserId(accountList.get(i).getCallBackUserId());
                        String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
                        String callBackLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
                        String name = callBackFirstName + callBackLastName;
                        format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(accountList.get(i).getCallBackUserId()), name);
                    }
                    String xf;
                    if (status.getShowMoneyStatus() == 0) {
                        xf = tot + "\n";
                    } else if (status.getShowMoneyStatus() == 1) {
                        xf = down + "U\n";
                    } else {
                        xf = tot + " | " + down + "U\n";
                    }
                    stringBuilder.append(format + ": " + xf);
                });
            }
            String sxf = "";
            BigDecimal sxfCount = accounts.stream().map(Account::getAccountHandlerMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            String dbr1;
            String dbx1;
            String sxfz1;
            String xfhl1;
            String xffl1;
            String sdtj1;
            String zrz1;
            String hl1;
            String fl1;
            String yxf1;//应下发
            String yxf2;//已下发
            String wxf1;
            String jrrk1;
            String b1;
            if (groupInfoSetting.getEnglish()){
                dbr1="单笔入款手续费";
                dbx1="单笔下发手续费";
                sxfz1="手续费总";
                xfhl1="下发汇率";
                xffl1="下发费率";
                sdtj1="手动添加";
                zrz1="总入账";
                hl1="汇率";
                fl1="费率";
                yxf1="应下发";
                yxf2="已下发";
                wxf1="未下发";
                jrrk1="今日入款：（";
                b1="笔）";
            }else{
                dbr1="Single deposit fee";
                dbx1="Single transaction fee";
                sxfz1="Fee total";
                xfhl1="Sending exchange rate";
                xffl1="Sending rate";
                sdtj1="Manually add";
                zrz1="Total Account";
                hl1="exchange rate";
                fl1="Rate";
                yxf1="Should be issued";
                yxf2="Issued";
                wxf1="Unissued";
                jrrk1="Deposit  ：";
                b1="";
            }
            String rukuan = status.getAccountHandlerMoney().compareTo(bigDecimal0) == 0 ? "" : "\n"+dbr1+"：<strong>" + status.getAccountHandlerMoney().stripTrailingZeros().toPlainString()+"</strong>";
            String xiafa = status.getIssueHandlerMoney().compareTo(bigDecimal0) == 0 ? "" : "\n"+dbx1+"：<strong>" + status.getIssueHandlerMoney().stripTrailingZeros().toPlainString()+"</strong>";
            String count = sxfCount.add(sxfCount2).compareTo(bigDecimal0) == 0 ? "" : "\n"+sxfz1+"：<strong>" + sxfCount.add(sxfCount2).setScale(0, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+"</strong>";
            sxf = rukuan + xiafa + count;//sxf2 是下发手续费
            String downExchangeText = status.getDownExchange().compareTo(bigDecimal0) == 0 ? "" : "\n"+xfhl1+"：<strong>" + status.getDownExchange().stripTrailingZeros().toPlainString()+"</strong>";
            String downRate = status.getDownRate().compareTo(bigDecimal0) == 0 ? "" : "\n"+xffl1+"：<strong>" + status.getDownRate().stripTrailingZeros().toPlainString()+"</strong>";
            String pAmountText = status.getPmoney().compareTo(BigDecimal.ZERO) != 0
                    ? "\n"+sdtj1+" ： <strong>" + status.getPmoney().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+"</strong>"
                    : "";
            //            入款分类：
            return "\n"+jrrk1+ (accounts.size()) + b1+"\n" +
//                    "\n已入账：<strong>" + total + "</strong>，:共" + (accounts.size()) + "笔:\n" +  暂时不用显示 已入账
                    stringBuilder + iusseText + "\n" +
                    "\n\n"+zrz1+"：<strong>" + total.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() +"</strong>"+
                    (rate.getExchange().compareTo(BigDecimal.ONE) > 0 ? "\n"+hl1+" ： <strong>" + rate.getExchange().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() +"</strong>": "") +
                    (rate.getRate().compareTo(BigDecimal.ZERO) > 0 ? "\n"+fl1+"：<strong>" + rate.getRate().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() +"</strong>": "") +
                    downExchangeText +
                    downRate +
                    "\n"+yxf1+" ： <strong>" + yxf +"</strong>"+
                    "\n"+yxf2+" ： <strong>" + yixf +"</strong>"+
                    pAmountText +
                    "\n"+wxf1+" ： <strong>" + wxf +"</strong>"+
                    (status.getShowHandlerMoneyStatus() == 0 ? sxf : "");
        } else {
            //已下发
            BigDecimal downed = issuesList.stream().filter(Objects::nonNull).filter(c -> !c.getPm()).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //用于区分手动添加的
            BigDecimal downed2 = issuesList.stream().filter(Objects::nonNull).filter(c -> c.getPm()).map(Issue::getDowned).reduce(BigDecimal.ZERO, BigDecimal::add);
            //应下发
            BigDecimal downing = accounts.stream().filter(Objects::nonNull).filter(c -> !c.getPm()).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal downing2 = accounts.stream().filter(Objects::nonNull).filter(c -> !c.getPm()).map(Account::getDowning).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal total = accounts.stream().filter(Objects::nonNull).map(Account::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            String yxf;//应下方
            String yixf;//已下发
            String wxf;//未下发
            if (status.getShowMoneyStatus() == 0) {
                yxf = downing.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                yixf = downed.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                BigDecimal subtract = downing2.subtract(downed);
                wxf = subtract.add(downed2).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
            } else if (status.getShowMoneyStatus() == 1) {
                yxf = yingxiafa + "U";
                yixf = yixiafa + "U";
                wxf = yingxiafa.subtract(yixiafa) + "U";
            } else {
                if (rate.getExchange().compareTo(BigDecimal.ONE) > 0) {
                    yxf = downing.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "   |    " + yingxiafa + "U";
                    yixf = downed.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "   |    " + yixiafa + "U";
                    BigDecimal subtract = downing2.subtract(downed);
                    wxf = subtract.add(downed2).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "   |    " + yingxiafa.subtract(yixiafa) + "U";
                } else {
                    // exchange <= 1 时不显示 U 部分
                    yxf = downing.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                    yixf = downed.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                    BigDecimal subtract = downing2.subtract(downed);
                    wxf = subtract.add(downed2).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "";
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            if (!accounts.isEmpty()) {
                for (int i = 0; i < status.getShowFew(); i++) {
                    if (accounts.size() > i) {
                        stringBuilder.append(
                                "<code>"+newList.get(i) + "</code>    " + accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + " "
                                        + accountDetails.get(i) + " " + accountOperatorNames.get(i) + " " + accountCallBackNames.get(i) + "\n");
                    }
                }
            }
            String sxf = "";
            String dbr1;
            String dbx1;
            String sxfz1;
            String xfhl1;
            String xffl1;
            String sdtj1;
            String zrz1;
            String hl1;
            String fl1;
            String yxf1;//应下发
            String yxf2;//已下发
            String wxf1;
            String none1;//无记录
            String jrrk1;
            String b1;
            if (groupInfoSetting.getEnglish()){
                dbr1="单笔入款手续费";
                dbx1="单笔下发手续费";
                sxfz1="手续费总";
                xfhl1="下发汇率";
                xffl1="下发费率";
                sdtj1="手动添加";
                zrz1="总入账";
                hl1="汇率";
                fl1="费率";
                yxf1="应下发";
                yxf2="已下发";
                wxf1="未下发";
                none1="无记录";
                jrrk1="今日入款：（";
                b1="笔）";
            }else{
                dbr1="Single deposit fee";
                dbx1="Single transaction fee";
                sxfz1="Fee total";
                xfhl1="Sending exchange rate";
                xffl1="Sending rate";
                sdtj1="Manually add";
                zrz1="Total Account";
                hl1="exchange rate";
                fl1="Rate";
                yxf1="Should be issued";
                yxf2="Issued";
                wxf1="Unissued";
                none1="No record";
                jrrk1="Deposit  ：";
                b1="";
            }
            BigDecimal sxfCount = accounts.stream().map(Account::getAccountHandlerMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            String rukuan = status.getAccountHandlerMoney().compareTo(bigDecimal0) == 0 ? "" : "\n"+dbr1+"：<strong>" + status.getAccountHandlerMoney()+"</strong>";
            String xiafa = status.getIssueHandlerMoney().compareTo(bigDecimal0) == 0 ? "" : "\n"+dbx1+"：<strong>" + status.getIssueHandlerMoney()+"</strong>";
            String count = sxfCount.add(sxfCount2).compareTo(bigDecimal0) == 0 ? "" : "\n"+sxfz1+"：<strong>" + sxfCount.add(sxfCount2).setScale(0, RoundingMode.HALF_UP)+"</strong>";
            sxf = rukuan + xiafa + count;//sxf2 是下发手续费
            String downExchangeText = status.getDownExchange().compareTo(bigDecimal0) == 0 ? "" : "\n"+xfhl1+"：<strong>" + status.getDownExchange()+"</strong>";
            String downRate = status.getDownRate().compareTo(bigDecimal0) == 0 ? "" : "\n"+xffl1+"：<strong>" + status.getDownRate()+"</strong>";
            String pAmountText = status.getPmoney().compareTo(BigDecimal.ZERO) != 0
                    ? "\n"+sdtj1+" ： <strong>" + status.getPmoney().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+"</strong>"
                    : "";
            return "\n"+jrrk1+ (accounts.size()) + b1+"\n" +
//                    "\n已入账：<strong>" + total + "</strong>，:共" + (accounts.size()) + "笔:\n" + 暂时不用显示已入账
                    " " + ""+none1+"" + iusseText +
                    "\n\n"+zrz1+"：<strong>" + 0 +"</strong>"+
                    (rate.getExchange().compareTo(BigDecimal.ONE) > 0 ? "\n"+hl1+" ： <strong>" + rate.getExchange().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+"</strong>" : "") +
                    (rate.getRate().compareTo(BigDecimal.ZERO) > 0 ? "\n"+fl1+"：<strong>" + rate.getRate().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()+"</strong>" : "") +
                    downExchangeText +
                    downRate +
                    "\n"+yxf1+" ： <strong>" + yxf +"</strong>"+
                    "\n"+yxf2+" ： <strong>" + yixf +"</strong>"+
                    pAmountText +
                    "\n"+wxf1+" ： <strong>" + wxf +"</strong>"+
                    (status.getShowHandlerMoneyStatus() == 0 ? sxf : "");
        }

    }

    private BigDecimal forYixiafa(List<Issue> issuesList) {
        BigDecimal temp = new BigDecimal(0);
        for (int i = 0; i < issuesList.size(); i++) {
            Issue issue = issuesList.get(i);
            if (issue.getPm()) {

            } else {
                Rate rate1 = rateService.selectRateByID(issue.getRateId());
                BigDecimal downExchange = issue.getDownExchange();
                BigDecimal downRate = issue.getDownRate(); // 下发费率，如 12 表示 12%
                // 使用自定义汇率或默认汇率
                BigDecimal exchange = downExchange.compareTo(BigDecimal.ZERO) != 0 ? downExchange : rate1.getExchange();
//            exchange = exchange.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
                BigDecimal uValue = issue.getDowned().divide(exchange, 2, RoundingMode.HALF_UP);
                // 如果有费率，扣除对应比例
                if (downRate.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP); // 转换为小数
                    BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros(); // 扣除的手续费
                    uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros(); // 实际到账金额
                }
                temp = temp.add(uValue);
            }
        }
        return temp;
    }

    //应下方
    private BigDecimal forYingxiafa(List<Account> accounts) {
        BigDecimal temp = new BigDecimal(0);
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getPm()) {

            } else {
                Rate rate1 = rateService.selectRateByID(accounts.get(i).getRateId());
                BigDecimal exchange = rate1.getExchange();
                BigDecimal divide = accounts.get(i).getDowning().divide(exchange, 2, RoundingMode.HALF_UP);
                temp = temp.add(divide);
            }
        }
        return temp;
    }

    //-----------------------------------Account 操作---------------------------------------
    public List<String> forAccountOperatorName(List<Account> accounts, int handleStatus) {
        //是否隐藏操作人 @ +accounts.get(0).getHandle()
        List<String> operatorNameList = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            User byUserId = userService.findByUserId(accounts.get(i).getUserId());
            String operatorFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
            String operatorNameLast = byUserId.getLastName() == null ? "" : byUserId.getLastName();
            //显示操作人
            String operatorName = handleStatus == 0 ? operatorFirstName + operatorNameLast : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(accounts.get(i).getUserId()), operatorName);
            operatorNameList.add(format);
        }
        return operatorNameList;
    }

    public List<String> forAccountCallBackName(List<Account> accounts, int callBackStatus) {
        List<String> callBackNameList = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getCallBackUserId() == null) {
                callBackNameList.add("");
                continue;
            }
            User byUserId = userService.findByUserId(accounts.get(i).getCallBackUserId());
            String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
            String callBackLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
            //显示回复人
            String callBackName = callBackStatus == 0 ? callBackFirstName + callBackLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(byUserId.getUserId()), callBackName);
            callBackNameList.add(format);
        }
        return callBackNameList;
    }

    public List<String> forAccountShowDetail(List<Account> accounts, int detailStatus) {
        List<String> showDetailList = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getPm()) {
//                BigDecimal total = accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP);
                //显示明细
                String showDetail = "";//-不涉及费率
                showDetailList.add(showDetail);
            } else {
                Rate rate = rateService.selectRateByID(accounts.get(i).getRateId());
                BigDecimal exchange = rate.getExchange().setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = accounts.get(i).getTotal().setScale(2, RoundingMode.HALF_UP);
                BigDecimal totalTimesRate1;
                BigDecimal rateRate = rate.getRate().multiply(BigDecimal.valueOf(0.01)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal total2;
                if (!rate.isCalcU()) {
                    totalTimesRate1 = total.multiply(rateRate).setScale(2, RoundingMode.HALF_UP);
                    total2 = total.subtract(totalTimesRate1);
                } else {
                    total2 = total;
                }
                String isCalc = "";
                if (rate.getRate().compareTo(BigDecimal.ZERO) > 0) {
                    isCalc = !rate.isCalcU() ? "*" + rate.getRate().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() : "";
                } else {
                    isCalc = "";
                }
                //显示明细
                String showDetail = detailStatus == 0 ? "/ " + rate.getExchange().setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                        + isCalc + "=" +
                        total2.divide(exchange, 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "U" : "";//-不涉及费率
                showDetailList.add(showDetail);
            }
        }
        return showDetailList;
    }

    //-----------------------------------Issue 操作---------------------------------------
    public List<String> forOperatorName(List<Issue> issueList, int handleStatus) {
        List<String> operatorNameList = new ArrayList<>();
        for (int i = 0; i < issueList.size(); i++) {
            User byUserId = userService.findByUserId(issueList.get(i).getUserId());
            String handleFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
            String handleLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
            //显示操作人
            String operatorName = handleStatus == 0 ? handleFirstName + handleLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(issueList.get(i).getUserId()), operatorName);
            operatorNameList.add(format);
        }
        return operatorNameList;
    }

    public List<String> forCallBackName(List<Issue> issuesList, int callBackStatus) {
        List<String> callBackNameList = new ArrayList<>();
        for (int i = 0; i < issuesList.size(); i++) {
            if (issuesList.get(i).getCallBackUserId() == null) {
                callBackNameList.add("");
                continue;
            }
            User byUserId = userService.findByUserId(issuesList.get(i).getCallBackUserId());//查询回复人信息
            String callBackFirstName = byUserId.getFirstName() == null ? "" : byUserId.getFirstName();
            String callBackLastName = byUserId.getLastName() == null ? "" : byUserId.getLastName();
            //显示回复人
            String callBackName = callBackStatus == 0 ? callBackFirstName + callBackLastName : "";
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(byUserId.getUserId()), callBackName);
            callBackNameList.add(format);
        }
        return callBackNameList;
    }

    public List<String> forShowDetail(List<Issue> issuesList, int detailStatus) {
        List<String> showDetailList = new ArrayList<>();
        for (int i = 0; i < issuesList.size(); i++) {
            if (issuesList.get(i).getPm()) {
                //显示明细
                String showDetail = "";//-不涉及费率
                showDetailList.add(showDetail);
            } else {
                Issue issue = issuesList.get(i);
                Rate rate = rateService.selectRateByID(issue.getRateId());
                // 获取下发汇率和费率
                BigDecimal downExchange = issue.getDownExchange();
                BigDecimal downRate = issue.getDownRate(); // 下发费率，如 12 表示 12%
                // 使用自定义汇率或默认汇率
                BigDecimal exchange = downExchange.compareTo(BigDecimal.ZERO) != 0 ? downExchange : rate.getExchange();
                exchange = exchange.setScale(2, RoundingMode.HALF_UP);
                String exchange2=exchange.compareTo(BigDecimal.ONE)==0?exchange+"":exchange.stripTrailingZeros().toPlainString();
                BigDecimal total = issue.getDowned().setScale(2, RoundingMode.HALF_UP);
                // 计算 U 值：金额 / 汇率
                BigDecimal uValue = total.divide(exchange, 2, RoundingMode.HALF_UP);
                String feeInfo = ""; // 费率信息
                // 如果有费率，扣除对应比例
                if (downRate.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal feePercent = downRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP).stripTrailingZeros(); // 转换为小数
                    BigDecimal feeAmount = uValue.multiply(feePercent).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros(); // 扣除的手续费
                    uValue = uValue.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros(); // 实际到账金额
                    feeInfo = " * " + downRate; // 显示格式如 *12%
                }
                // 构建显示文本
                String showDetail = detailStatus == 0 ? "/ " + exchange2 + feeInfo + "=" + uValue.stripTrailingZeros().toPlainString() + "U" : "";
                showDetailList.add(showDetail);
            }
        }
        return showDetailList;
    }
    //我需要识别 p100 p+100这种的 然后给status.setPmoney(100) 设置上

    public void pHandle(UserDTO userDTO, Status status, Account updateAccount, Issue issue) {
        String text = userDTO.getText().toLowerCase();
        if (text.startsWith("p")) {
            // 匹配 p100 或 p+100
            Pattern addPattern = Pattern.compile("^p[+]?(\\d+)$");
            Matcher addMatcher = addPattern.matcher(text);
            // 匹配 p-100
            Pattern subPattern = Pattern.compile("^p-(-?\\d+)$");
            Matcher subMatcher = subPattern.matcher(text);
            if (addMatcher.find()) {
                BigDecimal value = new BigDecimal(addMatcher.group(1));
                status.setPmoney(status.getPmoney().add(value));
                statusService.update(status);
                updateAccount.setPm(true);
            } else if (subMatcher.find()) {
                BigDecimal value = new BigDecimal(subMatcher.group(1));
                status.setPmoney(status.getPmoney().subtract(value));
                statusService.update(status);
                issue.setPm(true);
            } else {
                // 都不匹配
                updateAccount.setPm(false);
                issue.setPm(false);
            }
        }
    }
}
