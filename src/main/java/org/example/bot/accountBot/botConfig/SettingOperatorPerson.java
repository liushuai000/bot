package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.pojo.*;
import org.example.bot.accountBot.service.*;
import org.example.bot.accountBot.utils.ConstantMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 设置操作人员
 */

@Slf4j
@Service
public class SettingOperatorPerson{
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
    /**
     * 设置操作人员
     * @param split1 传输的文本 是否是 设置操作员
     * @param userDTO 封装的用户信息
     * @param sendMessage 发生的消息
     * @param text  消息文本 6976772117
     */
    Map<String, String> constantMap = ConstantMap.COMMAND_MAP_ENGLISH;//关键词的对应关系
    public void setHandle(String[] split1, SendMessage sendMessage, String text, UserDTO userDTO, User user6, Status status, GroupInfoSetting groupInfoSetting) {
        boolean isShowAdminMessage = false;
        if (split1[0].equals("设置操作员")||split1[0].equals("设置操作人")
                || split1[0].equals(constantMap.get("设置操作员"))||split1[0].equals(constantMap.get("设置操作人"))){
            if (!user6.isSuperAdmin()){//是普通权限
                accountBot.sendMessage(sendMessage,"您没有设置操作员权限! 只能管理设置");
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
                        }else {
                            userOperation1=new UserOperation();
                            userOperation1.setOperation(true);//是操作员
                            userOperation1.setUserId(user.getUserId());
                            userOperation1.setUsername(usernameTemp);
                            userOperation1.setGroupId(userDTO.getGroupId());
                            userOperation1.setAdminUserId(userDTO.getUserId());
                            userOperationService.insertUserOperation(userOperation1);
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
                    }
                }
                //回复用
            }else if ( !compile.matcher(text).find()){
                User callBackUser = userService.findByUserId(userDTO.getCallBackUserId());
                UserOperation userOperation = userOperationService.selectByUserAndGroupId(userDTO.getCallBackUserId(), userDTO.getGroupId());
                User user2 = userService.findByUsername(userDTO.getCallBackName());
                UserOperation userOperation2 = userOperationService.findByUsername(userDTO.getCallBackName(), userDTO.getGroupId());
                if (callBackUser!=null){
                    if (userOperation!=null && userOperation.isOperation()) {
                        isShowAdminMessage = true;
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
                    }
                } else if (user2!=null) {
                    if (userOperation2!=null &&userOperation2.isOperation()) {
                        isShowAdminMessage = true;
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
                }
            }
            if (!isShowAdminMessage){
                accountBot.sendMessage(sendMessage,"设置成功");
            }else {
                accountBot.sendMessage(sendMessage,"已设置该操作员无需重复设置");
            }
        }else if (split1[0].equals("显示操作人")||split1[0].equals("显示操作员")
                ||  split1[0].equals(constantMap.get("显示操作人"))|| split1[0].equals(constantMap.get("显示操作员"))){
            StringBuilder sb = new StringBuilder("当前操作人: ");
            List<UserOperation> userAuthorities=userOperationService.selectByUserOperator(userDTO.getGroupId(),true);
            List<User> users = new ArrayList<>();
            userAuthorities.stream().filter(Objects::nonNull).forEach(ua->{
                users.add(userService.selectUserNameOrUserId(ua.getUsername(),ua.getUserId()));
            });
            List<User> userNormalList = users.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toMap(User::getUserId, p -> p, (p1, p2) -> p1),
                            map -> new ArrayList<>(map.values())
                    ));
            for (int i = 0; i < userNormalList.size(); i++) {
                String lastName = userNormalList.get(i).getLastName()==null?"":userNormalList.get(i).getLastName();
                String callBackName=userNormalList.get(i).getFirstName()==null?"":userNormalList.get(i).getFirstName()+lastName+ "   ";
                String format;
                //如果没有用户id就显示用户名
                if (userNormalList.get(i).getUserId()!=null){
                    format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(userNormalList.get(i).getUserId()), callBackName);
                }else {
                    format=callBackName;
                }
                sb.append(format);
            }
            buttonList.implList(sendMessage,userDTO.getGroupId(),userDTO.getGroupTitle(),groupInfoSetting);
            accountBot.sendMessage(sendMessage,sb.toString());
        }else if (split1[0].equals("显示手续费")|| split1[0].equals(constantMap.get("显示手续费"))){
            status.setShowHandlerMoneyStatus(0);
            statusService.updateStatus("show_handler_money_status"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏手续费") || split1[0].equals(constantMap.get("隐藏手续费"))){
            status.setShowHandlerMoneyStatus(1);
            statusService.updateStatus("show_handler_money_status"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置手续费".length(), split1[0].length())));
            status.setAccountHandlerMoney(money);
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains(constantMap.get("设置手续费"))){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(constantMap.get("设置手续费").length(), split1[0].length())));
            status.setAccountHandlerMoney(money);
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置下发单笔手续费")||split1[0].contains("设置单笔下发手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(9, split1[0].length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains(constantMap.get("设置下发单笔手续费"))|| split1[0].contains(constantMap.get("设置单笔下发手续费"))){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(constantMap.get("设置下发单笔手续费").length(), split1[0].length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置下发手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置下发手续费".length(), split1[0].length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains(constantMap.get("设置下发手续费"))){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(constantMap.get("设置下发手续费").length(), split1[0].length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置入款单笔手续费")||split1[0].contains("设置单笔入款手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(9, split1[0].length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains(constantMap.get("设置入款单笔手续费"))|| split1[0].contains(constantMap.get("设置单笔入款手续费"))){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(constantMap.get("设置入款单笔手续费").length(), split1[0].length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置入款手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置入款手续费".length(), split1[0].length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains(constantMap.get("设置入款手续费"))){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(constantMap.get("设置入款手续费").length(), split1[0].length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示分类")|| split1[0].equals(constantMap.get("显示分类"))){
            status.setDisplaySort(0);
            statusService.updateStatus("display_sort"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏分类")|| split1[0].equals(constantMap.get("隐藏分类"))){
            status.setDisplaySort(1);
            statusService.updateStatus("display_sort"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("将操作员显示")||split1[0].equals("显示操作人名称")||split1[0].equals("显示操作人名字")
        ||split1[0].equals(constantMap.get("将操作员显示"))||split1[0].equals(constantMap.get("显示操作人名称"))||split1[0].equals(constantMap.get("显示操作人名字"))
        ){
            status.setHandleStatus(0);
            status.setCallBackStatus(1);
            statusService.updateStatus("handle_status"    ,0, userDTO.getGroupId());
            statusService.updateStatus("call_back_status", 1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("关闭显示")||split1[0].equals("隐藏操作人名称")||split1[0].equals("隐藏操作人名字")
                ||split1[0].equals("隐藏名字")||split1[0].equals("隐藏名称")
        ||split1[0].equals(constantMap.get("关闭显示"))||split1.equals(constantMap.get("隐藏操作人名称"))||split1.equals(constantMap.get("隐藏操作人名字"))
                ||split1.equals(constantMap.get("隐藏名字"))||split1.equals(constantMap.get("隐藏名称"))
        ){
            status.setHandleStatus(1);
            statusService.updateStatus("handle_status"    ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("将回复人显示")||split1[0].equals("显示回复人名称")
                || split1.equals(constantMap.get("将回复人显示")) || split1.equals(constantMap.get("显示回复人名称"))){
            status.setHandleStatus(1);
            status.setCallBackStatus(0);
            statusService.updateStatus("handle_status"     ,1, userDTO.getGroupId());
            statusService.updateStatus("call_back_status" , 0, userDTO.getGroupId());
        }else if (split1[0].equals("关闭回复人显示")||split1[0].equals("隐藏回复人显示") ||
                split1.equals(constantMap.get("关闭回复人显示"))|| split1.equals(constantMap.get("隐藏回复人显示"))){
            status.setCallBackStatus(1);
            statusService.updateStatus("call_back_status" , 1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示明细") || split1[0].equals(constantMap.get("显示明细"))){
            status.setDetailStatus(0);
            statusService.updateStatus("detail_status"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏明细")|| split1[0].equals(constantMap.get("隐藏明细"))){
            status.setDetailStatus(1);
            statusService.updateStatus("detail_status"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示余额")||split1[0].equals("显示金额")
                ||split1[0].equals(constantMap.get("显示余额"))||split1[0].equals(constantMap.get("显示金额"))) {
            status.setShowMoneyStatus(0);
            statusService.updateStatus("show_money_status"  ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].equals("显示USDT")||split1[0].equals("显示usdt")
                || split1[0].equals(constantMap.get("显示USDT"))||split1[0].equals(constantMap.get("显示usdt"))) {
            status.setShowMoneyStatus(1);
            statusService.updateStatus("show_money_status"  ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].equals("显示全部")||split1[0].equals(constantMap.get("显示全部"))) {
            status.setShowMoneyStatus(2);
            statusService.updateStatus("show_money_status"  ,2, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].equals("显示1条")||split1[0].equals("显示3条")||split1[0].equals("显示5条")
                ||(split1[0].equals(constantMap.get("显示1条"))||split1[0].equals(constantMap.get("显示3条"))||split1[0].equals(constantMap.get("显示5条")))) {
            int i = Integer.parseInt(split1[0].substring(2,3));
            status.setShowFew(i);
            statusService.updateStatus("show_few"            ,i , userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].startsWith("设置下发汇率")){
            BigDecimal downExchange=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置下发汇率".length(), split1[0].length())));
            status.setDownExchange(downExchange);
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage, "操作成功");
        } else if (split1[0].startsWith("设置下发费率")) {
            BigDecimal downRate=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置下发费率".length(), split1[0].length())));
            status.setDownRate(downRate);
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage, "操作成功");
        }
    }

    //删除操作人员
    public void deleteHandle(String text,SendMessage sendMessage,UserDTO userDTO) {
        if (text.length()<4){
            return;
        }
        if (text.startsWith("删除操作人") || text.startsWith(constantMap.get("删除操作人"))||text.startsWith("删除操作员") || text.startsWith(constantMap.get("删除操作员"))){
            String[] split = splitOperatorSkipFirst(text);
            for (String deleteName : split) {
                String userName=deleteName.substring(1, deleteName.length());//@ggg_id
                UserOperation byUsername = userOperationService.selectByUserName(userName,userDTO.getGroupId());
                if (byUsername!=null){
                    //修改为普通用户
                    userOperationService.deleteByUsername(userName,userDTO.getGroupId());
                    accountBot.sendMessage(sendMessage,"删除成功");
                }else if (byUsername==null){
                    accountBot.sendMessage(sendMessage,"未查询到此操作人!删除失败");
                }


            }
        }
    }
    public static String[] splitOperatorSkipFirst(String operator) {
        String[] parts = operator.split(" ");
        if (parts.length > 1) {
            return Arrays.copyOfRange(parts, 1, parts.length);
        }
        return new String[0];
    }
}
