package org.example.bot.accountBot.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleText {

    public String cleanHtmlExceptSpecificTags(String text) {
        if (text == null) return "";
        // 替换 <p> 和 <br> 为换行符
//        text = text.replaceAll("<p[^>]*>(\\s|<(br|/br)|&nbsp;)*</p>", "");
        text = text.replaceAll("<p[^>]*><br[^>]*></p>", "<br>");
        text = text.replaceAll("<p[^>]*>", "\n");
        text = text.replaceAll("</p>", "");
        text = text.replaceAll("<br[^>]*>", "\n");
        // 定义需要保留的标签
        String[] tagsToKeep = {"<strong>", "</strong>", "<code>", "</code>", "<pre>", "</pre>", "<em>", "</em>", "<a ", "</a>"};
        // 创建一个临时字符串，将需要保留的标签替换为占位符
        for (int i = 0; i < tagsToKeep.length; i++) {
            text = text.replace(tagsToKeep[i], "\uFFFD" + i + "\uFFFE");
        }
        // 处理 &nbsp; 实体，替换为空格
        text = text.replace("&nbsp;", " ");
        // 使用正则表达式提取并保留 href 属性
        Pattern hrefPattern = Pattern.compile("<a\\s+([^>]*?href=\"[^\"]*?\")[^>]*>");
        Matcher hrefMatcher = hrefPattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (hrefMatcher.find()) {
            hrefMatcher.appendReplacement(sb, "<a $1>");
        }
        hrefMatcher.appendTail(sb);
        text = sb.toString();
        // 使用正则表达式去除所有 HTML 标签
        text = text.replaceAll("<[^>]*>", "");
        // 将占位符替换回原来的标签
        for (int i = 0; i < tagsToKeep.length; i++) {
            text = text.replace("\uFFFD" + i + "\uFFFE", tagsToKeep[i]);
        }
        return text;
    }
}
