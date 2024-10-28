package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.*;

import org.example.bot.accountBot.service.*;
import org.example.bot.accountBot.utils.BaseConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class AccountBot extends TelegramLongPollingBot {
    @Value("${telegram.bot.token}")
    protected String botToken;
    @Value("${telegram.bot.username}")
    protected String username;
    @Value("${adminUserId}")
    protected String adminUserId;
    @Autowired
    protected RateService rateService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected StatusService statusService;
    @Autowired
    protected Utils utils;
    @Autowired
    protected DateOperator dateOperator; //操作时间
    @Autowired
    protected SettingOperatorPerson settingOperatorPerson;   //设置操作人员
    @Autowired
    protected ShowOperatorName showOperatorName;   //显示操作人名字
    @Autowired
    protected RuzhangOperations ruzhangOperations;    //入账和入账时发送的消息
    @Autowired
    protected NotificationService notificationService;
    @Autowired
    protected PaperPlaneBotSinglePerson paperPlaneBotSinglePerson;
    @Autowired
    protected AccountService accountService;
    @Autowired
    protected IssueService issueService;
    @Override
    public String getBotUsername() {
        return username;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }
    @Override
    public void onUpdateReceived(Update update) {
        String replyToText=null;
        if (update != null && update.getMessage() != null && update.getMessage().getReplyToMessage() != null) {
            replyToText = update.getMessage().getReplyToMessage().getText();
            if (replyToText != null) {
                log.info("ReplyToText: {}", replyToText);
            }
        }
        //接收消息
        assert update != null;
        Message message = update.getMessage();
        UserDTO userDTO = new UserDTO();
        if (update.getMessage() != null && update.getMessage().getFrom() != null &&
                update.getMessage().getReplyToMessage() != null && update.getMessage().getReplyToMessage().getFrom() != null) {
            userDTO.setCallBackUserId(update.getMessage().getReplyToMessage().getFrom().getId()+"");
            userDTO.setCallBackName(update.getMessage().getReplyToMessage().getFrom().getUserName());  // 确保 userName 不为 null
            userDTO.setCallBackFirstName(update.getMessage().getReplyToMessage().getFrom().getFirstName());
            userDTO.setCallBackLastName(update.getMessage().getReplyToMessage().getFrom().getLastName());
        }
        SendMessage sendMessage = new SendMessage();
        if (message==null)return;
        userDTO.setInfo(message);
        sendMessage.setChatId(String.valueOf(message.getChatId()==null?"":message.getChatId()));
        if (update.hasMessage() && update.getMessage().hasText()) this.BusinessHandler(message,sendMessage,replyToText,userDTO);
    }
    //getMessageContentIsContain  小心添加命令后拦截 注意!!!!
    public void BusinessHandler(Message message,SendMessage sendMessage, String replyToText, UserDTO userDTO) {
        //私聊的机器人  处理个人消息
        if (message.getChat().isUserChat()){
            paperPlaneBotSinglePerson.handleNonGroupMessage(message,sendMessage,userDTO);
            return;
        }
        User userTemp = userService.findByUserId(userDTO.getUserId());
        User userTemp2 = userService.findByUsername(userDTO.getUsername());
        //改成sql查询username 和userId 不要全查了 并且isNormal是false
        this.setIsAdminUser(sendMessage,userDTO,userTemp,userTemp2);
        User user1;
        if (userTemp!=null){
            user1=userTemp;
        }else if (userTemp2!=null){
            user1=userTemp2;
        }else {
            user1=userService.findByUserId(userDTO.getUserId());
        }
        //计算器功能
        utils.counter(message,sendMessage);
        notificationService.initNotification(userDTO);
        if (message.getText().charAt(0)!='+' && message.getText().charAt(0)!='-' &&
                !BaseConstant.getMessageContentIsContain(message.getText()) ) {
            return ;
        }
        User user = userService.findByUserId(user1.getSuperiorsUserId());
        if (user1.getSuperiorsUserId()!=null){
            //根据群组id查询不是空的ValidTime
            if (user==null||user.getValidTime().compareTo(new Date())<0){
                String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(adminUserId), "超级管理");
                this.sendMessage(sendMessage,"管理员无效或不在有效期内!请联系: "+format);
                return;
            }
        }else {
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(adminUserId), "超级管理");
            this.sendMessage(sendMessage,"管理员无效或不在有效期内!请联系: "+format);
            return;
        }
        if (!user1.isOperation()){
            this.sendMessage(sendMessage,"不是操作员 请联系管理员!");
            return;
        }
        String[] split1 = message.getText().split(" ");
        String[] split2 = message.getText().split("\\+");
        String[] split3 = message.getText().split("-");
        //初始化
        Rate rate=rateService.getInitRate(userDTO.getGroupId());
        Status status=statusService.getInitStatus(userDTO.getGroupId());
        Account updateAccount = new Account();
        Issue issue=new Issue();
        updateAccount.setGroupId(userDTO.getGroupId());
        issue.setGroupId(userDTO.getGroupId());
        //没有用户名的情况下
        if (StringUtils.isEmpty(userDTO.getUsername()))userDTO.setUsername("");
        //设置日切
        dateOperator.isOver24HourCheck(message, sendMessage, userDTO, status);
        List<Account> accountList=dateOperator.selectAccountIsRiqie(sendMessage,status,userDTO.getGroupId());

        //搜索出历史下发订单/判断是否过期
        List<Issue> issueList=issueService.selectIssueRiqie(status.isRiqie(),userDTO.getGroupId());
        //设置操作人员
        settingOperatorPerson.setHandle(split1, sendMessage, message,message.getText(),userDTO,user1,status);
        //设置费率/汇率
        ruzhangOperations.setRate(message,sendMessage,rate);
        //撤销入款
        ruzhangOperations.repeal(message,sendMessage,accountList,replyToText,userDTO,issueList);
        //入账操作
        ruzhangOperations.inHandle(split2,message.getText(),  updateAccount,  sendMessage, accountList, message,split3,
                rate,issue,issueList,userDTO,status);
        //入账时候已经调用过 +0显示账单用
        if (!showOperatorName.isEmptyMoney(message.getText())){
            //显示操作人名字 && 显示明细
            showOperatorName.replay(sendMessage,userDTO,updateAccount,rate,issueList,issue,message.getText(),status);
        }
        //删除操作人员
        settingOperatorPerson.deleteHandle(message.getText(),sendMessage);
        //删除今日数据/关闭日切/
        dateOperator.deleteTodayData(message,sendMessage,userDTO.getGroupId(),status);
        //通知功能
        notificationService.inform(message.getText(),sendMessage);
    }


    //判断消息是否是普通用户发送的消息 如果是就保存
    public void setIsAdminUser(SendMessage sendMessage,UserDTO userDTO,User user,User byUsername){
        if (userDTO.getCallBackUserId()!=null){
            //这个是回复人id  为了判断机器人消息
            User byUserId = userService.findByUserId(userDTO.getCallBackUserId());
            if (byUserId==null){
                User userNew = new User();
                userNew.setUserId(userDTO.getCallBackUserId());
                userNew.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                userNew.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                userNew.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                userNew.setOldUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                userNew.setOldFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                userNew.setOldLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                userNew.setOperation(false);
                userNew.setValidTime(new Date());
                userNew.setCreateTime(new Date());
                userService.insertUser(userNew);
            }
        }
        //能发送消息userid就不为空 因为有一种空的用户名情况 但是没有空的userId情况
        if (user!=null ) {
            if (user.getUserId()!=null){
                String firstName=user.getFirstName()==null?"": user.getFirstName();
                String lastName=user.getLastName()==null?"": user.getLastName();
                String name=firstName+lastName;
                String firstNameDTO=userDTO.getFirstName()==null?"": userDTO.getFirstName();
                String lastNameDTO=userDTO.getLastName()==null?"": userDTO.getLastName();
                String nameDTO=firstNameDTO+lastNameDTO;
                String username=userDTO.getUsername()==null?"": userDTO.getUsername();
                if (!user.getUsername().equals(username)){
                    String message="⚠\uFE0F用户名变更通知⚠\uFE0F\n"
                            +"用户:"+user.getUsername()+"\n"
                            +"⬇\uFE0F\n"
                            +userDTO.getUsername();
                    sendMessage(sendMessage,message);
                }
                if (!name.equals(nameDTO)){
                    String message="⚠\uFE0F用户昵称变更通知⚠\uFE0F\n"
                            +"用户:"+name+"\n"
                            +"⬇\uFE0F\n"
                            +nameDTO;
                    sendMessage(sendMessage,message);
                    sendMessage(sendMessage,message);
                }
                user.setLastName(lastNameDTO);
                user.setFirstName(firstNameDTO);
                userService.updateUserid(user);
            }
        } else if (byUsername!=null ){//设置完操作员  然后操作员也不说话 然后操作员直接修改了用户名 会有问题
            if (byUsername.getUsername()!=null){
                String firstNameDTO=userDTO.getFirstName()==null?"": userDTO.getFirstName();
                String lastNameDTO=userDTO.getLastName()==null?"": userDTO.getLastName();
                byUsername.setUserId(userDTO.getUserId());
                byUsername.setLastName(lastNameDTO);
                byUsername.setFirstName(firstNameDTO);
                userService.updateUsername(byUsername);
            }
        }else {
            User userNew = new User();
            userNew.setUserId(userDTO.getUserId());
            userNew.setUsername(userDTO.getUsername()==null?"":userDTO.getUsername());
            userNew.setLastName(userDTO.getLastName()==null?"":userDTO.getLastName());
            userNew.setFirstName(userDTO.getFirstName()==null?"":userDTO.getFirstName());
            userNew.setOldUsername(userDTO.getUsername()==null?"":userDTO.getUsername());
            userNew.setOldFirstName(userDTO.getFirstName()==null?"":userDTO.getFirstName());
            userNew.setOldLastName(userDTO.getLastName()==null?"":userDTO.getLastName());
            userNew.setOperation(false);
            userNew.setValidTime(new Date());
            userNew.setCreateTime(new Date());
            userService.insertUser(userNew);
        }
    }

    public void sendMessage(SendMessage sendMessage,String text) {
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
//        sendMessage.enableMarkdown(true);
        try {
            log.info("发送消息:{}", text);
            execute(sendMessage);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
 }


