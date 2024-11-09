package org.example.bot.accountBot.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.example.bot.accountBot.config.RestTemplateConfig;
import org.example.bot.accountBot.dto.QueryType;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

@Api(tags="账单查询请求")
@RestController
@RequestMapping("api")
public class AccountController {
    @Autowired
    AccountService accountService;

    //通知功能有bug      根据群组id查
//    @ApiOperation("获取所有账单信息")
//    @GetMapping("/findAccountByGroupId")
//    public JsonResult findAccountByGroupId(@RequestParam("groupId") String groupId,@RequestParam("addTime")  Date addTime,
//                                           @RequestParam("findAll")  boolean findAll,@RequestParam("isOperation")  boolean isOperation,
//                                           @RequestParam("username")  String username) {
//        QueryType queryType = new QueryType();
//        queryType.setGroupId(groupId).setAddTime(addTime).setFindAll(findAll).setOperation(isOperation).setUsername(username);
//        return new JsonResult(accountService.findAccountByGroupId(queryType));
//    }

    //通知功能有bug      根据群组id查
    @ApiOperation("获取所有账单信息")
    @PostMapping("/findAccountByGroupIdPost")
    public JsonResult findAccountByGroupIdPost(@RequestBody QueryType queryType) {
        return new JsonResult(accountService.findAccountByGroupId(queryType));
    }
    @Resource
    RestTemplateConfig restTemplateConfig;

    @ApiOperation("获取所有账单信息")
    @GetMapping("/get1")
    public Object get1() {
        String fullUrl = "https://www.htx.com/-/x/otc/v1/data/trade-market" + "?coinId=2&currency=172&tradeType=sell&currPage=1&payMethod=0&acceptOrder=0&country=" +
                "&blockType=general&online=1&range=0&amount=&isThumbsUp=false&isMerchant=false" +
                "&isTraded=false&onlyTradable=false&isFollowed=false&makerCompleteRate=0";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36");
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
//        headers.set("Authorization", "Bearer YOUR_API_KEY"); // 如果需要 API 密钥

        ResponseEntity<Object> forEntity = restTemplateConfig.restTemplate().exchange(
                fullUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Object.class
        );

        Object body = forEntity.getBody();
        System.err.println(body);
        return body;
    }


}
