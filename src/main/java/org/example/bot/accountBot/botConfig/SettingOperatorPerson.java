package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.StatusService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设置操作人员
 */

@Slf4j
@Service
public class SettingOperatorPerson{
    @Autowired
    UserService userService;
    @Autowired
    RateService rateService;
    @Autowired
    StatusService statusService;
    @Autowired
    AccountBot accountBot;
    @Value("${telegram.bot.username}")
    protected String username;
    @Value("${adminUserId}")
    protected String adminUserId;
    /**
     * 设置操作人员
     * @param split1 传输的文本 是否是 设置操作员
     * @param userDTO 封装的用户信息
     * @param sendMessage 发生的消息
     * @param message 消息
     * @param text  消息文本 6976772117
     */
    public void setHandle(String[] split1, SendMessage sendMessage, Message message, String text, UserDTO userDTO, User user1, Status status) {
        ButtonList buttonList = new ButtonList();
        boolean isShowAdminMessage = false;
        if (split1[0].equals("设置操作员")||split1[0].equals("设置操作人")){
            if (user1.isNormal()){
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
                    User user2 = userService.findByUsername(usernameTemp);
                    if (userDTO.getUsername().equals(adminUserId) ||user2!=null && usernameTemp.equals(user2.getUsername())){
                        if (user2.isOperation()){//是操作员
                            isShowAdminMessage = true;
                        }else {
                            user2.setOperation(true);//是操作员
                            user2.setSuperiorsUserId(userDTO.getUserId());
                            userService.updateUserByOperation(user2);
                        }
                    }else {
                        User user = new User();
                        user.setOperation(true);//是操作员
                        user.setSuperiorsUserId(userDTO.getUserId());
                        user.setUsername(usernameTemp);
                        userService.insertUser(user);
                    }
                }
                //回复用
            }else if ( !compile.matcher(text).find()){
                User callBackUser = userService.findByUserId(userDTO.getCallBackUserId());
                User user2 = userService.findByUsername(userDTO.getCallBackName());;
                if (callBackUser!=null){
                    if (callBackUser.isOperation()) {
                        isShowAdminMessage = true;
                    }else {
                        callBackUser.setUserId(userDTO.getCallBackUserId());
                        callBackUser.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        callBackUser.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                        callBackUser.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                        callBackUser.setOperation(true);
                        callBackUser.setSuperiorsUserId(userDTO.getUserId());
                        userService.updateUserByOperation(callBackUser);
                    }
                } else if (user2!=null) {
                    if (user2.isOperation()) {
                        isShowAdminMessage = true;
                    }else {
                        user2.setUserId(userDTO.getCallBackUserId());
                        user2.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        user2.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                        user2.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                        user2.setOperation(true);
                        user2.setSuperiorsUserId(userDTO.getUserId());
                        userService.updateUserByOperation(user2);
                    }
                }else {
                    User user = new User();
                    user.setOperation(true);//设置操作员
                    user.setSuperiorsUserId(userDTO.getUserId());
                    user.setUserId(userDTO.getCallBackUserId());
                    user.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                    user.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                    user.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                    userService.insertUser(user);
                }
            }
            if (!isShowAdminMessage){
                accountBot.sendMessage(sendMessage,"设置成功");
            }else {
                accountBot.sendMessage(sendMessage,"已设置该操作员无需重复设置");
            }
        }else if (split1[0].equals("显示操作人")||split1[0].equals("显示操作员")){
            StringBuilder sb = new StringBuilder("当前操作人: ");
            boolean flag = false;
            List<User> userNormalList =userService.selectByNormal(flag);
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
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,sb.toString());
        }else if (split1[0].equals("显示手续费")){
            status.setShowHandlerMoneyStatus(0);
            statusService.updateStatus("show_handler_money_status"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏手续费")){
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
        }else if (split1[0].contains("设置下发单笔手续费")||split1[0].contains("设置单笔下发手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(9, split1[0].length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置下发手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置下发手续费".length(), split1[0].length())));
            status.setIssueHandlerMoney(money);
            statusService.updateMoneyStatus("issue_handler_money"     ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置入款单笔手续费")||split1[0].contains("设置单笔入款手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring(9, split1[0].length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].contains("设置入款手续费")){
            BigDecimal money=BigDecimal.valueOf(Long.parseLong(split1[0].substring("设置入款手续费".length(), split1[0].length())));
            status.setAccountHandlerMoney(money);
            statusService.updateMoneyStatus("account_handler_money"    ,money, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示分类")){
            status.setDisplaySort(0);
            statusService.updateStatus("display_sort"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏分类")){
            status.setDisplaySort(1);
            statusService.updateStatus("display_sort"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("将操作员显示")||split1[0].equals("显示操作人名称")||split1[0].equals("显示操作人名字")){
            status.setHandleStatus(0);
            status.setCallBackStatus(1);
            statusService.updateStatus("handle_status"    ,0, userDTO.getGroupId());
            statusService.updateStatus("call_back_status", 1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("关闭显示")||split1[0].equals("隐藏操作人名称")||split1[0].equals("隐藏操作人名字")
                ||split1[0].equals("隐藏名字")||split1[0].equals("隐藏名称")){
            status.setHandleStatus(1);
            statusService.updateStatus("handle_status"    ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("将回复人显示")||split1[0].equals("显示回复人名称")){
            status.setHandleStatus(1);
            status.setCallBackStatus(0);
            statusService.updateStatus("handle_status"     ,1, userDTO.getGroupId());
            statusService.updateStatus("call_back_status" , 0, userDTO.getGroupId());
        }else if (split1[0].equals("关闭回复人显示")||split1[0].equals("隐藏回复人显示")){
            status.setCallBackStatus(1);
            statusService.updateStatus("call_back_status" , 1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示明细")){
            status.setDetailStatus(0);
            statusService.updateStatus("detail_status"     ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏明细")){
            status.setDetailStatus(1);
            statusService.updateStatus("detail_status"     ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示余额")||split1[0].equals("显示金额")) {
            status.setShowMoneyStatus(0);
            statusService.updateStatus("show_money_status"  ,0, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].equals("显示USDT")||split1[0].equals("显示usdt")) {
            status.setShowMoneyStatus(1);
            statusService.updateStatus("show_money_status"  ,1, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].equals("显示全部")) {
            status.setShowMoneyStatus(2);
            statusService.updateStatus("show_money_status"  ,2, userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }else if (split1[0].equals("显示1条")||split1[0].equals("显示3条")||split1[0].equals("显示5条")) {
            int i = Integer.parseInt(split1[0].substring(2,3));
            status.setShowFew(i);
            statusService.updateStatus("show_few"            ,i , userDTO.getGroupId());
            accountBot.sendMessage(sendMessage, "操作成功");
        }
    }

    //删除操作人员
    public void deleteHandle(String text,SendMessage sendMessage) {
        if (text.length()<4){
            return;
        }
        log.info("text:{}",text);
        String[] split = text.split(" ");
        if (split[0].equals("删除操作员")||split[0].equals("删除操作人")){
            String deleteName = split[1].substring(1);
            log.info("删除操作员:{}",deleteName);
//            userService.deleteHandler(deleteName);
            //修改为普通用户
            userService.updateIsNormal(true,deleteName, userService.findByUsername(deleteName));
            accountBot.sendMessage(sendMessage,"删除成功");
        }
    }
}
