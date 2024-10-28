package org.example.bot.accountBot.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.bot.accountBot.pojo.Account;
import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags="账单查询请求")
@RestController
@RequestMapping("api")
public class AccountController {
    @Autowired
    AccountService accountService;

    //通知功能有bug      根据群组id查
    @ApiOperation("获取所有账单信息")
    @PostMapping("/findAccountByGroupId")
    public JsonResult findAccountByGroupId(String groupId){
        List<Account> accounts=accountService.findAccountByGroupId(groupId);
        return new JsonResult(accounts);
    }



}
