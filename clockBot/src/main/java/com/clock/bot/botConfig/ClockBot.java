package com.clock.bot.botConfig;

import com.clock.bot.dto.UserDTO;
import com.clock.bot.mapper.UserMapper;
import com.clock.bot.pojo.User;
import com.clock.bot.pojo.UserOperation;
import com.clock.bot.pojo.UserStatus;
import com.clock.bot.service.UserOperationService;
import com.clock.bot.service.UserService;
import com.clock.bot.service.UserStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ClockBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    protected String botToken;
    @Value("${telegram.bot.username}")
    protected String username;
    @Value("${adminUserId}")
    protected String adminUserId;
    @Autowired
    private UserService userService;
    @Autowired
    private UserStatusService userStatusService;
    @Autowired
    private UserOperationService userOperationService;
    @Override
    public String getBotUsername() {
        return username;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }
    static String[] command = {"吃饭","上厕所","抽烟","其他"};
    public static String getMatchedCommand( String input) {
        for (String cmd : command) {
            if (cmd.equals(input)) {
                return cmd;
            }
        }
        return null;
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
        //点击按钮回调用
        if (update.hasCallbackQuery()){

//            nowExchange.CallbackQuery(message,sendMessage,rate,update);
            return;
        }
        if (message==null) {
            return;
        }
        userDTO.setInfo(message);
        if (message.getChat().isUserChat()){
            //处理私聊的消息
        }
        sendMessage.setChatId(String.valueOf(message.getChatId()==null?"":message.getChatId()));
        if (update.hasMessage() && update.getMessage().hasText()) this.BusinessHandler(message,sendMessage,replyToText,userDTO,update);
    }
    //
    private void BusinessHandler(Message message, SendMessage sendMessage, String replyToText, UserDTO userDTO, Update update) {
        String text = userDTO.getText();//查状态需要groupId
        User user = this.isNullUser(userDTO);
        String result="";
        UserStatus userStatus=userStatusService.selectUserStatus(userDTO);
        String name = user.getFirstLastName();
        Date workTime=userStatus!=null?userStatus.getWorkTime():new Date();
        String dateFromat = "";
        dateFromat=workTime.getMonth()+"/"+workTime.getDay()+" "+workTime.getHours()+":"+workTime.getMinutes()+"\n";
        if (text.equals("上班")){
         this.work(userStatus,user,userDTO,result,name,dateFromat);   
        }else if (text.equals("下班")){





        } else if (text.equals("回座")) {




        //执行活动的时候记得更新 status里的: returnHome userOperationId
        } else if (Arrays.asList(command).contains(text)) {
            this.activity(userStatus,user,userDTO,result,name,dateFromat);
        }
        this.sendMessage(sendMessage,result);
    }
    public void callback(UserStatus userStatus,User user,UserDTO userDTO,String result,String name,String dateFromat){

    }

    //活动
    public void activity(UserStatus userStatus,User user,UserDTO userDTO,String result,String name,String dateFromat){
        String matchedCommand = getMatchedCommand(userDTO.getText());
        if (userStatus==null){
            result="用户："+name+"\n" +
                    "用户标识："+user.getUserId()+"\n" +
                    "状态：❌ "+matchedCommand+"打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }else if (userStatus.isStatus()){//是上班状态
            if (userStatus.isReturnHome()){//如果有正在执行的其他状态
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "状态：❌ "+matchedCommand+"打卡失败！\n" +
                        "原因：您有正在进行的活动，"+matchedCommand+"\n" +
                        "提示：进行其他活动前，请先回座\n" +
                        "回座：/back";
            }else {
                UserOperation userOperation = new UserOperation();
                userOperation.setOperation(matchedCommand).setUserStatusId(userStatus.getId()+"").setStartTime(new Date())
                        .setCreateTime(new Date());
                userOperationService.insertUserOperation(userOperation);
                userStatus.setReturnHome(true);
                userStatus.setUserOperationId(userOperation.getId()+"");
                userStatusService.updateUserStatus(userStatus);
                List<UserOperation> userOperations=userOperationService.findByOperation(userStatus.getId(),matchedCommand);
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "✅ 打卡成功：上班 - "+dateFromat +
                        "注意：这是您第 "+userOperations.size()+" 次"+matchedCommand+"\n" +
                        "提示：活动完成后请及时打卡回座\n" +
                        "回座：/back\n" +
                        "------------------------\n" +
                        "点此联系客服升级至独享版";
            }
        }else if (!userStatus.isStatus()){//下班状态
            result="用户："+name+"\n" +
                    "用户标识："+user.getUserId()+"\n" +
                    "状态：❌ "+matchedCommand+"打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }
    }
    public void work(UserStatus userStatus,User user,UserDTO userDTO,String result,String name,String dateFromat){
            if (userStatus==null){
                userStatus=new UserStatus();
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "✅ 打卡成功：上班 - "+dateFromat +
                        "提示：请记得下班时打卡下班\n" +
                        "------------------------\n" +
                        "独享版支持用越南语、泰语、印尼语、英语显示报表文件和统计信息，便于多语言管理员查看，定制请联系 ttjdaka.com";
                userStatus.setStatus(true);
                userStatus.setUserId(user.getUserId());
                userStatus.setUsername(user.getUsername());
                userStatus.setGroupId(userDTO.getGroupId());
                userStatus.setGroupTitle(userDTO.getGroupTitle());
                userStatus.setWorkTime(new Date());
                userStatusService.insertUserStatus(userStatus);
            }else if (userStatus.isStatus()){
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "状态：❌ 上班打卡失败！\n" +
                        "原因：您之前还没有下班，请先下班\n" +
                        "上次上班打卡时间："+dateFromat +
                        "下班：/offwork";
            }else {//已经下班了  在次上班的
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "✅ 打卡成功：上班 - "+dateFromat +
                        "提示：请记得下班时打卡下班\n" +
                        "------------------------\n" +
                        "独享版支持用越南语、泰语、印尼语、英语显示报表文件和统计信息，便于多语言管理员查看，定制请联系 ttjdaka.com";
                userStatus.setStatus(true);
                userStatus.setGroupId(userDTO.getGroupId());
                userStatus.setGroupTitle(userDTO.getGroupTitle());
                userStatus.setWorkTime(new Date());
                userStatusService.insertUserStatus(userStatus);
            }
    }
    public User isNullUser(UserDTO userDTO){
        User byUserId = userService.findByUserId(userDTO.getUserId());
        if (byUserId==null){
            User user = new User();
            user.setUserId(userDTO.getUserId());
            user.setUsername(userDTO.getUsername());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setCreateTime(new Date());
            userService.insertUser(user);
            return user;
        }else {
            return byUserId;
        }
    }
    //拉取机器人进群的处理  检测本群内 近三天上线过的人
    private void onJinQunMessage(Update update) {
        if (update.hasMyChatMember()) { //获取个人信息和授权的时候才是admin
            ChatMemberUpdated chatMember = update.getMyChatMember();
//            if (chatMember.getNewChatMember().getStatus().equals("administrator")){
//                String message="";
//                update.getMessage();
//                SendMessage sendMessage = new SendMessage();
//                String chatId = chatMember.getChat().getId().toString();//群组id
//                sendMessage.setChatId(chatId);
//                this.tronAccountMessageTextHtml(sendMessage,chatId,message);
//            }
            // 检查是否为机器人被添加到群组  left 移除
            if (chatMember.getNewChatMember().getStatus().equals("member") || chatMember.getNewChatMember().getStatus().equals("left")) {
                String chatId = chatMember.getChat().getId().toString();//群组id
                Long id = chatMember.getFrom().getId();//拉机器人的用户
                String username = chatMember.getFrom().getUserName();
                String firstName = chatMember.getFrom().getFirstName();
                String lastName = chatMember.getFrom().getLastName();

                String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", id, id);
                String message="您好，"+format+"，机器人已检测到加入了新群组，正在初始化新群组，请稍候...";
                String message1="<b>请【群组的创建者】将机器人设置为群组管理员，否则可能影响机器人功能！</b>";
                String stats="状态：<code>已开启便捷回复键盘</code>";
                update.getMessage();
                SendMessage sendMessage = new SendMessage();
                PaperPlaneBotButton buttonList = new PaperPlaneBotButton();
                ReplyKeyboardMarkup replyKeyboardMarkup = buttonList.sendReplyKeyboard();
                sendMessage.setReplyMarkup(replyKeyboardMarkup);//是否在onUpdateReceived设置
                sendMessage.setChatId(chatId);
                this.sendMessage(sendMessage,message);
                this.sendMessage(sendMessage,message1);
                this.tronAccountMessageTextHtml(sendMessage,chatId,stats);
            }
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
    protected void editMessageText(EditMessageText editMessage, long chatId, int messageId, String text) {
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
