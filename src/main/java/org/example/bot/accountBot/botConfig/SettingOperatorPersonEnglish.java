package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.StatusService;
import org.example.bot.accountBot.service.UserOperationService;
import org.example.bot.accountBot.service.UserService;
import org.example.bot.accountBot.utils.ConstantMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 设置操作人员
 */

@Slf4j
@Service
public class SettingOperatorPersonEnglish {
    @Autowired
    UserService userService;
    @Autowired
    StatusService statusService;
    @Autowired
    StatusMapper statusMapper;

    @Autowired
    UserOperationService userOperationService;
    @Autowired
    AccountBot accountBot;
    @Value("${telegram.bot.username}")
    protected String username;
    @Autowired
    ButtonList buttonList;
    public boolean isValidSetOperatorCommand(String text) {
        // 匹配“设置操作员”或对应的英文关键词开头，并且后面跟着一个或多个 @username
        String regex = "^(设置操作员|设置操作人|set operator|add operator)(\\s+@\\w+)+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
    /**
     * 设置操作人员
     * @param text 传输的文本 是否是 设置操作员
     * @param userDTO 封装的用户信息
     * @param sendMessage 发生的消息
     * @param text  消息文本 6976772117
     */
    public void setHandle(SendMessage sendMessage, String text, UserDTO userDTO, User user6, Status status, GroupInfoSetting groupInfoSetting, UserNormal userNormalTempAdmin, Update update) {
        boolean isShowAdminMessage = false;
        String lowerText = text.toLowerCase();// 统一转小写处理
        List<String> upAdmin=new ArrayList<>();//已设置的管理
        List<String> succeedAdmin=new ArrayList<>();//设置成功的管理
        if (lowerText.startsWith("set operator")){
            if (!user6.isSuperAdmin()){//是普通权限
                accountBot.sendMessage(sendMessage,"您没有设置操作员权限! 只能管理设置");
                return;
            }
            if (update.getMessage().getReplyToMessage()!=null){
                UserOperation userOperation = userOperationService.selectByUserAndGroupId(userDTO.getCallBackUserId(), userDTO.getGroupId());
                if (userOperation != null && userOperation.isOperation()) {
                    accountBot.sendMessage(sendMessage, "已设置该操作员无需重复设置");
                    return;
                } else if (userOperation != null && !userOperation.isOperation()) {
                    userOperation.setOperation(true);
                    userOperationService.update(userOperation);
                    accountBot.sendMessage(sendMessage, "设置成功!");
                    return;
                } else if (userOperation == null) {
                    userOperation = new UserOperation();
                    userOperation.setOperation(true);
                    userOperation.setUserId(userDTO.getCallBackUserId());
                    userOperation.setAdminUserId(userNormalTempAdmin.getUserId());
                    userOperation.setUsername(userDTO.getCallBackName());
                    userOperation.setGroupId(userDTO.getGroupId());
                    userOperationService.insertUserOperation(userOperation);
                    accountBot.sendMessage(sendMessage, "设置成功!");
                    return;
                }
                return;
            }
            if (!isValidSetOperatorCommand(lowerText)) {
                accountBot.sendMessage(sendMessage, "命令错误，添加操作员请@对方的用户名，例如：设置操作员 @XXX");
                return;
            }
            Pattern compile = Pattern.compile("@(\\w+)");
            if (compile.matcher(text).find()){
                Matcher matcher = compile.matcher(text);//应该循环添加id
                List<String> userNames = new ArrayList<>();
                while (matcher.find()) {
                    // 将匹配到的用户名添加到列表中
                    userNames.add(matcher.group(1));
                }
                for (String usernameTemp : userNames) {
                    User user = userService.findByUsername(usernameTemp);
                    UserOperation userOperation1 = userOperationService.findByUsername(usernameTemp, userDTO.getGroupId());
                    if (user!=null && usernameTemp.equals(user.getUsername())){
                        if (userOperation1!=null && userOperation1.isOperation()){//是操作员
                            isShowAdminMessage = true;
                            upAdmin.add(" @"+usernameTemp);
                        }else {
                            userOperation1=new UserOperation();
                            userOperation1.setOperation(true);//是操作员
                            userOperation1.setUsername(usernameTemp);
                            userOperation1.setGroupId(userDTO.getGroupId());
                            userOperation1.setAdminUserId(userDTO.getUserId());
                            userOperationService.insertUserOperation(userOperation1);
                            succeedAdmin.add(" @"+usernameTemp);
                        }
                    }else {
                        User user1 = new User();
                        UserOperation userOperation = new UserOperation();
                        userOperation.setUsername(usernameTemp);
                        userOperation.setOperation(true);//是操作员
//                        userAuthority1.setUserId(userDTO.getUserId());//设置了也是null
                        userOperation.setGroupId(userDTO.getGroupId());

                        userOperation.setAdminUserId(userDTO.getUserId());
                        user1.setUsername(usernameTemp);
                        userOperationService.insertUserOperation(userOperation);
                        userService.insertUser(user1);
                        succeedAdmin.add(" @"+usernameTemp);
                    }
                }
                //回复用
            }else if ( !compile.matcher(lowerText).find()){
                User callBackUser = userService.findByUserId(userDTO.getCallBackUserId());
                UserOperation userOperation = userOperationService.selectByUserAndGroupId(userDTO.getCallBackUserId(), userDTO.getGroupId());
                User user2 = userService.findByUsername(userDTO.getCallBackName());
                UserOperation userOperation2 = userOperationService.findByUsername(userDTO.getCallBackName(), userDTO.getGroupId());
                if (callBackUser!=null){
                    if (userOperation!=null && userOperation.isOperation()) {
                        isShowAdminMessage = true;
                        upAdmin.add(" @" + callBackUser.getUsername());
                    }else {
                        callBackUser.setUserId(userDTO.getCallBackUserId());
                        callBackUser.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        callBackUser.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                        callBackUser.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                        userService.updateUser(callBackUser);
                        userOperation=new UserOperation();
                        userOperation.setAdminUserId(userDTO.getUserId());
                        userOperation.setUserId(userDTO.getCallBackUserId());
                        userOperation.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        userOperation.setOperation(true);
                        userOperation.setGroupId(userDTO.getGroupId());
                        userOperationService.insertUserOperation(userOperation);
                        succeedAdmin.add(" @" + callBackUser.getUsername());
                    }
                } else if (user2!=null) {
                    if (userOperation2!=null &&userOperation2.isOperation()) {
                        isShowAdminMessage = true;
                        upAdmin.add(" @" + user2.getUsername());
                    }else {
                        user2.setUserId(userDTO.getCallBackUserId());
                        user2.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        user2.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                        user2.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                        userService.updateUser(user2);
                        userOperation2=new UserOperation();
                        userOperation2.setAdminUserId(userDTO.getUserId());
                        userOperation2.setGroupId(userDTO.getGroupId());
                        userOperation2.setUserId(userDTO.getCallBackUserId());
                        userOperation2.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        userOperation2.setOperation(true);
                        userOperationService.insertUserOperation(userOperation2);
                        succeedAdmin.add(" @" + user2.getUsername());
                    }
                }else {
                    User user = new User();
                    user.setUserId(userDTO.getCallBackUserId());
                    user.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                    user.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                    user.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                    userService.insertUser(user);
                    UserOperation userOperation1 = new UserOperation();
                    userOperation1.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                    userOperation1.setOperation(true);//设置操作员
                    userOperation1.setUserId(userDTO.getCallBackUserId());
                    userOperation1.setGroupId(userDTO.getGroupId());
                    userOperation1.setAdminUserId(userDTO.getUserId());
                    userOperationService.insertUserOperation(userOperation1);
                    succeedAdmin.add(" @" + user.getUsername());
                }
            }
            if (!isShowAdminMessage){
                accountBot.sendMessage(sendMessage,"设置成功");
            }else {
                if (upAdmin.size()>0){
                    accountBot.sendMessage(sendMessage, "已设置该操作员无需重复设置"+upAdmin);
                }
                if (succeedAdmin.size()>0){
                    accountBot.sendMessage(sendMessage, "设置成功"+succeedAdmin);
                }
            }
        }else if (lowerText.equals("show operator")){
            String  admin = String.format("<a href=\"tg://user?id=%d\">@%s</a>", Long.parseLong(userNormalTempAdmin.getUserId()), userNormalTempAdmin.getUsername());
            StringBuilder sb = new StringBuilder("本群机器人最高权限管理员为："+admin+"\n");
            sb.append("其他操作员为: ");
            List<UserOperation> userAuthorities=userOperationService.selectByUserOperator(userDTO.getGroupId(),true);
            List<User> users = new ArrayList<>();
            for (UserOperation ua:userAuthorities){
                if (ua.getUserId()!=null && StringUtils.isNotBlank(ua.getUserId())&& ua.getUserId().equals(userNormalTempAdmin.getUserId())){
                    continue;
                }
                if (ua.getUsername()!=null && StringUtils.isNotBlank(ua.getUsername())){
                    User byUsername = userService.findByUsername(ua.getUsername());
                    users.add(byUsername);
                    if (ua.getUserId() == null || StringUtils.isBlank(ua.getUserId())){
                        ua.setUserId(byUsername.getUserId());
                        userOperationService.update(ua);
                    }
                } else if (ua.getUserId()!=null && StringUtils.isNotBlank(ua.getUserId())) {
                    User byUserId = userService.findByUserId(ua.getUserId());
                    users.add(byUserId);
                    if (ua.getUsername() == null || StringUtils.isBlank(ua.getUsername())){
                        ua.setUsername(byUserId.getUsername());
                        userOperationService.update(ua);
                    }
                }
            }
            if (users.isEmpty()){
                buttonList.implList(sendMessage,userDTO.getGroupId(),userDTO.getGroupTitle(),groupInfoSetting);
                accountBot.sendMessage(sendMessage,sb.toString());
                return;
            }
            List<User> userNormalList = users.stream().collect(Collectors.collectingAndThen(
                    Collectors.toMap(User::getUserId, p -> p, (p1, p2) -> p1),
                    map -> new ArrayList<>(map.values())));
            for (int i = 0; i < userNormalList.size(); i++) {
                String lastName = userNormalList.get(i).getLastName()==null?"":userNormalList.get(i).getLastName();
                String firstName=userNormalList.get(i).getFirstName()==null?"":userNormalList.get(i).getFirstName()+ "";
                String nickName=firstName+lastName;
                String username1 = userNormalList.get(i).getUsername();
                String format;
                //如果没有用户id就显示用户名
                if (userNormalList.get(i).getUserId()!=null && StringUtils.isNotBlank(username1)) {
                    format = String.format("<a href=\"tg://user?id=%d\">@%s</a>", Long.parseLong(userNormalList.get(i).getUserId()), username1);
                }else if (userNormalList.get(i).getUserId()!=null && StringUtils.isNotBlank(nickName)){
                    format = String.format("<a href=\"tg://user?id=%d\">@%s</a>", Long.parseLong(userNormalList.get(i).getUserId()), nickName);
                }else if (userNormalList.get(i).getUserId()==null || StringUtils.isBlank(userNormalList.get(i).getUserId())){
                    format= "@"+nickName;
                }else {
                    format= String.format("<a href=\"tg://user?id=%d\">@%s</a>", Long.parseLong(userNormalList.get(i).getUserId()), userNormalList.get(i).getUserId());
                }
                sb.append(format+" ");
            }
            buttonList.implList(sendMessage,userDTO.getGroupId(),userDTO.getGroupTitle(),groupInfoSetting);
            accountBot.sendMessage(sendMessage,sb.toString());
        }else if (lowerText.startsWith("delete operator")){
            if (userDTO.getCallBackUserId()!=null && StringUtils.isNotBlank(userDTO.getCallBackUserId())){
                if (userNormalTempAdmin.getUserId().equals(userDTO.getCallBackUserId())){
                    accountBot.sendMessage(sendMessage,"不能删除管理员!");
                    return;
                }
                UserOperation byUsername = userOperationService.selectByUserAndGroupId(userDTO.getCallBackUserId(),userDTO.getGroupId());
                if (byUsername!=null){
                    //修改为普通用户
                    userOperationService.deleteByUserId(userDTO.getCallBackUserId(),userDTO.getGroupId());
                    accountBot.sendMessage(sendMessage,"删除成功");
                }else if (byUsername==null){
                    byUsername= userOperationService.selectByUserName(userDTO.getCallBackName(),userDTO.getGroupId());
                    if (byUsername!=null) {
                        //修改为普通用户
                        userOperationService.deleteByUsername(userDTO.getCallBackName(), userDTO.getGroupId());
                        accountBot.sendMessage(sendMessage, "删除成功");
                    }else {
                        accountBot.sendMessage(sendMessage,"未查询到此操作人!删除失败");
                    }
                }
            }
        }else if (lowerText.startsWith("delete operator person")){
            if (userDTO.getCallBackUserId()!=null && StringUtils.isNotBlank(userDTO.getCallBackUserId())){
                if (userNormalTempAdmin.getUserId().equals(userDTO.getCallBackUserId())){
                    accountBot.sendMessage(sendMessage,"不能删除管理员!");
                    return;
                }
                UserOperation byUsername = userOperationService.selectByUserAndGroupId(userDTO.getCallBackUserId(),userDTO.getGroupId());
                if (byUsername!=null){
                    //修改为普通用户
                    userOperationService.deleteByUserId(userDTO.getCallBackUserId(),userDTO.getGroupId());
                    accountBot.sendMessage(sendMessage,"删除成功");
                }else if (byUsername==null){
                    byUsername= userOperationService.selectByUserName(userDTO.getCallBackName(),userDTO.getGroupId());
                    if (byUsername!=null) {
                        //修改为普通用户
                        userOperationService.deleteByUsername(userDTO.getCallBackName(), userDTO.getGroupId());
                        accountBot.sendMessage(sendMessage, "删除成功");
                    }else {
                        accountBot.sendMessage(sendMessage,"未查询到此操作人!删除失败");
                    }
                }
            }
        }else if (lowerText.startsWith("show handling fee")){
            status.setShowHandlerMoneyStatus(0);
            statusService.updateStatus("show_handler_money_status"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.startsWith("hidden fees")){
            status.setShowHandlerMoneyStatus(1);
            statusService.updateStatus("show_handler_money_status"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.startsWith("setup fee ")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(lowerText.substring("setup fee ".length(), lowerText.length())));
            status.setAccountHandlerMoney(money);
            status.setIssueHandlerMoney(money);
            status.setShowHandlerMoneyStatus(0);
            statusService.updateStatus("show_handler_money_status"     ,0, userDTO.getGroupId());
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.startsWith("set withdrawal fee")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(lowerText.substring("set withdrawal fee".length(), lowerText.length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.startsWith("set single withdrawal fee")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(lowerText.substring("set single withdrawal fee".length()), lowerText.length()));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.startsWith("set single deposit fee")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(lowerText.substring("set single deposit fee".length()), lowerText.length()));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.startsWith("set deposit fee")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(lowerText.substring("set deposit fee".length(), lowerText.length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("show categories")){
            status.setDisplaySort(0);
            statusService.updateStatus("display_sort"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("hide categories")){
            status.setDisplaySort(1);
            statusService.updateStatus("display_sort"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("show operator")||lowerText.equals("show operator name")){
            status.setHandleStatus(0);
            status.setCallBackStatus(1);
            statusService.updateStatus("handle_status"    ,0, userDTO.getGroupId());
            statusService.updateStatus("call_back_status", 1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("turn off display")||lowerText.equals("hide operator name")
                ||lowerText.equals("hide name")||lowerText.equals("hide titles")){
            status.setHandleStatus(1);
            statusService.updateStatus("handle_status"    ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("show replier")||lowerText.equals("show replier name")){
            status.setHandleStatus(1);
            status.setCallBackStatus(0);
            statusService.updateStatus("handle_status"     ,1, userDTO.getGroupId());
            statusService.updateStatus("call_back_status" , 0, userDTO.getGroupId());
        }else if (lowerText.equals("hide replyer display")||lowerText.equals("hide reply name")||lowerText.equals("hide replyer info") ){
            status.setCallBackStatus(1);
            statusService.updateStatus("call_back_status" , 1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("show details") ){
            status.setDetailStatus(0);
            statusService.updateStatus("detail_status"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("hide details")){
            status.setDetailStatus(1);
            statusService.updateStatus("detail_status"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (lowerText.equals("show balance")||lowerText.equals("display amount")) {
            status.setShowMoneyStatus(0);
            statusService.updateStatus("show_money_status"  ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (lowerText.equals("show usdt")) {
            status.setShowMoneyStatus(1);
            statusService.updateStatus("show_money_status"  ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (lowerText.equals("show all")) {
            status.setShowMoneyStatus(2);
            statusService.updateStatus("show_money_status"  ,2, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (lowerText.equals("show 1 item")||lowerText.equals("show 3 item")||lowerText.equals("show 5 item")) {
            int i = Integer.parseInt(lowerText.substring(5,6));
            status.setShowFew(i);
            statusService.updateStatus("show_few"            ,i , userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (lowerText.startsWith("set the exchange rate")){
            BigDecimal downExchange=BigDecimal.valueOf(Long.parseLong(lowerText.substring("set the exchange rate".length(), lowerText.length())));
            status.setDownExchange(downExchange);
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage, "操作成功");
        } else if (lowerText.startsWith("set the delivery rate")) {
            BigDecimal downRate=BigDecimal.valueOf(Long.parseLong(lowerText.substring("set the delivery rate".length(), lowerText.length())));
            status.setDownRate(downRate);
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage, "操作成功");
        }
    }

    //删除操作人员
    public void deleteHandleEnglish(String text, SendMessage sendMessage, UserDTO userDTO,UserNormal userNormal) {
        if (!isValidDeleteOperatorCommand(text)) {
            return; // 不符合格式，直接返回，不处理
        }
        if (text.length() < 15) {
            return;
        }
        String lowerText = text.toLowerCase();
        if (lowerText.startsWith("delete operator")) {
            List<String > upAdmin=new ArrayList<>();//未删除成功的
            List<String > successAdmin=new ArrayList<>();//删除成功的
            List<String> usernamesToDelete = extractUsernames(text);
            if (usernamesToDelete.isEmpty()) {
                accountBot.sendMessage(sendMessage, "未检测到要删除的操作员");
                return;
            }
            for (String username : usernamesToDelete) {
                User byUsername = userService.findByUsername(username);
                if (userNormal.getUsername().equals(username)){
                    accountBot.sendMessage(sendMessage,"不能删除本群权限人! @"+username);
                    continue;
                }
                if (byUsername != null) {
                    userOperationService.deleteByUsername(username, userDTO.getGroupId());
                    successAdmin.add(" @"+username);
                } else {
                    upAdmin.add(" @"+username);
                }
            }
            if (!upAdmin.isEmpty()){
                accountBot.sendMessage(sendMessage,"❌未查询到此操作人!删除失败 @"+upAdmin);
            }
            if (!successAdmin.isEmpty()){
                accountBot.sendMessage(sendMessage,"✅删除成功!" + successAdmin);
            }
        }
    }
    private boolean isValidDeleteOperatorCommand(String text) {
        String regex = "^delete\\s+operator(\\s+@\\w+)+$";
        return text.toLowerCase().matches(regex);
    }

    public static List<String> extractUsernames(String input) {
        List<String> usernames = new ArrayList<>();
        // 正则匹配 @username 形式，username 可以包含字母、数字、下划线
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            usernames.add(matcher.group(1)); // group(1) 是括号内的内容
        }
        return usernames;
    }

}
