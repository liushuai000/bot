package org.example.bot.accountBot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.example.bot.accountBot.dto.Merchant;
import org.example.bot.accountBot.dto.TronAccountDTO;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class RestTemplateConfig {
    @Value("${apiKey}")
    private String apiKey;//"2967e678-556e-497a-a892-a1dcee897ba5"
    @Value("${Origin}")
    private String Origin;
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 设置连接超时时间
        requestFactory.setReadTimeout(5000); // 设置读取超时时间

        return new RestTemplate(requestFactory);
    }
    private String getUserAgent(){
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";
        } else if (osName.contains("linux")) {
            return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";
        } else {
            return "Mozilla/5.0 (Unknown OS) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36";
        }
    }

    public List<Merchant> getForObjectMerchant(String url,Class clazz){
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", getUserAgent());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.set("Authorization", "Bearer YOUR_API_KEY"); // 如果需要 API 密钥
        ResponseEntity<Map> response = this.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
        ObjectMapper objectMapper = new ObjectMapper();
        List<Merchant> merchants = data.stream()
                .map(map -> objectMapper.convertValue(map, Merchant.class))
                .filter(Objects::nonNull).filter(c->!c.getUserName().equals("tg小程序专享"))
                .sorted(Comparator.comparing(Merchant::getPrice))
                .collect(Collectors.toList());
        return merchants;
    }

    public TronAccountDTO getForObjectTronAccount(String url){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        System.err.println("输出地址:"+url);
        httpGet.setHeader("Accept",  MediaType.APPLICATION_JSON_VALUE);
        httpGet.setHeader("TRON-PRO-API-KEY", apiKey);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        httpGet.setHeader("Origin", Origin);
        TronAccountDTO tronAccountDTO;
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            Gson gson = new Gson();
            tronAccountDTO = gson.fromJson(responseBody, TronAccountDTO.class);
            response.close();
            httpClient.close();
        }catch (Exception e){
            e.printStackTrace();
            return new TronAccountDTO();
        }
        return tronAccountDTO;
    }

    public List<TronHistoryDTO> getForObjectHistoryTrading(String url, Class clazz) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Accept",  MediaType.APPLICATION_JSON_VALUE);
        httpGet.setHeader("TRON-PRO-API-KEY", apiKey);
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        httpGet.setHeader("Origin", Origin);
        List<TronHistoryDTO> list;
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());

            Gson gson = new Gson();
            Map map = gson.fromJson(responseBody, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("token_transfers");
            Type tronHistoryDTOType = new TypeToken<List<TronHistoryDTO>>() {}.getType();
            list = gson.fromJson(gson.toJson(data), tronHistoryDTOType);
            response.close();
            httpClient.close();
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
        return list;

    }
}

