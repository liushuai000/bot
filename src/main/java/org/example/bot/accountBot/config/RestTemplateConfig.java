package org.example.bot.accountBot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bot.accountBot.dto.Merchant;
import org.example.bot.accountBot.dto.TronAccountDTO;
import org.example.bot.accountBot.dto.TronHistoryDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class RestTemplateConfig {
    private String apiKey="2967e678-556e-497a-a892-a1dcee897ba5";

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // 设置连接超时时间
        requestFactory.setReadTimeout(5000); // 设置读取超时时间

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        // 添加请求拦截器
        restTemplate.setInterceptors(Collections.singletonList(new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
                // 根据操作系统类型设置 User-Agent
                String userAgent = getUserAgent();
                request.getHeaders().set("User-Agent", userAgent);
                request.getHeaders().set("Accept", "application/json");
                // 执行请求
                ClientHttpResponse response = execution.execute(request, body);
                // 修改响应头
                response.getHeaders().remove("X-UA-Compatible");
                response.getHeaders().remove("X-Frame-Options");
                response.getHeaders().remove("X-Robots-Tag");
                return response;
            }
        }));
        // 设置响应的字符编码
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
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

    public TronAccountDTO getForObjectTronAccount(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", getUserAgent());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("TRON-PRO-API-KEY", apiKey);
//        headers.set("Authorization", "Bearer YOUR_API_KEY"); // 如果需要 API 密钥
        ResponseEntity<Map> response = this.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        return new ObjectMapper().convertValue(response.getBody(), TronAccountDTO.class);
    }

    public List<TronHistoryDTO> getForObjectHistoryTrading(String url, Class clazz) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", getUserAgent());
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.set("Authorization", "Bearer YOUR_API_KEY"); // 如果需要 API 密钥
        ResponseEntity<Map> response = this.restTemplate().exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map body = response.getBody();
        List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("token_transfers");
        ObjectMapper objectMapper = new ObjectMapper();
        return data.stream().map(map -> objectMapper.convertValue(map, TronHistoryDTO.class)).collect(Collectors.toList());
    }
}

