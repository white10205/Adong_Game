package com.adong.Partner.controller;

import com.adong.Partner.common.BaseResponse;
import com.adong.Partner.common.ErrorCode;
import com.adong.Partner.common.ResultUtils;
import com.adong.Partner.contant.ChatConstant;
import com.adong.Partner.exception.BusinessException;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.request.ChatRequest;
import com.adong.Partner.model.vo.ChatMessageVO;
import com.adong.Partner.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    private UserService userService;

    @Resource
    private com.hjj.homieMatching.service.ChatService chatService;

    /**
     * 获取私人聊天消息
     */
    @PostMapping("/privateChat")
    public BaseResponse<List<ChatMessageVO>> getPrivateChat(@RequestBody ChatRequest chatRequest,
                                                            HttpServletRequest request) {
        if (chatRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<ChatMessageVO> privateChat = chatService.getPrivateChat(chatRequest, ChatConstant.PRIVATE_CHAT, loginUser);
        return ResultUtils.success(privateChat);
    }
}
