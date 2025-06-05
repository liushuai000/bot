package org.example.bot.accountBot.botConfig;

import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.pojo.UserNormal;
import org.example.bot.accountBot.pojo.UserOperation;
import org.example.bot.accountBot.service.UserOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

/**
 * 显示权限人 管理员
 */
@Service
public class PermissionUser {
    @Autowired
    private UserOperationService userOperationService;
    @Autowired
    private AccountBot accountBot;

    public void getPermissionUser(SendMessage sendMessage, UserDTO userDTO, User user1, UserNormal userNormalTempAdmin) {
        List<UserOperation> userOperations = userOperationService.selectByUsers(userDTO.getGroupId());
        StringBuilder stringBuilder=new StringBuilder("本群机器人最高权限管理员为：");
        String format = String.format("<a href=\"tg://user?id=%s\">@%s</a>", userNormalTempAdmin.getUserId(), userNormalTempAdmin.getUsername());
        stringBuilder.append(format).append("\n");
        stringBuilder.append("其他操作员为：");
        for (UserOperation userOperation : userOperations) {
            if (userOperation.getUserId().equals(userNormalTempAdmin.getUserId())){
                continue;
            }
            String format1 = String.format("<a href=\"tg://user?id=%s\">@%s</a>", userOperation.getUserId(), userOperation.getUsername());
            stringBuilder.append(format1).append(" ");
        }
        accountBot.sendMessage(sendMessage, stringBuilder.toString());
    }




}
