package org.example.bot.accountBot.botConfig;

import org.apache.commons.lang3.StringUtils;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.pojo.UserNormal;
import org.example.bot.accountBot.pojo.UserOperation;
import org.example.bot.accountBot.service.UserOperationService;
import org.example.bot.accountBot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 显示权限人 管理员
 */
@Service
public class PermissionUser {
    @Autowired
    private UserOperationService userOperationService;
    @Autowired
    private AccountBot accountBot;
    @Autowired
    private UserService userService;
    public void getPermissionUser(SendMessage sendMessage, UserDTO userDTO, User user1, UserNormal userNormalTempAdmin) {
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
            return;
        }
        //p -> p.getUserId() != null 这里过滤了掉用户名为空的用户
        List<User> userNormalList = users.stream().filter(p -> p.getUserId() != null).collect(Collectors.collectingAndThen(
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
        accountBot.sendMessage(sendMessage, sb.toString());


    }




}
