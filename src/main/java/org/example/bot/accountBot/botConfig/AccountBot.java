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
    NotificationService notificationService;
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
        if (update.hasMessage() && update.getMessage().hasText())
            this.BusinessHandler(message,sendMessage,replyToText,userDTO);
    }
    //getMessageContentIsContain  小心添加命令后拦截 注意!!!!
    public void BusinessHandler(Message message,SendMessage sendMessage, String replyToText, UserDTO userDTO) {
        User user = userService.findByUserId(userDTO.getUserId());
        User user2 = userService.findByUsername(userDTO.getUsername());
        //改成sql查询username 和userId 不要全查了 并且isNormal是false
        this.setIsAdminUser(sendMessage,userDTO,user,user2);
        //计算器功能
        utils.counter(message,sendMessage);
        notificationService.initNotification(userDTO);
        if (message.getText().charAt(0)!='+' && message.getText().charAt(0)!='-' &&
                !BaseConstant.getMessageContentIsContain(message.getText()) ) {
            return ;
        }
        boolean present=true;
        if (user!=null){
            present = user.isNormal();
        }
        if (user2!=null){
             present = user2.isNormal();
        }
        if (!userDTO.getUserId().equals(adminUserId)&& present){
            this.sendMessage(sendMessage,"不是管理 请联系管理员!");
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
        //搜索出历史账单/判断是否过期
        List<Account> accountList=dateOperator.isOver24Hour(message,sendMessage,userDTO.getGroupId());
        //搜索出历史下发订单/判断是否过期
        List<Issue> issueList =dateOperator.issueIsOver24Hour(message,sendMessage,userDTO);
        //设置操作人员
        settingOperatorPerson.setHandle(split1, sendMessage, message,message.getText(),userDTO);
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
        dateOperator.deleteTodayData(message,sendMessage,userDTO.getGroupId());
        //通知功能
        notificationService.inform(message.getText(),sendMessage);
    }
    //判断消息是否是普通用户发送的消息 如果是就保存
    public void setIsAdminUser(SendMessage sendMessage,UserDTO userDTO,User user,User byUsername){
        //能发送消息userid就不为空 因为有一种空的用户名情况 但是没有空的userId情况
        if (user!=null ) {
            if (user.getUserId()!=null){
                String firstName=user.getFirstName()==null?"": user.getFirstName();
                String lastName=user.getLastName()==null?"": user.getLastName();
                String name=firstName+lastName;
                String firstNameDTO=userDTO.getFirstName()==null?"": userDTO.getFirstName();
                String lastNameDTO=userDTO.getLastName()==null?"": userDTO.getLastName();
                String nameDTO=firstNameDTO+lastNameDTO;
                if (!user.getUsername().equals(userDTO.getUsername())){
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
            userNew.setOldUsername(userDTO.getUsername()==null?"":userDTO.getUsername());
            userNew.setOldFirstName(userDTO.getFirstName()==null?"":userDTO.getFirstName());
            userNew.setOldLastName(userDTO.getLastName()==null?"":userDTO.getLastName());
            if (userDTO.getUserId().equals(adminUserId)){
                userNew.setNormal(false);
            }else {
                userNew.setNormal(true);
            }
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


