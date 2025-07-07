package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.GroupInfoSettingMapper;
import org.example.bot.accountBot.mapper.GroupInnerUserMapper;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.*;

import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.*;
import org.example.bot.accountBot.utils.BaseConstant;
import org.example.bot.accountBot.utils.StyleText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

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
    @Value("${toAdminUserId}")
    protected String toAdminUserId;
    @Value("${threeAdminUserId}")
    protected String threeAdminUserId;
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
    protected DownAddress downAddress;//群内下发地址
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
    @Autowired
    private StatusMapper statusMapper;
    @Autowired
    private GroupInnerUserMapper groupInnerUserMapper;
    @Override
    public String getBotUsername() {
        return username;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }
    public AccountBot() {
        List<String> list = Arrays.asList(
                "message",                  // 普通消息
                "edited_message",           // 编辑过的消息
                "channel_post",             // 频道消息
                "edited_channel_post",      // 编辑过的频道消息
                "inline_query",             // 内联查询
                "chosen_inline_result",     // 选择了内联结果
                "callback_query",           // 按钮回调
                "shipping_query",           // 发货信息（支付相关）
                "pre_checkout_query",       // 支付前确认（支付相关）
                "poll",                     // 投票事件
                "poll_answer",              // 用户提交投票答案
                "my_chat_member",           // ✅ 机器人自身在群中的权限变化
                "chat_member",              // 群成员加入/退出/变动
                "chat_join_request",        // 用户申请加入私密群
                "message_reaction",         // 表情反应（用户加了 emoji）
                "message_reaction_count"    // 表情反应数量变化
        );
        this.getOptions().setAllowedUpdates(list);
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
        if (update.hasMessage() && update.getMessage().hasText()) {
            this.BusinessHandler(message, sendMessage, replyToText, replayToMessageId, userDTO, update);
        }else  if (update.hasMessage() && update.getMessage().hasPhoto()){
            String caption = update.getMessage().getCaption();
            userDTO.setText(caption);
            this.BusinessHandler(message,sendMessage,replyToText,replayToMessageId,userDTO,update);
        }else  if (update.hasMessage() && update.getMessage().hasVideo()){
            String caption = update.getMessage().getCaption();
            userDTO.setText(caption);
            this.BusinessHandler(message,sendMessage,replyToText,replayToMessageId,userDTO,update);
        }else if (update.hasMessage() && update.getMessage().getChat().isGroupChat()||message.getChat().isSuperGroupChat()){
            Status status=statusService.getInitStatus(update,userDTO.getGroupId(),userDTO.getGroupTitle());
            // 判断是否是带有 caption 的图片或视频消息
            if ((message.hasPhoto() || message.hasVideo()) && message.getCaption() != null && !message.getCaption().isEmpty()) {
                String caption = message.getCaption();
                boolean b = downAddress.validAddress(userDTO.getText(), sendMessage, status);//验证地址
                if (! b){
                }
            }
        }
    }
    public void insertInnerUser(String type, String groupId, String userId,
                                String username, String firstName, String lastName) {
        QueryWrapper<GroupInnerUser> queryWrapper = new QueryWrapper<>();
        if (groupId!=null){
            queryWrapper.eq("group_id", groupId);
        }else {
            queryWrapper.eq("type", type);
        }
        queryWrapper.eq("user_id", userId);
        GroupInnerUser existing = groupInnerUserMapper.selectOne(queryWrapper);
        if (existing == null) {
            GroupInnerUser newUser = new GroupInnerUser();
            newUser.setType(type);
            newUser.setGroupId(groupId);
            newUser.setUserId(userId);
            newUser.setUsername(username);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setLastTime(new Date());
            groupInnerUserMapper.insert(newUser);
        } else {
            existing.setType(type);
            existing.setUsername(username);
            existing.setFirstName(firstName);
            existing.setLastName(lastName);
            existing.setLastTime(new Date());
            groupInnerUserMapper.update(existing, queryWrapper);
        }
    }

    public void BusinessHandler(Message message,SendMessage sendMessage, String replyToText,Integer replayToMessageId, UserDTO userDTO,Update update) {
        GroupInfoSetting groupInfoSetting = null;
        if (userDTO.getGroupId()!=null){
            groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", userDTO.getGroupId()));
            if (groupInfoSetting==null){
                groupInfoSetting=new GroupInfoSetting();
                groupInfoSetting.setGroupId(Long.valueOf(userDTO.getGroupId()));
                groupInfoSettingMapper.insert(groupInfoSetting);
            }
            if (userDTO.getText()!=null && (userDTO.getText().equals("切换中文") || userDTO.getText().equals("切换英文")
                    ||  userDTO.getText().toLowerCase().equals("switch to chinese")||  userDTO.getText().toLowerCase().equals("switch to english"))){
                groupInfoSettingBotMessage.switchEn(userDTO.getText().toLowerCase(), Long.valueOf(userDTO.getGroupId()),groupInfoSetting);
                return;
            }
            this.insertInnerUser(userDTO.getGroupTitle(),userDTO.getGroupId(),userDTO.getUserId(),userDTO.getUsername(),userDTO.getFirstName(),userDTO.getLastName());
        }
        if (userDTO.getText()!=null && (userDTO.getText().equals("/detail"+"@"+username)||userDTO.getText().equals("/detail"))){
            SendMessage sendMessageDetail = new SendMessage();
            if (message.getChat().isUserChat()){
                sendMessageDetail.setChatId(userDTO.getUserId());
            }else {
                sendMessageDetail.setChatId(userDTO.getGroupId());
            }
            sendMessageDetail.setText(this.getDetail());
            this.sendMessage(sendMessageDetail);
        }
        //私聊的机器人  处理个人消息
        if (message.getChat().isUserChat()){
            this.insertInnerUser("私聊",userDTO.getGroupId(),userDTO.getUserId(),userDTO.getUsername(),userDTO.getFirstName(),userDTO.getLastName());
            paperPlaneBotSinglePerson.handleTronAccountMessage(sendMessage,update,userDTO);
            paperPlaneBotSinglePerson.handlerPrivateUser(update,message,sendMessage,userDTO);//处理私聊消息
            paperPlaneBotSinglePerson.handleNonGroupMessage(message,sendMessage,userDTO);
            return;
        }
        if (userDTO.getText()==null){
            return;
        }
        if(userDTO.getText().startsWith("查询") || userDTO.getText().startsWith("Query")){
            nowExchange.Query(sendMessage,update,groupInfoSetting);
        }
        User userTemp = userService.findByUserId(userDTO.getUserId());
        User userTemp2 = userService.findByUsername(userDTO.getUsername());
        //改成sql查询username 和userId 不要全查了 并且isNormal是false
        this.setIsAdminUser(sendMessage,userDTO,userTemp,userTemp2,groupInfoSetting);
        User user1 = null;
        if (userTemp!=null){
            user1=userTemp;
        }else if (userTemp2!=null){
            user1=userTemp2;
        }
        if (userDTO.getText().equals("z0")||userDTO.getText().equals("Z0")){
            Rate rate=rateService.getInitRate(userDTO.getGroupId());
            nowExchange.getNowExchange(sendMessage,userDTO,rate, groupInfoSetting);
        }
        //计算器功能
        utils.counter(userDTO.getText(),sendMessage);
        notificationService.initNotification(userDTO);
        Status status=statusService.getInitStatus(update,userDTO.getGroupId(),userDTO.getGroupTitle());
        downAddress.viewAddress(userDTO.getText(),sendMessage,status);
        if (downAddress.isTronAddress(userDTO.getText())) {
            downAddress.validAddress(userDTO.getText(), sendMessage, status);//验证地址
            return;
        }
        UserNormal userNormalTempAdmin =userNormalService.selectByGroupId(userDTO.getGroupId());//超级管理
        if (userDTO.getText().equals("权限人") || userDTO.getText().equals("管理员")
                || userDTO.getText().toLowerCase().equals("authorized person")||  userDTO.getText().toLowerCase().equals("admin")){
            permissionUser.getPermissionUser(sendMessage,userDTO,user1,userNormalTempAdmin,groupInfoSetting);
        }
        UserOperation userOperation = userOperationService.selectByUserAndGroupId(userDTO.getUserId(), userDTO.getGroupId());
        if (userOperation==null){
            userOperation= userOperationService.selectByUserName(userDTO.getUsername(), userDTO.getGroupId());
        }
        if (userDTO.getText().charAt(0)!='+' && userDTO.getText().charAt(0)!='-' &&
                (!BaseConstant.getMessageContentIsContain(userDTO.getText())
                        && !BaseConstant.getMessageContentIsContainEnglish2(userDTO.getText()))) {
            return ;
        }
        if ((userDTO.getText().equals("取消") && replyToText == null) ||
                (userDTO.getText().toLowerCase().equals("cancel") && replyToText == null)){
            return;
        }
        if (userOperation==null || !userOperation.isOperation()){
//            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(userNormalTempAdmin.getUserId()), "权限人");
//            this.sendMessage(sendMessage,"没有使用权限，请联系本群权限人 "+format);
            return;
        }else if(userOperation.isOperation()){
            //如果是本群权限人
            if (userNormalTempAdmin.getUserId().equals(userDTO.getUserId())){
                UserNormal userNormal = userNormalService.selectByUserId(userOperation.getUserId(), userDTO.getGroupId());
                if (userNormal==null || !userNormal.isAdmin() ){
                    if (groupInfoSetting.getEnglish()){
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(threeAdminUserId), "管理员");
                        this.sendMessage(sendMessage,"您在本群不是管理!请联系: "+format);
                    }else {
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(threeAdminUserId), "admin");
                        this.sendMessage(sendMessage,"You are not an administrator in this group! Please contact: "+format);
                    }
                    return;
                }else {
                    User byUserId = userService.findByUserId(userDTO.getUserId());
                    //如果有效期没过期
                    if (byUserId.getValidTime()==null){
                        if (groupInfoSetting.getEnglish()){
                            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(botUserId), "记账机器人");
                            this.sendMessage(sendMessage,"您现在没有使用权限,请私聊机器人 @"+format+" .点击获取个人信息获取权限.");
                        }else{
                            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(botUserId), "Accounting Robot");
                            this.sendMessage(sendMessage,"You do not have permission to use it now, please chat with the robot privately @"+format+" .Click to obtain personal information access permission.");
                        }
                        return;
                    }else if (new Date().compareTo(byUserId.getValidTime())>=0){
                        if (groupInfoSetting.getEnglish()){
                            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(threeAdminUserId), "管理员");
                            this.sendMessage(sendMessage,"您的使用期限已到期,请私聊管理员 @"+format);
                        }else{
                            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(threeAdminUserId), "admin");
                            this.sendMessage(sendMessage,"Your usage period has expired, please chat with the administrator @"+format);
                        }
                        return;
                    }
                }
            }else {
                User userAuth = userService.findByUserId(userOperation.getAdminUserId());
                //如果有效期没过期
                if (userAuth.getValidTime()==null){
                    if (groupInfoSetting.getEnglish()){
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(botUserId), "记账机器人");
                        this.sendMessage(sendMessage,"本群权限人现在没有使用权限,请私聊机器人 @"+format+" .点击获取个人信息获取权限.");
                    }else{
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(botUserId), "Accounting Robot");
                        this.sendMessage(sendMessage,"The authorized person in this group does not have permission to use it now. Please privately chat with the robot @"+format+" .Click to obtain personal information access permission.");
                    }
                    return;
                }else if (new Date().compareTo(userAuth.getValidTime())>=0){
                    if (groupInfoSetting.getEnglish()){
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(threeAdminUserId), "管理员");
                        this.sendMessage(sendMessage,"本群权限人的使用期限已到期,请私聊管理员 @"+format);
                    }else{
                        String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(threeAdminUserId), "admin");
                        this.sendMessage(sendMessage,"The period of use for this group has expired. Please privately chat with the administrator. @"+format);
                    }
                    return;
                }
            }
        }
        String[] split1 = userDTO.getText().split(" ");
        String[] split2 = userDTO.getText().split("\\+");
        String[] split3 = userDTO.getText().split("-");
        //初始化
        Rate rate=rateService.getInitRate(userDTO.getGroupId());
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
        dateOperator.isOver24HourCheck( sendMessage, userDTO, status,accountList,issueList);
        //设置操作人员
        settingOperatorPerson.setHandle(split1, sendMessage,userDTO.getText(),userDTO,user1,status,groupInfoSetting,userNormalTempAdmin,update);
        settingOperatorPersonEnglish.setHandle(sendMessage,userDTO.getText(),userDTO,user1,status,groupInfoSetting,userNormalTempAdmin,update);
        downAddress.downAddress(sendMessage,userDTO,status,userNormalTempAdmin,userOperation,groupInfoSetting);//设置下发地址
        //设置费率/汇率
        ruzhangOperations.setRate(userDTO.getText(),sendMessage,rate);
        //撤销入款
        ruzhangOperations.repeal(sendMessage,accountList,replyToText,replayToMessageId,userDTO,issueList,status);
        ruzhangOperations.repealEn(sendMessage,accountList,replyToText,replayToMessageId,userDTO,issueList,status);
        //识别P是否手动添加 是(true)
        ruzhangOperations.pHandle(userDTO, status,updateAccount,issue);
        //删除今日数据/关闭日切/
        dateOperator.deleteTodayData(userDTO.getText(),sendMessage,userDTO.getGroupId(),status,rate,groupInfoSetting);
        //入账操作
        ruzhangOperations.inHandle(split2,userDTO.getText(),  updateAccount,  sendMessage, accountList, message,split3,
                rate,issue,issueList,userDTO,status,groupInfoSetting);
        //删除操作人员
        settingOperatorPerson.deleteHandle(userDTO.getText(),sendMessage,userDTO,userNormalTempAdmin);
        settingOperatorPersonEnglish.deleteHandleEnglish(userDTO.getText(),sendMessage,userDTO,userNormalTempAdmin);
        //通知功能
        notificationService.inform(userDTO.getText(),sendMessage);
    }


    //拉取机器人进群处理
    public void onJinQunMessage(Update update){
        if (update.hasMyChatMember()) { //获取个人信息和授权的时候才是admin
            ChatMemberUpdated chatMember = update.getMyChatMember();
            if (chatMember.getNewChatMember().getStatus().equals("administrator")){
                SendMessage sendMessage = new SendMessage();
                String chatId = chatMember.getChat().getId().toString();//群组id
                sendMessage.setChatId(chatId);
                GroupInfoSetting groupInfoSetting=groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", chatId));
                if (groupInfoSetting==null){
                    groupInfoSetting=new GroupInfoSetting();
                    groupInfoSetting.setGroupId(Long.valueOf(chatId));
                    groupInfoSettingMapper.insert(groupInfoSetting);
                }
                String message;
                if (groupInfoSetting.getEnglish()){
                    message="<b>感谢您把我设为贵群管理</b> ❤\uFE0F\n" +
                            "➖➖➖➖➖➖➖➖➖➖➖";
                }else{
                    message="<b>Thank you for setting me as your group administrator</b> ❤\uFE0F\n" +
                            "➖➖➖➖➖➖➖➖➖➖➖";
                }
                this.tronAccountMessageTextHtml(sendMessage,chatId,message);
            }else if (chatMember.getNewChatMember().getStatus().equals("member")) {
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
                GroupInfoSetting groupInfoSetting = groupInfoSettingMapper.selectOne(new QueryWrapper<GroupInfoSetting>().eq("group_id", chatId));
                if (groupInfoSetting==null){
                    groupInfoSetting = new GroupInfoSetting();
                    groupInfoSetting.setGroupId(Long.valueOf(chatId));
//                    groupInfoSetting.setEnglish(true);///
                    groupInfoSetting.setEnglish(false);//切换中文要修改111
                    groupInfoSettingMapper.insert(groupInfoSetting);
                }else {
//                    groupInfoSetting.setEnglish(true);
                    groupInfoSetting.setEnglish(false);///
                    groupInfoSettingMapper.updateById(groupInfoSetting);
                }
                String message;
                if (groupInfoSetting.getEnglish()){
                    message="<b>感谢权限人把我添加到贵群</b> ❤\uFE0F\n" +
                            "➖➖➖➖➖➖➖➖➖➖➖";
                }else{
                    message="<b>Thanks to the authority for adding me to your group</b> ❤\uFE0F\n" +
                            "➖➖➖➖➖➖➖➖➖➖➖";
                }
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                this.tronAccountMessageTextHtml(sendMessage,chatId,message);
            } else if ( chatMember.getNewChatMember().getStatus().equals("left") || chatMember.getNewChatMember().getStatus().equals("kicked")) {
                String chatId = chatMember.getChat().getId().toString();
                statusMapper.delete(new QueryWrapper<Status>().eq("group_id", chatId));
            }
        }
        //检测群内有用户退群或者被踢出群
        if (update.hasChatMember()) {
            ChatMemberUpdated chatMember = update.getChatMember();
            ChatMember oldMember = chatMember.getOldChatMember();
            ChatMember newMember = chatMember.getNewChatMember();
            String chatId = chatMember.getChat().getId().toString();
            String userId = chatMember.getFrom().getId() + "";
            String username = chatMember.getFrom().getUserName();
            String firstName = chatMember.getFrom().getFirstName();
            String lastName = chatMember.getFrom().getLastName();
            if ("left".equals(newMember.getStatus()) || "kicked".equals(newMember.getStatus())) {
                this.insertInnerUser("退群", chatId, userId, username, firstName, lastName);
            }
            if ("member".equals(newMember.getStatus())){
                this.insertInnerUser(chatMember.getChat().getTitle(), chatId, userId, username, firstName, lastName);
            }
        }
    }
    public String getDetail(){
        return "1\uFE0F⃣增加机器人进群。群右上角--Add member-输入 @"+this.getBotUsername()+"\n" +
                " Add robots to the group. In the upper right corner of the group--Add member-enter @"+this.getBotUsername()+"\n" +
                "\n" +
                " 2\uFE0F⃣ 设置操作人 @AAA\n" +
                " Set operator @AAA\n" +
                "删除操作人 @AAA\n" +
                "Delete operator @AAA\n" +
                "先打空格在打@，会弹出选择更方便\n" +
                "Type space first and then @, a pop-up will appear for easier selection\n" +
                "显示操作人（Show operator）\n" +
                "\n" +
                "  如果对方没有设置用户名，可以回复对方信息并发送“设置操作员”或“删除操作员”来新增或删除操作员\n" +
                "If the other party has not set a user name, you can reply to the other party's message and send \"Set operator\" or \"Delete operator\" to add or delete an operator.\n" +
                "\n" +
                "3\uFE0F⃣输入”设置费率X.X%“\n" +
                "Enter \"set rate X.X%\"\n" +
                "输入”设置汇率X.X%“\n" +
                "Enter \"Set exchange rate  X.X%\"\n" +
                "\n" +
                " 4\uFE0F⃣ 入款指令；+     修正；D-\n" +
                "Deposit instruction; + correction; D-\n" +
                " 下发指令；-       修正；T-\n" +
                "Withdrawal instruction;- Amendment;T-\n" +
                "\n" +
                " 如果输入错误，可以用 入款-XXX  或 下发-XXX，来修正\n" +
                "If you make an input error, you can use Deposit-XXX or Issue-XXX to correct it.\n" +
                "“撤销入款”：可撤销最近一条入款记录\n" +
                "“Cancel deposit”: You can cancel the most recent deposit record\n" +
                "“撤销下发”：可撤销最近一条下发记录\n" +
                "“Undo delivery”: You can revoke the most recent delivery record.\n" +
                "\n" +
                " 5\uFE0F⃣“OTC”, “币价”，“Z0”,即可查询OKex官方最新OTC交易价格,底部可查询微信支付宝和银行卡价格.\n" +
                "\"OTC\", \"Coin Price\" and \"Z0\" can view the latest OTC transaction prices on OKex. At the bottom, you can view the prices of WeChat, Alipay and bank cards.\n" +
                " 6\uFE0F⃣账单（bill）\n" +
                "删除账单（删除今日账单）\n" +
                "Delete bill (delete today's bill)\n" +
                "删除全部账单（删除历史账单谨慎使用）\n" +
                "Delete all bills (be careful when deleting historical bills)\n" +
                "\n" +
                " 7\uFE0F⃣计算器 （calculator）\n" +
                "100+100            1000*5\n" +
                "1000/5               1000-5\n" +
                "8\uFE0F⃣TRC20地址信息查询（TRC20 address information query）\n" +
                "查询TEtYFNQCaDWZXsjoMwxxJ9tymhXx*****\n" +
                "QueryTEtYFNQCaDWZXsjoMwxxJ9tymhXx*****\n" +
                "\n" +
                "9\uFE0F⃣“设置日切12”：设置每日截止时间+（0~23之间的整数）（可修改各组每日截止时间，默认为中国北京时间12:00）\n" +
                "\"Set daily cut-off 12\": Set the daily cut-off time + (an integer between 0 and 23) (the daily cut-off time of each group can be modified, the default is 12:00 Beijing time, China)\n" +
                "\uD83D\uDD1F其他功能（Other functions）\n" +
                "通知（notify）\n" +
                "显示操作人名称（Show operator name）\n" +
                "隐藏操作人名称（Hide operator name）\n" +
                "显示回复人名称（Show replyer name）\n" +
                "隐藏回复人名称（Hide reply name）\n" +
                "隐藏名称（Hide name）\n" +
                "切换中文（Switch to Chinese）\n" +
                "切换英文（Switch to English）\n" +
                "显示分类（Show categories）\n" +
                "隐藏分类（Hide categories）\n" +
                "设置手续费10（Setup fee 10）0为关闭（0 is off）\n" +
                "隐藏手续费（Hidden fees）\n" +
                "显示手续费（Show handling fee）\n" +
                "显示1条（Show 1 item）\n" +
                "显示3条（Show 3 item）\n" +
                "显示5条（Show 5 item）\n" +
                "显示金额（Display amount）\n" +
                "显示usdt（show usdt）\n" +
                "显示全部（Show all）\n" +
                "显示明细（Show details）\n" +
                "隐藏明细（hide details）\n" +
                "“设置下发地址“：设置下发地+下发地址信息\n" +
                "“查看下发地址“ ： 群内输入关键字    下发地址\n" +
                "P余额功能+指令，P100{增加余额，只显示在独立分类}\n" +
                "After-sales customer service: @vipkefu @yaokevipBot\n" +
                "售后客服： @vipkefu @yaokevipBot";
    }


    //判断消息是否是普通用户发送的消息 如果是就保存
    public void setIsAdminUser(SendMessage sendMessage,UserDTO userDTO,User user,User byUsername,GroupInfoSetting groupInfoSetting){
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
                    String message;
                    if (groupInfoSetting.getEnglish()){
                        message="⚠\uFE0F用户名变更通知⚠\uFE0F\n"
                                +"用户:"+user.getUsername()+"\n"
                                +"⬇\uFE0F\n"
                                +userDTO.getUsername();
                    }else{
                        message="⚠\uFE0FUsername change notification⚠\uFE0F\n"
                                +"用户:"+user.getUsername()+"\n"
                                +"⬇\uFE0F\n"
                                +userDTO.getUsername();
                    }
                    user.setUsername(username);
                    sendMessage(sendMessage,message);
                }
                if (!name.equals(nameDTO)){
                    String message;
                    if (groupInfoSetting.getEnglish()){
                        message="⚠\uFE0F用户昵称变更通知⚠\uFE0F\n"
                                +"用户:"+name+"\n"
                                +"⬇\uFE0F\n"
                                +nameDTO;
                    }else{
                        message="⚠\uFE0FUser nickname change notification⚠\uFE0F\n"
                                +"用户:"+name+"\n"
                                +"⬇\uFE0F\n"
                                +nameDTO;
                    }
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
    public void sendMessageReplay(SendMessage sendMessage,boolean isReplay,int replyMessageId) {
        sendMessage.enableHtml(true);
        sendMessage.setParseMode("HTML");
//        sendMessage.enableMarkdown(true);
        try {
            if (isReplay){
                sendMessage.setReplyToMessageId(replyMessageId);
            }
            execute(sendMessage);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    //检查消息是否来自管理员
    public ChatMember findStatus(String chatId) {
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(Long.parseLong(chatId));
        getChatMember.setUserId(Long.valueOf(botUserId));
        try {
            ChatMember chatMember = execute(getChatMember);
            return chatMember; // 返回 true 表示被踢出
        } catch (TelegramApiException e) {
            System.err.println("检查用户状态失败: " + e.getMessage());
            return null;
        }
    }
    //退群操作
    public void leaveChat(String chatId) {
        try {
            // 创建一个 LeaveChat 对象
            LeaveChat leaveChat = new LeaveChat();
            leaveChat.setChatId(chatId);
            // 执行退群操作
            execute(leaveChat);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
//            throw new RuntimeException(e);
        }
    }


    public List<Integer> sendMediaGroup(SendMediaGroup sendMediaGroup) {
        try {
            List<Message> messages = execute(sendMediaGroup);
            List<Integer> messageIds = messages.stream().map(Message::getMessageId).collect(Collectors.toList());
            return messageIds;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    public Integer sendPhotoIsDelete(SendPhoto sendPhoto) {
        try {
            Message message = execute(sendPhoto);
            return message.getMessageId();
        }catch (TelegramApiException e){
            log.info(e.getMessage());
        }
        return null;
    }

    public Integer sendVideoIsDelete(SendVideo sendVideo) {
        try {
            Message message = execute(sendVideo);
            return message.getMessageId();
        }catch (TelegramApiException e){
            log.info(e.getMessage());
        }
        return null;
    }

    public void oneBroadcast(SendMessage sendMessage) throws TelegramApiException {
        String string = new StyleText().cleanHtmlExceptSpecificTags(sendMessage.getText());
        sendMessage.setText(string);
        Message execute = execute(sendMessage);
    }
    public Integer sendVideo(SendVideo sendVideo, Boolean isReplay,Integer replyMessageId) {
        try {
            if (isReplay){
                sendVideo.setReplyToMessageId(replyMessageId);
            }
            Message message = execute(sendVideo);
            int messageId = message.getMessageId();
            return messageId;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    public Integer sendPhone(SendPhoto sendPhoto, Boolean isReplay,Integer replyMessageId) {
        try {
            if (isReplay){
                sendPhoto.setReplyToMessageId(replyMessageId);
            }
            Message message = execute(sendPhoto);
            int messageId = message.getMessageId();
            return messageId;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private void sendMediaGroup(Message message, String chatId, String caption) throws TelegramApiException {
        SendMediaGroup mediaGroup = new SendMediaGroup();
        mediaGroup.setChatId(chatId);
        List<InputMedia> mediaList = new ArrayList<>();
        boolean isFirst = true;
        if (message.hasPhoto()) {
            List<PhotoSize> photos = message.getPhoto();
            if (photos == null || photos.isEmpty()) return;

            for (PhotoSize photo : photos) {
                InputMediaPhoto mediaPhoto = new InputMediaPhoto();
                mediaPhoto.setMedia(photo.getFileId());
                if (isFirst && caption != null && !caption.isEmpty()) {
                    mediaPhoto.setCaption(caption);
                    mediaPhoto.setParseMode("HTML");
                    isFirst = false;
                }
                mediaList.add(mediaPhoto);
            }
        } else if (message.hasVideo()) {
            Video video = message.getVideo();
            InputMediaVideo mediaVideo = new InputMediaVideo();
            mediaVideo.setMedia(video.getFileId());
            if (caption != null && !caption.isEmpty()) {
                mediaVideo.setCaption(caption);
                mediaVideo.setParseMode("HTML");
            }
            mediaList.add(mediaVideo);
        } else {
            log.warn("Unsupported media type in media group.");
            return;
        }
        mediaGroup.setMedias(mediaList);
        try {
            execute(mediaGroup);
        } catch (TelegramApiException e) {
            log.error("发送媒体组失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    public void nowDeleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }
}


