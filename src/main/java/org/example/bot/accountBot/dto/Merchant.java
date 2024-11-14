package org.example.bot.accountBot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Data
@JsonIgnoreProperties
public class Merchant {
    private Long id;
    private Long uid;
    private String userName;
    private Integer merchantLevel;
    private List<String> merchantTags;
    private Integer coinId;
    private Integer currency;
    private Integer tradeType;
    private Integer blockType;
    private String payMethod;
    @JsonIgnoreProperties
    private List<PayMethod> payMethods;
    private Integer payTerm;
    @JsonDeserialize(using =PayNameDeserializer.class)
    private List<PayName> payName;
    private Double minTradeLimit;
    private Double maxTradeLimit;
    private BigDecimal price;
    private Double tradeCount;
    private Boolean isOnline;
    private Boolean isFollowed;
    private Integer tradeMonthTimes;
    private Integer orderCompleteRate;
    private Integer takerAcceptOrder;
    private Double takerAcceptAmount;
    private Integer takerLimit;
    private Long gmtSort;
    private Boolean isCopyBlock;
    private Integer thumbUp;
    private Object seaViewRoom;
    private Boolean isTrade;
    private Integer totalTradeOrderCount;
    private String labelName;
    private Boolean isVerifyCapital;

    @Data
    @JsonIgnoreProperties
    public static class PayName {
        private Integer bankType;
        private String bankName; // 添加此字段
        private Long id;
    }
    @JsonIgnoreProperties
    @Data
    public static class PayMethod {
        private Integer payMethodId;
        private String name;
        private String color;
        private Boolean isRecommend;
    }
    @JsonIgnoreProperties
    public static class PayNameDeserializer extends JsonDeserializer<List<PayName>> {
        @Override
        public List<PayName> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value != null) {
                ObjectMapper mapper = new ObjectMapper();
                return Arrays.asList(mapper.readValue(value, PayName[].class));
            }
            return new ArrayList<>();
        }
    }
}

