package org.example.bot.accountBot.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.example.bot.accountBot.dto.*;

import org.example.bot.accountBot.service.AccountService;
import org.example.bot.accountBot.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Api(tags="账单查询请求")
@RestController
@RequestMapping("api")
public class AccountController {
    @Autowired
    AccountService accountService;
    //通知功能有bug      根据群组id查
    @ApiOperation("获取所有账单信息")
    @PostMapping("/findAccountByGroupIdPost")
    public JsonResult findAccountByGroupIdPost(@RequestBody QueryType queryType) {
        return new JsonResult(accountService.findAccountByGroupId(queryType));
    }

    @ApiOperation("登录")
    @PostMapping("/accountLogin")
    public JsonResult accountLogin(@RequestBody LoginFromDTO loginFromDTO) {
        return accountService.login(loginFromDTO);
    }
    @ApiOperation("注册")
    @PostMapping("/accountRegister")
    public JsonResult accountRegister(@RequestBody LoginFromDTO loginFromDTO) {
        return accountService.accountRegister(loginFromDTO);
    }
    @ApiOperation("获取所有群组")
    @PostMapping("/findGroupList")
    public JsonResult findGroupList(@RequestBody QueryGroupDTO queryDTO) {
        return accountService.findGroupList(queryDTO);
    }

    @ApiOperation("查询标签列的群")
    @GetMapping("/updateExpireTime")
    public JsonResult updateExpireTime(@RequestParam("userId") String userId,@RequestParam("expireTime") String expireTime) {
        return accountService.updateExpireTime(userId,expireTime);
    }
    @ApiOperation("查询标签列的群")
    @PostMapping("/findGroupListTag")
    public JsonResult findGroupListTag(@RequestBody QueryGroupTagDTO dto) {
        return accountService.findGroupListTag(dto);
    }
    @ApiOperation("获取所有用户")
    @PostMapping("/getUserList")
    public JsonResult getUserList(@RequestBody QueryUserDTO queryDTO) {
        return accountService.getUserList(queryDTO);
    }
    @ApiOperation("保存收款图片和收款地址等..配置")
    @PostMapping("/saveAccountConfigEdit")
    public JsonResult saveCustomerConfig(@RequestBody ConfigDTO dto) {
        return accountService.saveCustomerConfig(dto);
    }
    @ApiOperation("更新机器人在群组的状态")
    @GetMapping("/setStatus")
    public JsonResult setStatus() {
        return accountService.setStatus();
    }

    @ApiOperation("批量退群")
    @PostMapping("/saveAccountSetting")
    public JsonResult saveAccountSetting(@RequestBody AccountSettingDTO dto) {
        return accountService.saveAccountSetting(dto);
    }

    @ApiOperation("修改登录密码")
    @GetMapping("/accountChangePassword")
    public JsonResult accountChangePassword(@RequestParam("username") String username,
                                            @RequestParam("newPassword") String newPassword, @RequestParam("secretKey") String secretKey) {
        return accountService.accountChangePassword(username,newPassword,secretKey);
    }


    @ApiOperation("设置超级管理的查询")
    @GetMapping("/findAccountUser")
    public JsonResult findAccountUser(@RequestParam("page") Integer page, @RequestParam("size") Integer size,
                                      @RequestParam("keyword") String keyword) {
        return accountService.findAccountUser(page,size,keyword);
    }
    @ApiOperation("设置超级管理")
    @GetMapping("/setAccountSuperAdminUser")
    public JsonResult setAccountSuperAdminUser(@RequestParam String userId) {
        return accountService.setAccountSuperAdminUser(userId);
    }
    @ApiOperation("取消设置超级管理")
    @GetMapping("/cancelAccountSuperAdminUser")
    public JsonResult quxiaoAccountSuperAdminUser(@RequestParam String userId) {
        return accountService.quxiaoAccountSuperAdminUser(userId);
    }


    @ApiOperation("机器人退群")
    @GetMapping("/getAccountSetting")
    public JsonResult getAccountSetting() {
        return accountService.getAccountSetting();
    }
    @ApiOperation("机器人退群")
    @GetMapping("/leaveGroup")
    public JsonResult leaveGroup(@RequestParam String groupId) {
        return accountService.leaveGroup(groupId);
    }
    @ApiOperation("批量退群")
    @PostMapping("/allLeaveGroup")
    public JsonResult allLeaveGroup(@RequestBody MangerAllGroupQueryDTO dto) {
        return accountService.allLeaveGroup(dto.getGroupIds());
    }
    @ApiOperation("一键广播 向勾选群发送消息")
    @PostMapping("/sendAllMessage")
    public JsonResult sendAllMessage(@RequestBody ManagerGroupMessageDTO dto) {
        return accountService.sendAllMessage(dto);
    }

    @ApiOperation("广播 如果UserId为空则全部")
    @PostMapping("/sendUserMessage")
    public JsonResult sendUserMessage(@RequestBody UserMessageDTO dto) {
        return accountService.sendUserMessage(dto);
    }
    @ApiOperation("如果groupId为空则全部")
    @PostMapping("/sendGroupMessage")
    public JsonResult sendGroupMessage(@RequestBody GroupMessageDTO dto) {
        return accountService.sendGroupMessage(dto);
    }
    @ApiOperation("如果groupId为空则全部")
    @PostMapping("/sendGroupMessageTag")
    public JsonResult sendGroupMessageTag(@RequestBody GroupMessageDTO dto) {
        return accountService.sendGroupMessageTag(dto);
    }

    @ApiOperation("设置群标签")
    @GetMapping("/setTagGroup")
    public JsonResult setTagGroup(@RequestParam("groupId") String groupId, @RequestParam("tag") String tag) {
        return accountService.setTagGroup(groupId, tag);
    }
    @ApiOperation("根据群标签")
    @GetMapping("/getTagAll")
    public JsonResult getTagAll(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer size, @RequestParam("groupId") String groupId) {
        return accountService.getTagAll(page, size, groupId);
    }
    @ApiOperation("查询用户订单记录")
    @GetMapping("/findAccountUserOrder")
    public JsonResult findAccountUserOrder(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer size,
                                           @RequestParam(value = "keyword",required = false) String keyword,
                                           @RequestParam(value = "startTime",required = false) String startTime,
                                           @RequestParam(value = "endTime",required = false) String endTime,
                                           @RequestParam(value = "selectedType",required = false) String selectedType) {
        return accountService.findAccountUserOrder(page, size, keyword,startTime, endTime,selectedType);
    }
    @ApiOperation("群标签")
    @GetMapping("/deleteTagGroup")
    public JsonResult deleteTagGroup(@RequestParam("groupId") String groupId, @RequestParam("tag") String tag) {
        return accountService.deleteTagGroup(groupId, tag);
    }
    @ApiOperation("查询收款图片和收款地址")
    @GetMapping("/findConfig")
    public JsonResult findConfig() {
        return accountService.findConfig();
    }

    @Value("${uploadFileGangbo}")
    private String uploadFileGangbo;
    @Value("${upload}")
    private String upload;
    @PostMapping("/uploadFileGangbo")//这个是进群欢迎消息配置上传文件
    public JsonResult uploadFileGangbo(@RequestParam("file") MultipartFile file) {
        try {
            JsonResult jsonResult = new JsonResult();
            // 创建上传目录
            Path uploadPath = Paths.get(uploadFileGangbo);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一的文件名
            String originalFilename = file.getOriginalFilename();
            String baseName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            Path filePath = uploadPath.resolve(originalFilename);
            String contentType = file.getContentType();
            int counter = 1;
            while (Files.exists(filePath)) {
                String newFilename = baseName + "_" + counter + extension;
                filePath = uploadPath.resolve(newFilename);
                counter++;
            }
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            // 构建文件的 URL
            String fileUrl = upload + "UpgradeGangbo/" + filePath.getFileName().toString();
            String fileName = filePath.getFileName().toString();
            // 封装文件信息到 Map
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("url", fileUrl);
            fileInfo.put("name", fileName);
            // 设置返回的数据
            jsonResult.setData(fileInfo);
            jsonResult.setMessage("文件上传成功");
            jsonResult.setCode(200);
            return jsonResult;
        } catch (IOException e) {
            JsonResult jsonResult = new JsonResult();
            jsonResult.setCode(1); // 假设 1 表示失败
            jsonResult.setMessage("文件上传失败: " + e.getMessage());
            return jsonResult;
        }
    }
}
