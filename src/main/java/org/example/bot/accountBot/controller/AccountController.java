package org.example.bot.accountBot.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.example.bot.accountBot.dto.QueryType;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags="账单查询请求")
@RestController
@RequestMapping("api")
public class AccountController {
    @Autowired
    AccountService accountService;

    //通知功能有bug      根据群组id查
    @ApiOperation("获取所有账单信息")
    @GetMapping("/findAccountByGroupId")
    public JsonResult findAccountByGroupId(@RequestBody QueryType queryType) {
        return new JsonResult(accountService.findAccountByGroupId(queryType));
    }


}
