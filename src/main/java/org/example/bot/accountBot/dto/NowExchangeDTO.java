package org.example.bot.accountBot.dto;

import lombok.Data;

import java.util.List;
@Data
public class NowExchangeDTO {
    Integer code;
    String message;
    Integer totalCount;
    Integer pageSize;
    Integer totalPage;
    Integer currPage;
    List<Merchant> data;
    Boolean success;
}
