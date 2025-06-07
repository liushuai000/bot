package org.example.bot.accountBot.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationExample {

    private static final String APP_KEY = "2a7ebf111b16f79e"; // 替换为你的APP KEY
    private static final String SECRET = "fFJIfTzH4BnuSB3wManZqPKoe2vH14IG"; // 替换为你的密钥
    private static final String[] tagsToKeep = {"<strong>", "</strong>", "<code>", "</code>", "<pre>", "</pre>", "<em>", "</em>", "<a ", "</a>"};

    public static String translateText(String text, boolean newLanguage) {
        if (newLanguage) {
            return text;
        }
        if (text.equals("切换语言成功！")) {
            return text;
        }
        try {
            StringBuilder inputBuilder = new StringBuilder(text);
            Map<String, String> placeholders = extractLinks(inputBuilder);
            String translatedText = translateTextWithoutTags(inputBuilder.toString(), newLanguage);
            translatedText=translatedText.replaceAll("<a href=\"tg://user\\?\" id=(\\d+)\">", "<a href=\"tg://user?id=$1\">");//中文状态翻译问题
            String finalText = restoreLinks(translatedText, placeholders);
            return normalizeTelegramLinks(finalText);//英文
        }catch (Exception e){
            return e.getMessage();
        }
    }

    public static Map<String, String> extractLinks(StringBuilder text) {
        Map<String, String> placeholders = new LinkedHashMap<>();

        // 仅匹配 href 以 http 或 https 开头的链接
        Pattern pattern = Pattern.compile("<a\\s+href=\"(https?://[^\"]+)\">([^<]*)</a>");
        Matcher matcher = pattern.matcher(text);
        int index = 0;

        while (matcher.find()) {
            String href = matcher.group(1);
            String inner = matcher.group(2);
            String placeholder = "-" + index ;
            String fullMatch = matcher.group(0);

            placeholders.put(placeholder, "<a href=\"" + href + "\">" + inner + "</a>");
            int start = matcher.start();
            int end = matcher.end();
            text.replace(start, end, placeholder);

            matcher = pattern.matcher(text); // 重建 matcher，因为 text 被修改了
            index++;
        }

        return placeholders;
    }


    // 还原翻译后的文本中的占位符为原始HTML
    public static String restoreLinks(String translatedText, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            translatedText = translatedText.replace(entry.getKey(), " "+entry.getValue());
        }
        return translatedText;
    }

    public static String normalizeTelegramLinks(String input) {
        // 匹配被破坏的 tg 链接
        Pattern pattern = Pattern.compile(
                "<\\s*a\\s+href\\s*=\\s*\"tg\\s*:\\s*/\\s*/\\s*user\\s*\\?\\s*id\\s*=\\s*(\\d+)\\s*\"\\s*>",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String userId = matcher.group(1);
            String fixed = "<a href=\"tg://user?id=" + userId + "\">";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(fixed));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String translateTextWithoutTags(String text, boolean newLanguage) throws Exception {
        String from = newLanguage ? "en" : "zh-CHS"; // 源语言
        String to = newLanguage ? "zh-CHS" : "en"; // 目标语言
        long salt = System.currentTimeMillis();
        String str1 = APP_KEY + text + salt + SECRET;
        String sign = MD5(str1); // 计算签名
        String urlStr = "https://openapi.youdao.com/api?q=" + URLEncoder.encode(text, "UTF-8") + "&from="
                + from + "&to=" + to + "&appKey=" + APP_KEY + "&salt=" + salt + "&sign=" + sign;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // 解析 JSON 结果
        return parseAndCheckResponse(response.toString());
    }

    public static String parseAndCheckResponse(String jsonResponse) throws Exception {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
        String errorCode = jsonObject.get("errorCode").getAsString();
        if ("0".equals(errorCode)) {
            // 请求成功
            return jsonObject.getAsJsonArray("translation").get(0).getAsString();
        } else {
            // 请求失败
            throw new Exception("请求失败，错误码: " + errorCode);
        }
    }

    public static String MD5(String str) {
        try {
            // 获取MD5实例
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算MD5摘要
            byte[] messageDigest = md.digest(str.getBytes());
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
