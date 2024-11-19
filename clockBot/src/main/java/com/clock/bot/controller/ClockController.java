package com.clock.bot.controller;

import com.clock.bot.dto.QueryType;
import com.clock.bot.service.UserStatusService;
import com.clock.bot.utils.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags="账单查询请求")
@RestController
@RequestMapping("/clockApi")
public class ClockController {
    @Autowired
    UserStatusService userStatusService;



    @ApiOperation("获取所有账单信息")
    @PostMapping("/findClockList")
    public JsonResult findClockList(@RequestBody QueryType queryType) {
        return new JsonResult(userStatusService.findClockList(queryType));
    }
}
