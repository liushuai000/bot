package org.example.bot.accountBot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bot.accountBot.dto.Merchant;
import org.example.bot.accountBot.dto.TronAccountDTO;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class RestTemplateConfig {


    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 设置连接超时时间
        requestFactory.setReadTimeout(5000); // 设置读取超时时间

        return new RestTemplate(requestFactory);
    }


    public List<Merchant> getForObjectMerchant(String url,Class clazz){
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
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

    public TronAccountDTO getForObjectTronAccount(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.set("Authorization", "Bearer YOUR_API_KEY"); // 如果需要 API 密钥
        ResponseEntity<Map> response = this.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return new ObjectMapper().convertValue(response.getBody(), TronAccountDTO.class);
    }

    public List<TronHistoryDTO> getForObjectHistoryTrading(String url, Class clazz) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.set("Authorization", "Bearer YOUR_API_KEY"); // 如果需要 API 密钥
        ResponseEntity<Map> response = this.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map body = response.getBody();
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("token_transfers");
        ObjectMapper objectMapper = new ObjectMapper();
        return data.stream().map(map -> objectMapper.convertValue(map, TronHistoryDTO.class)).collect(Collectors.toList());
    }
}

