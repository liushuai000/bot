package org.example.bot.accountBot.botConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.UserDTO;
import org.example.bot.accountBot.mapper.StatusMapper;
import org.example.bot.accountBot.mapper.UserMapper;
import org.example.bot.accountBot.pojo.Status;
import org.example.bot.accountBot.pojo.User;
import org.example.bot.accountBot.pojo.UserNormal;
import org.example.bot.accountBot.pojo.UserOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 群内下发地址
 */
@Slf4j
@Service
public class DownAddress {
    @Autowired
    StatusMapper statusMapper;
    @Autowired
    AccountBot accountBot;
    @Autowired
    UserMapper userMapper;
    /**
     * 只有权限人跟操作员可以设置
     * 本群其他人发送地址的时候 机器人需要提醒
     * @param userDTO
     * @param status   userNormalTempAdmin (权限人)   userOperation(操作员)
     */
    public void downAddress(SendMessage sendMessage, UserDTO userDTO, Status status, UserNormal userNormalTempAdmin, UserOperation userOperation) {
        String text = userDTO.getText();
        String currentUserId = userDTO.getUserId();
        if (text.startsWith("设置下发地址") || text.startsWith("修改下发地址")){
            // 权限校验：只有权限人或操作员可以操作
            if (!currentUserId.equals(userNormalTempAdmin.getUserId()) &&
                    !currentUserId.equals(userOperation.getUserId()) && !userOperation.isOperation()) {
                accountBot.sendMessage(sendMessage, "⚠️ 你没有权限设置下发地址！");
                return;
            }
            String address =text.substring(6, text.length());
            if (address.length()!=34){
                accountBot.sendMessage(sendMessage,"❌地址错误!请检查地址");
                return;
            }
            status.setDAddress(address);
            status.setDTime(new Date());
            status.setDUserId(userDTO.getUserId());
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage,"✅设置下发地址成功\n\n当前下发地址: <code>"+status.getDAddress()+"</code>");
            return;
            //TTyYfnPDvmVbLDZoPDLnVr8gkC88888888 34位
        }
        if (text.startsWith("set the delivery address")){
            // 权限校验：只有权限人或操作员可以操作
            if (!currentUserId.equals(userNormalTempAdmin.getUserId()) &&
                    !currentUserId.equals(userOperation.getUserId()) && !userOperation.isOperation()) {
                accountBot.sendMessage(sendMessage, "⚠️ 你没有权限设置下发地址！");
                return;
            }
            String address =text.substring("set the delivery address".length(), text.length());
            if (address.length()!=34){
                accountBot.sendMessage(sendMessage,"❌地址错误!请检查地址");
                return;
            }
            status.setDAddress(address);
            status.setDTime(new Date());
            status.setDUserId(userDTO.getUserId());
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage,"✅设置下发地址成功\n\n当前下发地址: "+status.getDAddress());
            return;
            //TTyYfnPDvmVbLDZoPDLnVr8gkC88888888 34位
        }
        if (text.startsWith("modify the delivery address")){
            // 权限校验：只有权限人或操作员可以操作
            if (!currentUserId.equals(userNormalTempAdmin.getUserId()) &&
                    !currentUserId.equals(userOperation.getUserId()) && !userOperation.isOperation()) {
                accountBot.sendMessage(sendMessage, "⚠️ 你没有权限设置下发地址！");
                return;
            }
            String address =text.substring("modify the delivery address".length(), text.length());
            if (address.length()!=34){
                accountBot.sendMessage(sendMessage,"❌地址错误!请检查地址");
                return;
            }
            status.setDAddress(address);
            status.setDTime(new Date());
            status.setDUserId(userDTO.getUserId());
            statusMapper.updateById(status);
            accountBot.sendMessage(sendMessage,"✅设置下发地址成功\n\n当前下发地址: "+status.getDAddress());
            return;
        }
    }

    public boolean viewAddress(String text,SendMessage sendMessage,Status status){
        text = text.toLowerCase();
        if (text.startsWith("查看下发地址") || text.startsWith("下发地址")){
            if (status.getDAddress()==null){
                accountBot.sendMessage(sendMessage,"❌本群没有设置下发地址!");
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", status.getDUserId()));
            String firstLastName = user.getFirstLastName();
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(user.getUserId()), firstLastName);
            String a="本群唯一下发地址:\n\n<code>"+status.getDAddress()+"</code>\n\n设置时间:"
                    +sdf.format(status.getDTime())+"\n\n设置人:"+format+"\n\n点击复制";
            accountBot.sendMessage(sendMessage,a);
            return false;
        }
        if (text.startsWith("view the sending address") || text.startsWith("sending address")){
            if (status.getDAddress()==null){
                accountBot.sendMessage(sendMessage,"❌本群没有设置下发地址!");
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", status.getDUserId()));
            String firstLastName = user.getFirstLastName();
            String format = String.format("<a href=\"tg://user?id=%d\">%s</a>", Long.parseLong(user.getUserId()), firstLastName);
            String a="本群唯一下发地址:\n\n<code>"+status.getDAddress()+"</code>\n\n设置时间:"
                    +sdf.format(status.getDTime())+"\n\n设置人:"+format+"\n\n点击复制";
            accountBot.sendMessage(sendMessage,a);
            return false;
        }
        return false;
    }

    /**
     * 用户发送任意内容，只要其中包含波场地址，就自动提取并校验是否与已设置的下发地址匹配
     * @param text 用户发送的消息内容（可为任意文本）
     * @param sendMessage 用于回复消息
     * @param status 当前群状态对象  false不继续 true继续
     */
    public boolean validAddress(String text, SendMessage sendMessage, Status status) {
        // 检查是否设置了下发地址
        if (status.getDAddress() == null || status.getDAddress().isEmpty()) {
//            accountBot.sendMessage(sendMessage,"❌本群没有设置下发地址!");
            return false;
        }
        String targetAddress = status.getDAddress();
        // 使用正则表达式提取所有可能的 TRON 地址
        List<String> candidates = extractTronAddresses(text);
        for (String candidate : candidates) {
            if (candidate.equals(targetAddress)) {
                sendSuccessMessage(sendMessage, status);
                return true;
            }
        }
        sendMismatchMessage(sendMessage, status);
        return false;
    }


    /**
     * 提取文本中所有符合 TRON 地址格式的候选地址
     * @param text 用户输入的文本
     * @return List<String> 所有可能的 TRON 地址
     */
    private List<String> extractTronAddresses(String text) {
        List<String> addresses = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return addresses;
        }
        // 正则表达式：匹配所有长度为34位且以 T 开头的 Base58 字符串
        String regex = "\\b[Tt][1-9A-HJ-NP-Za-km-z]{33}\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String address = matcher.group();
            if (isTronAddress(address)) {
                addresses.add(address);
            }
        }
        return addresses;
    }

    /**
     * 判断是否为合法的 TRON 地址（TRC20）
     * @param address
     * @return boolean
     */
    public boolean isTronAddress(String address) {
        if (address == null || address.length() != 34) {
            return false;
        }
        // TRON 地址必须以 T 开头（大小写均可）
        if (!address.startsWith("T") && !address.startsWith("t")) {
            return false;
        }
        // 定义 Base58 字符集（排除 0, O, I, l）
        String base58Regex = "^[1-9A-HJ-NP-Za-km-z]+$";
        return address.matches(base58Regex);
    }
    // 成功提示
    private void sendSuccessMessage(SendMessage sendMessage, Status status) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", status.getDUserId()));
        String firstLastName = user.getFirstLastName();
        String format = String.format("<a href=\"tg://user?id=%d\">@%s</a>", Long.parseLong(user.getUserId()), firstLastName);
        String success = "\uD83D\uDD14下发地址提醒\uD83D\uDD14\n" +
                "\n" +
                "客户发送地址与设置地址吻合✅\n" +
                "请客户与操作员检查下发地址\uD83D\uDD0D\n" +
                "\n" +
                "本群地址\n" +
                "\n" +
                "<code>" + status.getDAddress() + "</code>" + "\n" +
                "\n" +
                "设置人 " + format;
        accountBot.sendMessage(sendMessage, success);
    }
    // 不匹配提示
    private void sendMismatchMessage(SendMessage sendMessage, Status status) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", status.getDUserId()));
        String firstLastName = user.getFirstLastName();
        String format = String.format("<a href=\"tg://user?id=%d\">@%s</a>", Long.parseLong(user.getUserId()), firstLastName);
        String err = "\uD83D\uDD14下发地址提醒\uD83D\uDD14\n" +
                "\n" +
                "客户发送地址与设置地址异常❌\n" +
                "请客户与操作员检查下发地址\uD83D\uDD0D\n" +
                "\n" +
                "本群地址\n" +
                "\n" +
                "<code>" + status.getDAddress() + "</code>" + "\n" +
                "\n" +
                "设置人 " + format;
        accountBot.sendMessage(sendMessage, err);
        return ;
    }


}
