package com.clock.bot.botConfig;

import com.clock.bot.dto.UserDTO;
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

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    static String[] command = {"吃饭","上厕所","抽烟","其它"};
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
        String firstLastName = user.getFirstLastName();
        String name = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(user.getUserId()), firstLastName);
        Date workTime=userStatus!=null?userStatus.getWorkTime():new Date();
        String dateFromat =workTime.getMonth()+"/"+workTime.getDay()+" "+workTime.getHours()+":"+workTime.getMinutes()+"\n";
        Date date = new Date();
        String nowTime =date.getMonth()+"/"+date.getDay()+" "+date.getHours()+":"+date.getMinutes()+"\n";
        if (text.equals("上班")){
            result=this.work(userStatus,user,userDTO,result,name,dateFromat);
        }else if (text.equals("下班")){
            result=this.downWork(userStatus,user,userDTO,result,name,nowTime);
        } else if (text.equals("回座")) {
            result=this.callback(userStatus,user,userDTO,result,name,nowTime);
        } else if (Arrays.asList(command).contains(text)) {
            result=this.activity(userStatus,user,userDTO,result,name,nowTime);
        }
        this.sendMessage(sendMessage,result);
    }
    //下班
    private String downWork(UserStatus userStatus, User user, UserDTO userDTO, String result, String name, String dateFromat) {
        if (userStatus==null) {
            result = "用户：" + name + "\n" +
                    "用户标识：" + user.getUserId() + "\n" +
                    "状态：❌ 下班打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }else if (userStatus.isStatus()) {//是上班状态
            if (userStatus.isReturnHome()) {//执行中
                UserOperation userOperation = userOperationService.findById(userStatus.getUserOperationId());
                result = "用户：" + name + "\n" +
                        "用户标识：" + user.getUserId() + "\n" +
                        "状态：❌ 下班打卡失败！\n" +
                        "原因：您有正在进行的活动，"+userOperation.getOperation()+"\n" +
                        "提示：下班之前，请先打卡回座结算之前的活动\n" +
                        "回座：/back";
            }else if (!userStatus.isReturnHome()){
                userStatus.setStatus(false);
                userStatus.setWorkDownTime(new Date());
                userStatusService.updateUserStatus(userStatus);
                List<UserStatus> userStatuses=userStatusService.selectTodayUserStatus(userDTO.getUserId(),userDTO.getGroupId());
                // 计算总工作时间
                Duration totalDuration = userStatuses.stream().filter(Objects::nonNull).map(UserStatus::getDuration).reduce(Duration.ZERO, Duration::plus);
                List<Integer> ids = userStatuses.stream().filter(Objects::nonNull).map(UserStatus::getId).collect(Collectors.toList());
                List<UserOperation> operations=userOperationService.findByUserStatusIds(ids);
                AtomicLong seconds = new AtomicLong();
                AtomicReference<Integer> eat = new AtomicReference<>(0);
                AtomicReference<Integer> wc = new AtomicReference<>(0);
                AtomicReference<Integer> smoking = new AtomicReference<>(0);
                AtomicReference<Integer> other = new AtomicReference<>(0);
                AtomicLong eatSeconds = new AtomicLong();
                AtomicLong wcSeconds = new AtomicLong();
                AtomicLong smokingSeconds = new AtomicLong();
                AtomicLong otherSeconds = new AtomicLong();
                operations.stream().filter(Objects::nonNull).forEach(operation->{
                    //"吃饭","上厕所","抽烟","其它"
                    if (operation.getOperation().equals("吃饭")){
                        eat.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        eatSeconds.set(between.getSeconds());
                    }else if (operation.getOperation().equals("上厕所")) {
                        wc.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        wcSeconds.set(between.getSeconds());
                    }else if (operation.getOperation().equals("抽烟")) {
                        smoking.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        smokingSeconds.set(between.getSeconds());
                    }else if (operation.getOperation().equals("其它")) {
                        other.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        otherSeconds.set(between.getSeconds());
                    }
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    seconds.set(between.getSeconds());
                });
                String eatText=eat.get()==0?"":"本日吃饭："+eat.get()+" 次\n";
                String wcText=wc.get()==0?"":"本日上厕所："+wc.get()+" 次\n";
                String smokingText=smoking.get()==0?"":"本日抽烟："+smoking.get()+" 次\n";
                String otherText=other.get()==0?"":"本日其它："+other.get()+" 次\n";
                String o1 = eatSeconds.get() == 0 ? "" : "今日累计吃饭时间："+eatSeconds.get();
                String o2 = wcSeconds.get() == 0 ? "" : "今日累计上厕所时间："+wcSeconds.get();
                String o3 = smokingSeconds.get() == 0 ? "" : "今日累计抽烟时间："+smokingSeconds.get();
                String o4 = otherSeconds.get() == 0 ? "" : "今日累计其它时间："+otherSeconds.get();
                String text1;
                if (eatSeconds.get()==0&&wcSeconds.get()==0&&smokingSeconds.get()==0&&otherSeconds.get()==0){
                    text1="";
                }else {
                    text1=o1+o2+o3+o4+"------------------------\n" ;
                }
                String text;
                if (eat.get()==0&&wc.get()==0&&smoking.get()==0&&other.get()==0){
                    text="";
                }else {
                    text=eatText+wcText+smokingText+otherText+"------------------------\n" ;
                }
                long l = totalDuration.getSeconds() - seconds.get();//纯工作时间
                result = "用户：" + name + "\n" +
                        "用户标识：<code>" + user.getUserId() + "</code>\n" +
                        "✅ 打卡成功：下班 - <code>"+dateFromat+"</code>\n" +
                        "提示：本日工作时间已结算\n" +
                        "今日工作总计："+ totalDuration.toHours() + "时 " + totalDuration.toMinutes() + "分 " + totalDuration.getSeconds() + "秒"+"\n" +
                        "纯工作时间："+new Date(l)+ "秒\n" +
                        "------------------------\n" +
                        "今日累计活动总时间："+new Date(seconds.get())+"\n" +
                        text1+text+
                        "点此联系客服升级至独享版";
            }
        }else if (!userStatus.isStatus()){//下班状态
            result="用户："+name+"\n" +
                    "用户标识："+user.getUserId()+"\n" +
                    "状态：❌ 下班打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }
        return result;
    }

    //回座
    public String callback(UserStatus userStatus,User user,UserDTO userDTO,String result,String name,String dateFromat){
        if (userStatus==null){
            result="用户："+name+"\n" +
                    "用户标识："+user.getUserId()+"\n" +
                    "状态：❌  回座打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }else if (userStatus.isStatus()) {//是上班状态
            if (userStatus.isReturnHome()){//执行中
                userStatus.setReturnHome(false);
                userStatusService.updateUserStatus(userStatus);
                String userOperationId = userStatus.getUserOperationId();
                UserOperation userOperation=userOperationService.findById(userOperationId);
                userOperation.setEndTime(new Date());
                userOperationService.updateUserOperation(userOperation);
                int id = userStatus.getId();
                List<UserOperation> operations=userOperationService.findByStatusId(id+"");
                AtomicLong seconds = new AtomicLong();
                AtomicReference<Integer> eat = new AtomicReference<>(0);
                AtomicReference<Integer> wc = new AtomicReference<>(0);
                AtomicReference<Integer> smoking = new AtomicReference<>(0);
                AtomicReference<Integer> other = new AtomicReference<>(0);
                AtomicLong eatSeconds = new AtomicLong();
                AtomicLong wcSeconds = new AtomicLong();
                AtomicLong smokingSeconds = new AtomicLong();
                AtomicLong otherSeconds = new AtomicLong();
                operations.stream().filter(Objects::nonNull).forEach(operation->{
                    //"吃饭","上厕所","抽烟","其它"
                    if (operation.getOperation().equals("吃饭")){
                        eat.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        eatSeconds.set(between.getSeconds());
                    }else if (operation.getOperation().equals("上厕所")) {
                        wc.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        wcSeconds.set(between.getSeconds());
                    }else if (operation.getOperation().equals("抽烟")) {
                        smoking.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        smokingSeconds.set(between.getSeconds());
                    }else if (operation.getOperation().equals("其它")) {
                        other.updateAndGet(v -> v + 1);
                        Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                        otherSeconds.set(between.getSeconds());
                    }
                    Duration between = Duration.between(operation.getStartTime().toInstant(), operation.getEndTime().toInstant());
                    seconds.set(between.getSeconds());
                });
                String eatText=eat.get()==0?"":"本日吃饭："+eat.get()+" 次\n";
                String wcText=wc.get()==0?"":"本日上厕所："+wc.get()+" 次\n";
                String smokingText=smoking.get()==0?"":"本日抽烟："+smoking.get()+" 次\n";
                String otherText=other.get()==0?"":"本日其它："+other.get()+" 次\n";
                String o1 = eatSeconds.get() == 0 ? "" : "今日累计吃饭时间："+eatSeconds.get();
                String o2 = wcSeconds.get() == 0 ? "" : "今日累计上厕所时间："+wcSeconds.get();
                String o3 = smokingSeconds.get() == 0 ? "" : "今日累计抽烟时间："+smokingSeconds.get();
                String o4 = otherSeconds.get() == 0 ? "" : "今日累计其它时间："+otherSeconds.get();
                String text1;
                if (eatSeconds.get()==0&&wcSeconds.get()==0&&smokingSeconds.get()==0&&otherSeconds.get()==0){
                    text1="";
                }else {
                    text1=o1+o2+o3+o4+"------------------------\n" ;
                }
                String text;
                if (eat.get()==0&&wc.get()==0&&smoking.get()==0&&other.get()==0){
                    text="";
                }else {
                    text=eatText+wcText+smokingText+otherText+"------------------------\n" ;
                }
                Duration between = Duration.between(userOperation.getStartTime().toInstant(), userOperation.getEndTime().toInstant());
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "✅ "+dateFromat+" 回座打卡成功："+userOperation.getOperation()+"\n" +
                        "提示：本次活动时间已结算\n" +
                        "本次活动耗时："+between.getSeconds()+" 秒\n" + text1+text+"今日累计活动总时间："+seconds+" 秒\n"+
                        "获取安全知识，电报使用技巧，欢迎关注： t.me/ttj817_channel";
            }else if (!userStatus.isReturnHome()){
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "状态：❌ 回座打卡失败！\n" +
                        "原因：您没有进行中的活动\n" +
                        "您可以————\n" +String.join("\n", command);
            }
        }else if (!userStatus.isStatus()){//下班状态
            result="用户："+name+"\n" +
                    "用户标识："+user.getUserId()+"\n" +
                    "状态：❌ 回座打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }
        return result;
    }

    //活动
    public String activity(UserStatus userStatus,User user,UserDTO userDTO,String result,String name,String dateFromat){
        String matchedCommand = getMatchedCommand(userDTO.getText());
        if (userStatus==null){
            result="用户："+name+"\n" +
                    "用户标识："+user.getUserId()+"\n" +
                    "状态：❌ "+matchedCommand+"打卡失败！\n" +
                    "原因：您之前还没有上班，请先上班\n" +
                    "上班：/work";
        }else if (userStatus.isStatus()){//是上班状态
            if (userStatus.isReturnHome()){//如果有正在执行的其它状态
                UserOperation userOperation = userOperationService.findById(userStatus.getUserOperationId());
                result="用户："+name+"\n" +
                        "用户标识："+user.getUserId()+"\n" +
                        "状态：❌ "+matchedCommand+"打卡失败！\n" +
                        "原因：您有正在进行的活动，"+userOperation.getOperation()+"\n" +
                        "提示：进行其它活动前，请先回座\n" +
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
        return result;
    }
    public String work(UserStatus userStatus,User user,UserDTO userDTO,String result,String name,String dateFromat){
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
                userStatus.setCreateTime(new Date());
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
                userStatus=new UserStatus();
                userStatus.setStatus(true);
                userStatus.setUserId(user.getUserId());
                userStatus.setUsername(user.getUsername());
                userStatus.setGroupId(userDTO.getGroupId());
                userStatus.setGroupTitle(userDTO.getGroupTitle());
                userStatus.setWorkTime(new Date());
                userStatus.setCreateTime(new Date());
                userStatusService.insertUserStatus(userStatus);
            }
            return result;
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
        sendMessage.disableWebPagePreview();//禁用预览链接
//        sendMessage.enableMarkdown(true);
        try {
            log.info("发送消息:{}", text);
            execute(sendMessage);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

}