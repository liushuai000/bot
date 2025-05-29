package org.example.bot.accountBot.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StyleText {
    public Map<String, String> parseMixedText(String text) {
        // 使用正则表达式匹配字母和数字
        Pattern pattern = Pattern.compile("(?i)([a-z]+)(\\d+)|(?i)(\\d+)([a-z]+)");
        Matcher matcher = pattern.matcher(text);
        Map<String, String> result = new HashMap<>();
        if (matcher.find()) {
            // 匹配到字母在前的情况
            if (matcher.group(1) != null && matcher.group(2) != null) {
                result.put("letters", matcher.group(1).toUpperCase());
                result.put("lettersMin", matcher.group(1).toLowerCase());
            }
            // 匹配到数字在前的情况
            else if (matcher.group(3) != null && matcher.group(4) != null) {
                result.put("letters", matcher.group(4).toUpperCase());
                result.put("lettersMin", matcher.group(4).toLowerCase());
            }
        }

        return result;
    }
    public int extractNumber(String data) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            int number = Integer.parseInt(matcher.group());
            if (number >= 10) {
                return number;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
    public static List<String> extractUsernames(String text) {
        List<String> usernames = new ArrayList<>();
        // 正则表达式匹配以 @ 开头，后面跟着一个或多个非空格字符，直到下一个空格或字符串结束
        Pattern pattern = Pattern.compile(" @(\\S+)(?=\\s|$)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames;
    }




    public String cleanHtmlExceptSpecificTags(String text) {
        if (text == null) return "";
        // 定义需要保留的标签
        String[] tagsToKeep = {"<strong>", "</strong>", "<code>", "</code>", "<pre>", "</pre>", "<em>", "</em>", "<a ", "</a>"};
        // 创建一个临时字符串，将需要保留的标签替换为占位符
        for (int i = 0; i < tagsToKeep.length; i++) {
            text = text.replace(tagsToKeep[i], "\uFFFD" + i + "\uFFFE");
        }
        // 处理 <p> 和 </p> 标签，替换为换行符
        text = text.replace("<p>", "\n").replace("</p>", "");
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
