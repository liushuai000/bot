package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    AccountBot accountBot;
    @Value("${telegram.bot.username}")
    protected String username;
    @Value("${adminUserId}")
    protected String adminUserId;
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


    /**
     * 设置操作人员
     * @param split1 传输的文本 是否是 设置操作员
     * @param userDTO 封装的用户信息
     * @param userList 获取操作人列表
     * @param sendMessage 发生的消息
     * @param message 消息
     * @param text  消息文本 6976772117
     */
    public void setHandle(String[] split1, List<User> userList, SendMessage sendMessage, Message message, String text, UserDTO userDTO) {
        ButtonList buttonList = new ButtonList();
        boolean isShowAdminMessage = false;
        if (split1[0].equals("设置操作员")||split1[0].equals("设置操作人")){
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
                        if (!user2.isNormal()){
                            isShowAdminMessage = true;
                        }else {
                            user2.setNormal(false);
                            userService.updateUserByNormal(user2);
                        }
                    }else {
                        User user = new User();
                        user.setNormal(false);//是否是普通用户
                        user.setUsername(usernameTemp);
                        userService.insertUser(user);
                    }
                }
                //回复用
            }else if ( !compile.matcher(text).find()){
                User callBackUser = userService.findByUserId(userDTO.getCallBackUserId());
                User user2 = userService.findByUsername(userDTO.getCallBackName());;
                if (callBackUser!=null){
                    if (!callBackUser.isNormal()) {
                        isShowAdminMessage = true;
                    }else {
                        callBackUser.setUserId(userDTO.getCallBackUserId());
                        callBackUser.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        callBackUser.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                        callBackUser.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                        callBackUser.setNormal(false);
                        userService.updateUserByNormal(callBackUser);
                    }
                } else if (user2!=null) {
                    if (!user2.isNormal()) {
                        isShowAdminMessage = true;
                    }else {
                        user2.setUserId(userDTO.getCallBackUserId());
                        user2.setUsername(userDTO.getCallBackName()==null?"":userDTO.getCallBackName());
                        user2.setFirstName(userDTO.getCallBackFirstName()==null?"":userDTO.getCallBackFirstName());
                        user2.setLastName(userDTO.getCallBackLastName()==null?"":userDTO.getCallBackLastName());
                        user2.setNormal(false);
                        userService.updateUserByNormal(user2);
                    }
                }else {
                    User user = new User();
                    user.setNormal(false);//是否是普通用户
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
        }else if (split1[0].equals("将操作员显示")){
            rateService.updateHandleStatus(0);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("关闭显示")){
            rateService.updateHandleStatus(1);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("将回复人显示")||split1[0].equals("显示回复人名称")){
            rateService.updateCallBackStatus(0);
        }else if (split1[0].equals("关闭回复人显示")){
            rateService.updateCallBackStatus(1);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示明细")){
            rateService.updateDetailStatus(0);
//            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏明细")){
            rateService.updateDetailStatus(1);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示操作人名称")||split1[0].equals("显示操作人名字")){
            rateService.updateHandleStatus(0);
        }else if (split1[0].equals("隐藏操作人名称")||split1[0].equals("隐藏操作人名字")
                    ||split1[0].equals("隐藏名字")||split1[0].equals("隐藏名称")){
            rateService.updateHandleStatus(1);
        }
    }

    /**
     * 判断群组内消息是否包含机器人识别的消息在回复
     * @param text
     * @return
     */
    public boolean getMessageContentIsContain(String text) {
        String[] array={"通知","设置日切","清理今天数据",
                "删除今天数据","清理今天账单","清理今日账单","删除今日账单","清理今天帐单","删除今天账单",
                "删除账单", "删除今天帐单","删除帐单","清除账单","删除账单","清除帐单",
                "删除帐单","删除全部账单","清除全部账单","关闭日切","显示操作人名字","显示操作人名称",
                "显示明细","+0","-0","+0u","-0u","+0U","-0U","设置费率",
                "设置汇率","设置入款单笔手续费","撤销入款","取消","撤销下发",
                "删除操作员",   "删除操作人",   "设置操作员",   "设置操作人",   "显示操作人",   "显示操作员",
                "将操作员显示","关闭显示",   "将回复人显示",
                "关闭回复人显示", "显示明细", "隐藏明细", "显示操作人名称","显示操作人名字","隐藏操作人名称","隐藏操作人名字"
        };
        boolean matches = containsAny(array, text);
        return matches;
    }
    public static boolean containsAny(String[] array, String input) {
        for (String str : array) {
            if (input.contains(str)) {
                return true; // 如果input包含array中的某个元素，则返回true
            }
        }
        return false; // 如果没有任何元素匹配，返回false
    }
}
