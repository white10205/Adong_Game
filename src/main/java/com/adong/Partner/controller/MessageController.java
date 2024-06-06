package com.adong.Partner.controller;


import com.adong.Partner.common.BaseResponse;
import com.adong.Partner.common.ResultUtils;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.service.MessageService;
import com.adong.Partner.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * 消息控制器
 *
 * @author Adong
 * @date 2024/6/5
 */
@RestController
@RequestMapping("/message")
@Api(tags = "消息管理模块")
public class MessageController {

    /**
     * 消息服务
     */
    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * 用户是否有新消息
     */
    @GetMapping

    public BaseResponse<Boolean> userHasNewMessage(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return ResultUtils.success(false);
        }
        Boolean hasNewMessage = messageService.hasNewMessage(loginUser.getId());
        return ResultUtils.success(hasNewMessage);
    }

    /**
     * 获取用户新消息数量
     */
    @GetMapping("/num")

    public BaseResponse<Long> getUserMessageNum(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return ResultUtils.success(0L);
        }
        long messageNum = messageService.getMessageNum(loginUser.getId());
        return ResultUtils.success(messageNum);
    }

}
