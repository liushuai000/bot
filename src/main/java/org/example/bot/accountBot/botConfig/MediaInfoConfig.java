package org.example.bot.accountBot.botConfig;

import lombok.extern.slf4j.Slf4j;
import org.example.bot.accountBot.dto.FileItemDTO;
import org.example.bot.accountBot.utils.StyleText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MediaInfoConfig {
    @Autowired
    @Lazy
    private AccountBot groupManageBot;
    StyleText styleText=new StyleText();

    public <T extends BotFileItemInfo> void sendCombinedMessageGeneric(String chatId, SendMessage sendMessage, List<T> fileItemList) {
        List<InputMedia> mediaList = new ArrayList<>();
        boolean isFirst = true;
        for (T fileItem : fileItemList) {
            String url = fileItem.getUrl();
            if (url != null && !url.isEmpty()) {
                String type = fileItem.getType();
                if (type != null && !type.isEmpty()) {
                    if (type.startsWith("image/")) {
                        InputMediaPhoto photo = new InputMediaPhoto();
                        photo.setMedia(url);
                        if (isFirst) {
                            photo.setCaption(styleText.cleanHtmlExceptSpecificTags(sendMessage.getText())); // 只有第一个媒体文件设置 caption
                            photo.setParseMode("HTML");
                            isFirst = false;
                        }
                        mediaList.add(photo);
                    } else if (type.startsWith("video/")) {
                        InputMediaVideo video = new InputMediaVideo();
                        video.setMedia(url);
                        if (isFirst) {
                            video.setCaption(styleText.cleanHtmlExceptSpecificTags(sendMessage.getText())); // 只有第一个媒体文件设置 caption
                            video.setParseMode("HTML");
                            isFirst = false;
                        }
                        mediaList.add(video);
                    }
                }
            }
        }
        if (!mediaList.isEmpty()) {
            if (mediaList.size() == 1) {
                // 如果只有一个文件，调用方法
                sendSingleMediaFileIsDelete(chatId, sendMessage, fileItemList.get(0).getUrl(), fileItemList.get(0).getType());
            } else if (mediaList.size() >= 2 && mediaList.size() <= 10) {
                SendMediaGroup sendMediaGroup = new SendMediaGroup();
                sendMediaGroup.setChatId(chatId);
                sendMediaGroup.setMedias(mediaList);
                groupManageBot.sendMediaGroup(sendMediaGroup);
            } else {
                log.error("媒体数量必须在2到10之间，当前数量: {}", mediaList.size());
            }
        }
    }
    //兼容传递url 和类型来发送消息  删除消息
    protected Integer sendSingleMediaFileIsDelete(String chatId, SendMessage sendMessage, String url, String type) {
        if (url != null && !url.isEmpty()) {
            if (type != null && !type.isEmpty()) {
                if (type.startsWith("image/")) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setPhoto(new InputFile(url));
                    sendPhoto.setCaption(styleText.cleanHtmlExceptSpecificTags(sendMessage.getText())); // 设置文本作为 caption
                    sendPhoto.setParseMode("HTML"); // 显式启用 HTML 解析模式
                    sendPhoto.setReplyMarkup(sendMessage.getReplyMarkup());
                    Integer returnMessageId = groupManageBot.sendPhotoIsDelete(sendPhoto);
                    return returnMessageId;
                } else if (type.startsWith("video/")) {
                    SendVideo sendVideo = new SendVideo();
                    sendVideo.setChatId(chatId);
                    sendVideo.setVideo(new InputFile(url));
                    sendVideo.setCaption(styleText.cleanHtmlExceptSpecificTags(sendMessage.getText())); // 设置文本作为 caption
                    sendVideo.setParseMode("HTML"); // 显式启用 HTML 解析模式
                    sendVideo.setReplyMarkup(sendMessage.getReplyMarkup());
                    Integer returnMessageId =groupManageBot.sendVideoIsDelete(sendVideo);
                    return returnMessageId;
                } else if (type.startsWith("photo/")) {
                    SendPhoto sendPhoto = new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setPhoto(new InputFile(url));
                    sendPhoto.setCaption(styleText.cleanHtmlExceptSpecificTags(sendMessage.getText())); // 设置文本作为 caption
                    sendPhoto.setParseMode("HTML"); // 显式启用 HTML 解析模式
                    sendPhoto.setReplyMarkup(sendMessage.getReplyMarkup());
                    Integer returnMessageId =groupManageBot.sendPhotoIsDelete(sendPhoto);
                    return returnMessageId;
                }
            }
        }
        return null;
    }
}
