package org.example.bot.accountBot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.TronAccountDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@ComponentScan(basePackages = {"org.example.bot.accountBot.service"})
public class Main {
    public static void main(String[] args) throws IOException {
        String url = "https://apilist.tronscanapi.com/api/accountv2?address=TWxokzzX2Y68iVdhZg8LbJWp9bjsj8w9Pc";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("TRON-PRO-API-KEY", "2967e678-556e-497a-a892-a1dcee897ba5");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        httpGet.setHeader("Origin", "http://www.yaoke.cc:8081");

        CloseableHttpResponse response = httpClient.execute(httpGet);

        String responseBody = EntityUtils.toString(response.getEntity());
        Gson gson = new Gson();
        TronAccountDTO tronAccountDTO1 = gson.fromJson(responseBody, TronAccountDTO.class);

        TronAccountDTO tronAccountDTO = new ObjectMapper().convertValue(responseBody, TronAccountDTO.class);
        System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
        System.out.println("Response Body: " + responseBody);

        response.close();
        httpClient.close();
    }
}
