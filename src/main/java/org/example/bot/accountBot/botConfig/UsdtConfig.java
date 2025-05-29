package org.example.bot.accountBot.botConfig;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UsdtConfig {

    private final OkHttpClient client = new OkHttpClient();
    private double getUsdtBalance(String address) throws IOException {
        String url = "https://apilist.tronscanapi.com/api/account/tokens?address=" + address;
        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JSONObject json = new JSONObject(body);
            JSONArray tokens = json.getJSONArray("data");
            for (int i = 0; i < tokens.size(); i++) {
                JSONObject token = tokens.getJSONObject(i);
                if ("USDT".equals(token.getStr("tokenAbbr"))) {
                    return token.getDouble("balance") / 1_000_000;
                }
            }
        }
        return 0.0;
    }

    private String fetchRealTimeUSDTPriceFromOKX(String type) {
        String url = "https://www.okx.com/v3/c2c/tradingOrders/books" +
                "?quoteCurrency=cny&baseCurrency=usdt&side=sell&paymentMethod="+type+"&userType=all&limit=10";
        Request request = new Request.Builder().url(url).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                JSONObject json = new JSONObject(body);
                JSONObject data1 = json.getJSONObject("data");
                JSONArray data = data1.getJSONArray("sell");
                StringBuilder result = new StringBuilder("\n");
                for (int i = 0; i < data.size(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    String price = item.getStr("price");
                    String name = item.getStr("nickName");
                    result.append(String.format("%d.) %s %s\n", i + 1, price, name));
                }
                return result.toString();
            } else {
                return "请求失败：" + response.code();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
