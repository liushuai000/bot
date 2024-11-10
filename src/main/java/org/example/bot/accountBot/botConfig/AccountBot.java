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
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    private UserAuthorityService userAuthorityService;
    @Autowired
    private NowExchange nowExchange;//获取最新汇率

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
        onJinQunMessage(update);
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
        if (update.hasCallbackQuery()){
            Rate rate=rateService.getInitRate(update.getCallbackQuery().getMessage().getChatId().toString());
            nowExchange.CallbackQuery(message,sendMessage,rate,update);
            return;
        }
        if (message==null) {
            return;
        }
        userDTO.setInfo(message);
        sendMessage.setChatId(String.valueOf(message.getChatId()==null?"":message.getChatId()));
        if (update.hasMessage() && update.getMessage().hasText()) this.BusinessHandler(message,sendMessage,replyToText,userDTO,update);
    }

    public void BusinessHandler(Message message,SendMessage sendMessage, String replyToText, UserDTO userDTO,Update update) {
        //私聊的机器人  处理个人消息
        if (message.getChat().isUserChat()){
            paperPlaneBotSinglePerson.handleTronAccountMessage(sendMessage,update,userDTO);
            paperPlaneBotSinglePerson.handleNonGroupMessage(message,sendMessage,userDTO);
            return;
        }
        if(userDTO.getText().startsWith("查询")){
            nowExchange.Query(sendMessage,update);
        }
        User userTemp = userService.findByUserId(userDTO.getUserId());
        User userTemp2 = userService.findByUsername(userDTO.getUsername());
        UserAuthority userAuthority=userAuthorityService.selectByUserAndGroupId(userDTO.getUserId(),userDTO.getGroupId());
        UserAuthority userAuthorityName=userAuthorityService.findByUsername(userDTO.getUsername(),userDTO.getGroupId());
        //改成sql查询username 和userId 不要全查了 并且isNormal是false
        this.setIsAdminUser(sendMessage,userDTO,userTemp,userTemp2);
        User user1;
        UserAuthority userAuthorityName1;
        if (userTemp!=null){
            user1=userTemp;
            userAuthorityName1=userAuthority;
        }else if (userTemp2!=null){
            user1=userTemp2;
            userAuthorityName1=userAuthorityName;
        }else {
            user1=userService.findByUserId(userDTO.getUserId());
            userAuthorityName1=userAuthorityService.selectByUserAndGroupId(userDTO.getUserId(),userDTO.getGroupId());
        }
        if (userDTO.getText().equals("z0")||userDTO.getText().equals("Z0")){
            Rate rate=rateService.getInitRate(userDTO.getGroupId());
            nowExchange.getNowExchange(message,sendMessage,userDTO,rate,update);
        }
        //计算器功能
        utils.counter(message,sendMessage);
        notificationService.initNotification(userDTO);
        if (message.getText().charAt(0)!='+' && message.getText().charAt(0)!='-' &&
                !BaseConstant.getMessageContentIsContain(message.getText()) ) {
            return ;
        }
        User user = userService.findByUserId(user1.getSuperiorsUserId());
        if (user1.getSuperiorsUserId()!=null || userAuthorityName1==null){
            //根据群组id查询不是空的ValidTime
            if (user==null||user.getValidTime().compareTo(new Date())<0){
                String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(adminUserId), "超级管理");
                this.sendMessage(sendMessage,"管理员无效或不在有效期内!请联系: "+format);
                return;
            }
        }
        if (user==null && !userAuthorityName1.isOperation()){
            this.sendMessage(sendMessage,"不是操作员 请联系管理员!");
            return;
        }
        String[] split1 = message.getText().split(" ");
        String[] split2 = message.getText().split("\\+");
        String[] split3 = message.getText().split("-");
        //初始化
        Rate rate=rateService.getInitRate(userDTO.getGroupId());
        Status status=statusService.getInitStatus(userDTO.getGroupId(),userDTO.getGroupTitle());
        Account updateAccount = new Account();
        Issue issue=new Issue();
        updateAccount.setGroupId(userDTO.getGroupId());
        issue.setGroupId(userDTO.getGroupId());
        //没有用户名的情况下
        if (StringUtils.isEmpty(userDTO.getUsername()))userDTO.setUsername("");
        //查询最新数据用这个 dateOperator.selectIsRiqie
        List<Account> accountList=dateOperator.selectIsRiqie(sendMessage,status,userDTO.getGroupId());
        List<Issue> issueList=dateOperator.selectIsIssueRiqie(sendMessage,status,userDTO.getGroupId());
        //设置日切 如果日切时间没结束 第二次设置日切 也需要修改账单的日切时间
        dateOperator.isOver24HourCheck(message, sendMessage, userDTO, status,accountList,issueList);
        accountList =dateOperator.checkRiqie(sendMessage,status,accountList);
        issueList =dateOperator.checkRiqieIssue(sendMessage,status,issueList);
        if (status.isRiqie()){
            //如果日切时间小于等于当前时间
            if (status.getSetTime().compareTo(new Date())<=0) {
                this.sendMessage(sendMessage,"日切时间已更新，当前日切时间为 ：每天:"+status.getSetTime().getHours()+"时"+
                        status.getSetTime().getMinutes()+"分"+status.getSetTime().getSeconds()+"秒");
            }
        }
        //设置操作人员
        settingOperatorPerson.setHandle(split1, sendMessage,message.getText(),userDTO,user1,status);
        //设置费率/汇率
        ruzhangOperations.setRate(message,sendMessage,rate);
        //撤销入款
        ruzhangOperations.repeal(message,sendMessage,accountList,replyToText,userDTO,issueList);
        //删除今日数据/关闭日切/
        dateOperator.deleteTodayData(message,sendMessage,userDTO.getGroupId(),status,accountList,issueList);
        //入账操作
        ruzhangOperations.inHandle(split2,message.getText(),  updateAccount,  sendMessage, accountList, message,split3,
                rate,issue,issueList,userDTO,status);
        //删除操作人员
        settingOperatorPerson.deleteHandle(message.getText(),sendMessage);
        //通知功能
        notificationService.inform(message.getText(),sendMessage);
    }
    //拉取机器人进群处理
    public void onJinQunMessage(Update update){
        if (update.hasMyChatMember()) {
            ChatMemberUpdated chatMember = update.getMyChatMember();
            // 检查是否为机器人被添加到群组  left 移除
            if (chatMember.getNewChatMember().getStatus().equals("member")) {
                String chatId = chatMember.getChat().getId().toString();//群组id
                Long id = chatMember.getFrom().getId();//拉机器人的用户
                String userName = chatMember.getFrom().getUserName();
                UserAuthority userAuthority = new UserAuthority();
                userAuthority.setUserId(id+"");
                userAuthority.setOperation(true);
                userAuthority.setUsername(userName);
                userAuthority.setGroupId(chatId);
                UserAuthority repeat = userAuthorityService.repeat(userAuthority,chatId);
                if (repeat==null){
                    userAuthorityService.insertUserAuthority(userAuthority);
                }
                String message="<b>感谢权限人把我添加到贵群</b> ❤\uFE0F\n" +
                        "➖➖➖➖➖➖➖➖➖➖➖\n" +
                        "\n" +
                        "请根据需求先对机器人进行设置；\n" +
                        "费率：设置费率0\n" +
                        "汇率：设置汇率7\n" +
                        "注意：设置操作人 @*****\n" +
                        "\n" +
                        "<b>（本群默认开启日切北京时间中午12时）</b>\n" +
                        "<b>（如没有日切需求发送：关闭日切）</b>\n" +
                        "\n" +
                        "我也可以查询TRC20地址如下：\n" +
                        "输入: 查询TEtYFxxxxxxxxj8W9pC\n" +
                        "\n" +
                        "➖➖➖➖➖➖➖➖➖➖➖\n" +
                        "<b>详情使用说明请私聊我</b> @Evipbot";
                update.getMessage();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                this.tronAccountMessageTextHtml(sendMessage,chatId,message);
            }
        }
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
            userNew.setValidTime(new Date());
            userNew.setCreateTime(new Date());
            userService.insertUser(userNew);
        }
    }
    protected void tronAccountMessageText(SendMessage sendMessage,String chatId, String text) {
        sendMessage.setChatId(chatId);
//        sendMessage.setMessageId(messageId);
        sendMessage.setText(text);
        sendMessage.setParseMode("Markdown");
//        sendMessage.enableHtml(true);
        sendMessage.disableWebPagePreview();//禁用预览链接
        // 发送编辑消息请求
        try {
            execute(sendMessage); // 执行消息编辑命令
        } catch (TelegramApiException e) {
            e.printStackTrace(); // 处理异常
        }
    }
    protected void tronAccountMessageTextHtml(SendMessage sendMessage,String chatId, String text) {
        sendMessage.setChatId(chatId);
//        sendMessage.setMessageId(messageId);
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
//        sendMessage.enableHtml(true);
        sendMessage.disableWebPagePreview();//禁用预览链接
        // 发送编辑消息请求
        try {
            execute(sendMessage); // 执行消息编辑命令
        } catch (TelegramApiException e) {
            e.printStackTrace(); // 处理异常
        }
    }
    protected void editMessageText(EditMessageText editMessage,long chatId, int messageId, String text) {
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setText(text);
        editMessage.setParseMode("HTML");
        editMessage.enableHtml(true);
        // 发送编辑消息请求
        try {
            execute(editMessage); // 执行消息编辑命令
        } catch (TelegramApiException e) {
            e.printStackTrace(); // 处理异常
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


