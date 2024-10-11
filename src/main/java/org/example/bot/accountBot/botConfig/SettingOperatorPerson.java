package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.service.RateService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
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
    @Value("${adminUserName}")
    protected String adminUserName;
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
            userService.deleteHandler(deleteName);
            accountBot.sendMessage(sendMessage,"删除成功");
        }
    }
    /**
     * 设置操作人员
     * @param split1 传输的文本 是否是 设置操作员
     * @param userName 用户名 chishui_id 这玩意是用户id
     * @param firstName 赤水 是用户名
     * @param userList 获取操作人列表
     * @param sendMessage 发生的消息
     * @param message 消息
     * @param callBackName 回复人的名称
     * @param callBackFirstName 回复人的昵称
     * @param text  消息文本
     */
    public void setHandle(String[] split1, String userName, String firstName, List<User> userList,
                           SendMessage sendMessage, Message message, String callBackName,
                           String callBackFirstName, String text) {
        ButtonList buttonList = new ButtonList();
        if (userList.stream().anyMatch(user -> Objects.equals(user.getUsername(), firstName))){
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"已设置该操作员无需重复设置");
        }else if (split1[0].equals("设置操作员")||split1[0].equals("设置操作人")){
            if (!userList.isEmpty()&&userName.equals(userList.get(0).getUsername())||userName.equals(adminUserName)){//群内最高权限人发送：显示操作人后，机器人会统计出此群组内的操作员
                User user = new User();
                if (callBackName!=null){
                    user.setUsername(callBackName);
                    user.setFirstName(callBackFirstName);
                    if (null==userService.findByUsername(userName)){
                        userService.insertUser(user);
                    }
                }else {
                    Pattern pattern = Pattern.compile("@(\\w+)");
                    Matcher matcher = pattern.matcher(text);
                    List<String> usernames = new ArrayList<>();
                    while (matcher.find()) {
                        // 将匹配到的用户名添加到列表中
                        usernames.add(matcher.group(1));
                    }
                    if (userService.findByUsernames(usernames).isEmpty()){
                        for (String users : usernames) {
                            user.setUsername(users);
                            userService.insertUser(user);
                        }
                    }
                }
//                buttonList.implList(message, sendMessage);
                accountBot.sendMessage(sendMessage,"设置成功");
            }
        }else if (split1[0].equals("显示操作人")||split1[0].equals("显示操作员")){
//            int size = userList.size();
            StringBuilder sb = new StringBuilder("当前操作人: ");
            sb.append(" @");
            for (int i = 0; i < userList.size(); i++) {
                sb.append(userList.get(i).getUsername());
                if (i < userList.size() - 1) {
                    sb.append(" @");
                }
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
        }else if (split1[0].equals("将回复人显示")){
            rateService.updateCallBackStatus(0);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("关闭回复人显示")){
            rateService.updateCallBackStatus(1);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示明细")){
            rateService.updateDetailStatus(0);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("隐藏明细")){
            rateService.updateDetailStatus(1);
            buttonList.implList(message, sendMessage);
            accountBot.sendMessage(sendMessage,"操作成功");
        }else if (split1[0].equals("显示操作人名称")||split1[0].equals("显示操作人名字")){
            rateService.updateHandleStatus(1);
        }else if (split1[0].equals("隐藏操作人名称")||split1[0].equals("隐藏操作人名字")){
            rateService.updateHandleStatus(0);
        }
    }
}
