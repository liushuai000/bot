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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationExample {

    private static final String APP_KEY = "2a7ebf111b16f79e"; // 替换为你的APP KEY
    private static final String SECRET = "fFJIfTzH4BnuSB3wManZqPKoe2vH14IG"; // 替换为你的密钥
    private static final String[] tagsToKeep = {"<strong>", "</strong>", "<code>", "</code>", "<pre>", "</pre>", "<em>", "</em>", "<a ", "</a>"};
    public static void main(String[] args) throws Exception {
        boolean newLanguage = false;
        String query = "<strong> \uD83D\uDED2选择你需要的商品：\n" +
                "\uFE0F没使用过本店商品的，请先少量购买测试，以免造成不必要的争执！谢谢合作 </strong>";
        // 使用占位符替换HTML标签
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("__tag_placeholder_0__", "<strong>");
        placeholders.put("__tag_placeholder_1__", "</strong>");
        String queryWithPlaceholders = query.replace("<strong>", "__tag_placeholder_0__")
                .replace("</strong>", "__tag_placeholder_1__");

        // 假设translate方法是你的翻译服务
        String translatedQuery = translateText(queryWithPlaceholders, newLanguage);
        // 恢复HTML标签
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            translatedQuery = translatedQuery.replace(entry.getKey(), entry.getValue());
        }
        System.out.println(translatedQuery);
    }

    public static String translateText(String text, boolean newLanguage) {
        if (newLanguage){//不翻译中文
            return text;
        }
        if (text.equals("切换语言成功！")){
            return text;
        }
        try {
            String translatedText = translateTextWithoutTags(text, newLanguage);
            return translatedText.replaceAll("<a href=\"tg://user\\?\" id=(\\d+)\">", "<a href=\"tg://user?id=$1\">");
        }catch (Exception e){
            return e.getMessage();
        }
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
