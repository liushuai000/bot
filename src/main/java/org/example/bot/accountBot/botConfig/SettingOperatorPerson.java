package org.example.bot.accountBot.botConfig;


import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.pojo.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SettingOperatorPerson extends accountBot {
    ButtonList buttonList=new ButtonList();
    //设置操作人员
    private void setHandle(String[] split1, String userName, String firstName, List<User> userList,
                           SendMessage sendMessage, Message message, String callBackName,
                           String callBackFirstName, String text) {
        if (userList.stream().anyMatch(user -> Objects.equals(user.getUsername(), firstName))){
            sendMessage.setText("已设置该操作员无需重复设置");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息4");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("设置操作员")||split1[0].equals("设置操作人")){
            if (!userList.isEmpty()&&userName.equals(userList.get(0).getUsername())){//群内最高权限人发送：显示操作人后，机器人会统计出此群组内的操作员
                User user = new User();
                if (callBackName!=null){
                    user.setUsername(callBackName);
                    user.setFirstname(callBackFirstName);
                    accService.insertUser(user);
                }else {
                    Pattern pattern = Pattern.compile("@(\\w+)");
                    Matcher matcher = pattern.matcher(text);
                    List<String> userLists = new ArrayList<>();
                    while (matcher.find()) {
                        // 将匹配到的用户名添加到列表中
                        userLists.add(matcher.group(1));
                    }

                    // 打印提取到的用户列表
                    for (String users : userLists) {
                        user.setUsername(users);
                        accService.insertUser(user);
                    }
                }
                sendMessage.setText("设置成功");
                buttonList.implList(message, sendMessage);
                try {
                    log.info("发送消息5");
                    execute(sendMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else if (split1[0].equals("显示操作人")||split1[0].equals("显示操作员")){
            int size = userList.size();
            StringBuilder sb = new StringBuilder("当前操作人: ");
            sb.append(" @");
            for (int i = 0; i < userList.size(); i++) {
                sb.append(userList.get(i).getUsername());
                if (i < userList.size() - 1) {
                    sb.append(" @");
                }
            }
            sendMessage.setText(sb.toString());
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息11");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("将操作员显示")){
            accService.updateHandleStatus(0);
            sendMessage.setText("操作成功");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("关闭显示")){
            accService.updateHandleStatus(1);
            sendMessage.setText("操作成功");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("将回复人显示")){
            accService.updateCallBackStatus(0);
            sendMessage.setText("操作成功");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("关闭回复人显示")){
            accService.updateCallBackStatus(1);
            sendMessage.setText("操作成功");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("显示明细")){
            accService.updateDatilStatus(0);
            sendMessage.setText("操作成功");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (split1[0].equals("隐藏明细")){
            accService.updateDatilStatus(1);
            sendMessage.setText("操作成功");
            buttonList.implList(message, sendMessage);
            try {
                log.info("发送消息5");
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
