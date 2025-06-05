package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.pojo.*;

import org.example.bot.accountBot.service.*;
import org.example.bot.accountBot.utils.BaseConstant;
import org.example.bot.accountBot.utils.ConstantMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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
    @Value("${botUserId}")
    protected String botUserId;
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
    private SettingOperatorPersonEnglish settingOperatorPersonEnglish;//setHandle
    @Autowired
    protected RuzhangOperations ruzhangOperations;    //入账和入账时发送的消息
    @Autowired
    protected NotificationService notificationService;
    @Autowired
    protected PaperPlaneBotSinglePerson paperPlaneBotSinglePerson;
    @Autowired
    private UserNormalService userNormalService;
    @Autowired
    private UserOperationService userOperationService;
    @Autowired
    private NowExchange nowExchange;//获取最新汇率
    @Autowired
    private GroupInfoSettingMapper groupInfoSettingMapper;//中英文切换
    @Autowired
    private GroupInfoSettingBotMessage groupInfoSettingBotMessage;//注意处理进群记录用户信息或者是 切换中英文
    @Autowired
    private PermissionUser permissionUser;//查询本群权限人

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
        Integer replayToMessageId=null;
        if (update != null && update.getMessage() != null && update.getMessage().getReplyToMessage() != null) {
            replyToText = update.getMessage().getReplyToMessage().getText();
            replayToMessageId = update.getMessage().getReplyToMessage().getMessageId();
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
        if (update.hasMessage() && update.getMessage().hasText()) this.BusinessHandler(message,sendMessage,replyToText,replayToMessageId,userDTO,update);
    }
    public void BusinessHandler(Message message,SendMessage sendMessage, String replyToText,Integer replayToMessageId, UserDTO userDTO,Update update) {
        GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getGroupId()));
        //切换中英文初始化 或更新
        if (message.getChat().isGroupChat()||message.getChat().isSuperGroupChat()){
            groupInfoSetting=groupInfoSettingBotMessage.getGroupOrCreate(userDTO.getText(), Long.valueOf(userDTO.getGroupId()));
        } else if (message.getChat().isUserChat()) {
            groupInfoSetting= groupInfoSettingBotMessage.getGroupOrCreate(userDTO.getText(), Long.valueOf(userDTO.getUserId()));
        }
        if (userDTO.getText().equals("/detail"+"@"+username)||userDTO.getText().equals("/detail")){
            String sc="<strong>中英文对应如下以下命令包括括号里的都可识别(The Chinese and English correspondence is as follows)</strong>\n";
            String allCommandsDetail = new ConstantMap().getAllCommandsDetail();
            SendMessage sendMessageDetail = new SendMessage();
            if (message.getChat().isUserChat()){
                sendMessageDetail.setChatId(userDTO.getUserId());
            }else {
                sendMessageDetail.setChatId(userDTO.getGroupId());
            }
            sendMessageDetail.setText(sc+allCommandsDetail);
            this.sendMessage(sendMessageDetail);
        }
        //私聊的机器人  处理个人消息
        if (message.getChat().isUserChat()){
            paperPlaneBotSinglePerson.handleTronAccountMessage(sendMessage,update,userDTO);
            paperPlaneBotSinglePerson.handleNonGroupMessage(message,sendMessage,userDTO);
            return;
        }
        if(userDTO.getText().startsWith("查询") || userDTO.getText().startsWith("CX") || userDTO.getText().startsWith("Query")){
            nowExchange.Query(sendMessage,update);
        }
        User userTemp = userService.findByUserId(userDTO.getUserId());
        User userTemp2 = userService.findByUsername(userDTO.getUsername());
        //改成sql查询username 和userId 不要全查了 并且isNormal是false
        this.setIsAdminUser(sendMessage,userDTO,userTemp,userTemp2);
        User user1 = null;
        if (userTemp!=null){
            user1=userTemp;
        }else if (userTemp2!=null){
            user1=userTemp2;
        }
        if (userDTO.getText().equals("z0")||userDTO.getText().equals("Z0")){
            Rate rate=rateService.getInitRate(userDTO.getGroupId());
            nowExchange.getNowExchange(sendMessage,userDTO,rate);
        }
        //计算器功能
        utils.counter(message,sendMessage);
        notificationService.initNotification(userDTO);
        UserNormal userNormalTempAdmin =userNormalService.selectByGroupId(userDTO.getGroupId());//超级管理
        if (message.getText().equals("权限人") || message.getText().equals("管理员")
                || message.getText().toLowerCase().equals("authorized person")||  message.getText().toLowerCase().equals("admin")){
            permissionUser.getPermissionUser(sendMessage,userDTO,user1,userNormalTempAdmin);
        }
        if (message.getText().charAt(0)!='+' && message.getText().charAt(0)!='-' &&
                (!BaseConstant.getMessageContentIsContain(message.getText()) && !BaseConstant.getMessageContentIsContainEnglish(message.getText())
                        && !BaseConstant.getMessageContentIsContainEnglish2(message.getText()))) {
            return ;
        }
        UserOperation userOperation = userOperationService.selectByUserAndGroupId(userDTO.getUserId(), userDTO.getGroupId());
        if (userOperation==null){
            userOperation= userOperationService.selectByUserName(userDTO.getUsername(), userDTO.getGroupId());
        }
        if (userOperation==null || !userOperation.isOperation()){
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(userNormalTempAdmin.getUserId()), "权限人");
            this.sendMessage(sendMessage,"没有使用权限，请联系本群权限人 "+format);
            return;
        }else if(userOperation.isOperation()){
            //如果是本群权限人
            if (userNormalTempAdmin.getUserId().equals(userDTO.getUserId())){
                UserNormal userNormal = userNormalService.selectByUserId(userOperation.getUserId(), userDTO.getGroupId());
                if (userNormal==null || !userNormal.isAdmin() ){
                    String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(adminUserId), "管理员");
                    this.sendMessage(sendMessage,"您在本群不是管理!请联系: "+format);
                    return;
                }else {
                    User byUserId = userService.findByUserId(userDTO.getUserId());
                    //如果有效期没过期
                    if (byUserId.getValidTime()==null){
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(botUserId), "记账机器人");
                        this.sendMessage(sendMessage,"您现在没有使用权限,请私聊机器人 @"+format+" .点击获取个人信息获取权限.");
                        return;
                    }else if (new Date().compareTo(byUserId.getValidTime())>=0){
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(adminUserId), "管理员");
                        this.sendMessage(sendMessage,"您的使用期限已到期,请私聊管理员 @"+format);
                        return;
                    }
                }
            }else {
                User userAuth = userService.findByUserId(userOperation.getAdminUserId());
                //如果有效期没过期
                if (userAuth.getValidTime()==null){
                    String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(botUserId), "记账机器人");
                    this.sendMessage(sendMessage,"本群权限人现在没有使用权限,请私聊机器人 @"+format+" .点击获取个人信息获取权限.");
                    return;
                }else if (new Date().compareTo(userAuth.getValidTime())>=0){
                    String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(adminUserId), "管理员");
                    this.sendMessage(sendMessage,"本群权限人的使用期限已到期,请私聊管理员 @"+format);
                    return;
                }
            }
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
        //查询最新数据用这个 dateOperator.selectIsRiqie dateOperator.checkRiqie
        dateOperator.checkRiqie(sendMessage,status);//检查日切时间  如果时间没到往前查24小时数据 如果时间到了 往后查大于当前时间的数据
        List<Account> accountList=dateOperator.selectIsRiqie(sendMessage,status,userDTO.getGroupId());
        List<Issue> issueList=dateOperator.selectIsIssueRiqie(sendMessage,status,userDTO.getGroupId());
        //设置日切 如果日切时间没结束 第二次设置日切 也需要修改账单的日切时间
        dateOperator.isOver24HourCheck(message, sendMessage, userDTO, status,accountList,issueList);
        //设置操作人员
        settingOperatorPerson.setHandle(split1, sendMessage,message.getText(),userDTO,user1,status,groupInfoSetting);
        settingOperatorPersonEnglish.setHandle(sendMessage,message.getText(),userDTO,user1,status,groupInfoSetting);
        //设置费率/汇率
        ruzhangOperations.setRate(message,sendMessage,rate);
        //撤销入款
        ruzhangOperations.repeal(message,sendMessage,accountList,replyToText,replayToMessageId,userDTO,issueList);
        ruzhangOperations.repealEn(message,sendMessage,accountList,replyToText,replayToMessageId,userDTO,issueList);
        //删除今日数据/关闭日切/
        dateOperator.deleteTodayData(message,sendMessage,userDTO.getGroupId(),status,accountList,issueList);
        //入账操作
        ruzhangOperations.inHandle(split2,message.getText(),  updateAccount,  sendMessage, accountList, message,split3,
                rate,issue,issueList,userDTO,status,groupInfoSetting);
        //删除操作人员
        settingOperatorPerson.deleteHandle(message.getText(),sendMessage,userDTO);
        settingOperatorPersonEnglish.deleteHandleEnglish(message.getText(),sendMessage,userDTO);
        //通知功能
        notificationService.inform(message.getText(),sendMessage);
    }


    //拉取机器人进群处理
    public void onJinQunMessage(Update update){
        if (update.hasMyChatMember()) { //获取个人信息和授权的时候才是admin
            ChatMemberUpdated chatMember = update.getMyChatMember();
            if (chatMember.getNewChatMember().getStatus().equals("administrator")){
                String message="<b>感谢您把我设为贵群管理</b> ❤\uFE0F\n" +
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
                        "命令详情/detail"+
                        "\n" +
                        "➖➖➖➖➖➖➖➖➖➖➖\n" +
                        "<b>详情使用说明请私聊我</b> @"+this.getBotUsername();
                update.getMessage();
                SendMessage sendMessage = new SendMessage();
                String chatId = chatMember.getChat().getId().toString();//群组id
                sendMessage.setChatId(chatId);
                this.tronAccountMessageTextHtml(sendMessage,chatId,message);
            }else if (chatMember.getNewChatMember().getStatus().equals("member") || chatMember.getNewChatMember().getStatus().equals("left")) {
                String chatId = chatMember.getChat().getId().toString();//群组id
                Long id = chatMember.getFrom().getId();//拉机器人的用户
                String username = chatMember.getFrom().getUserName();
                String firstName = chatMember.getFrom().getFirstName();
                String lastName = chatMember.getFrom().getLastName();
                User byUser = userService.findByUserId(id + "");
                if (byUser==null){
                    byUser = new User();
                    byUser.setUserId(id+"");
                    byUser.setSuperAdmin(false);
                    byUser.setUsername(username);
                    byUser.setLastName(lastName);
                    byUser.setCreateTime(new Date());
                    byUser.setFirstName(firstName);
                    userService.insertUser(byUser);
                    UserNormal userNormal = userNormalService.selectByUserAndGroupId(id + "", chatId);
                    if (userNormal==null){
                        userNormal = new UserNormal();
                        userNormal.setAdmin(true);
                        userNormal.setGroupId(chatId);
                        userNormal.setUserId(id+"");
                        userNormal.setCreateTime(new Date());
                        userNormal.setUsername(username);
                        userNormalService.insertUserNormal(userNormal);
                    }else {
                        userNormal.setAdmin(true);
                        userNormalService.update(userNormal);
                    }
                    UserOperation userOperation = userOperationService.selectByUserAndGroupId(id + "", chatId);
                    if (userOperation==null){
                        userOperation=new UserOperation();
                        userOperation.setUserId(id+"");
                        userOperation.setOperation(true);
                        userOperation.setAdminUserId(id+"");
                        userOperation.setGroupId(chatId);
                        userOperation.setUsername(username);
                        userOperation.setCreateTime(new Date());
                        userOperationService.insertUserOperation(userOperation);
                    }else {
                        userOperation.setOperation(true);
                        userOperationService.update(userOperation);
                    }
                }else if (byUser!=null){
                        UserNormal userNormal = userNormalService.selectByUserAndGroupId(id + "", chatId);
                        if (userNormal==null){
                            userNormal = new UserNormal();
                            userNormal.setAdmin(true);
                            userNormal.setGroupId(chatId);
                            userNormal.setUserId(id+"");
                            userNormal.setUsername(username);
                            userNormal.setCreateTime(new Date());
                            userNormalService.insertUserNormal(userNormal);
                        }else {
                            userNormal.setAdmin(true);
                            userNormalService.update(userNormal);
                        }
                        UserOperation userOperation = userOperationService.selectByUserAndGroupId(id + "", chatId);
                        if (userOperation==null){
                            userOperation=new UserOperation();
                            userOperation.setUserId(id+"");
                            userOperation.setOperation(true);
                            userOperation.setAdminUserId(id+"");
                            userOperation.setGroupId(chatId);
                            userOperation.setUsername(username);
                            userOperation.setCreateTime(new Date());
                            userOperationService.insertUserOperation(userOperation);
                        }else {
                            userOperation.setOperation(true);
                            userOperationService.update(userOperation);
                        }
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
                        "命令详情/detail"+
                        "\n" +
                        "➖➖➖➖➖➖➖➖➖➖➖\n" +
                        "<b>详情使用说明请私聊我</b> @"+this.getBotUsername();
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
                    user.setUsername(username);
                    sendMessage(sendMessage,message);
                }
                if (!name.equals(nameDTO)){
                    String message="⚠\uFE0F用户昵称变更通知⚠\uFE0F\n"
                            +"用户:"+name+"\n"
                            +"⬇\uFE0F\n"
                            +nameDTO;
                    user.setLastName(lastNameDTO);
                    user.setFirstName(firstNameDTO);
                    sendMessage(sendMessage,message);
                }
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
            userNew.setCreateTime(new Date());
            userService.insertUser(userNew);
        }
    }
    protected void tronAccountMessageText(SendMessage sendMessage,String chatId, String text) {
        sendMessage.setChatId(chatId);
//        sendMessage.setMessageId(messageId);
        text = groupInfoSettingBotMessage.handler(chatId,text);
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
        text = groupInfoSettingBotMessage.handler(chatId,text);
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        sendMessage.enableHtml(true);
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
        text = groupInfoSettingBotMessage.handler(String.valueOf(chatId),text);
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
    public Integer sendMessage(SendMessage sendMessage,String text) {
        text = groupInfoSettingBotMessage.handler(sendMessage.getChatId(),text);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("HTML");
//        sendMessage.enableMarkdown(true);
        try {
            log.info("发送消息:{}", text);
            Message execute = execute(sendMessage);
            return execute.getMessageId();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }

    public void sendMessage(SendMessage sendMessage) {
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("HTML");
//        sendMessage.enableMarkdown(true);
        try {
            execute(sendMessage);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}


