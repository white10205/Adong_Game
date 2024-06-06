package com.adong.Partner.controller;

import com.adong.Partner.common.BaseResponse;
import com.adong.Partner.common.ErrorCode;
import com.adong.Partner.common.ResultUtils;
import com.adong.Partner.exception.BusinessException;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.request.FriendAddRequest;
import com.adong.Partner.model.vo.UserVO;
import com.adong.Partner.service.FriendService;
import com.adong.Partner.service.UserService;
import com.adong.Partner.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/friend")
public class FriendController {
    @Resource
    UserService userService;

    @Resource
    FriendService friendService;

    /**
     * 添加好友
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addFriend(@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request){
        Long friendId = friendAddRequest.getFriendId();
        if(friendId == null || friendId <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        long userId = loginUser.getId();
        boolean result = friendService.addFriend(userId,friendId);
        return ResultUtils.success(result);
    }

    /**
     * 删除好友
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFriend(@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request){
        Long friendId = friendAddRequest.getFriendId();
        if(friendId == null || friendId <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        long userId = loginUser.getId();
        boolean result = friendService.deleteFriend(userId,friendId);
        return ResultUtils.success(result);
    }
    /**
     * 查看好友列表
     */
    @GetMapping("/list")
    public BaseResponse<List<UserVO>> listFriends(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = loginUser.getId();
        List<UserVO> friendList = friendService.listFriends(userId, request);
        return ResultUtils.success(friendList);
    }

    /**
     * 搜索好友
     */
    @GetMapping("/searchByAccount")
    public BaseResponse<List<UserVO>> searchByAccount(@RequestParam(required = false) String searchText, HttpServletRequest request) {
        log.info("根据账号查找用户信息,{}", searchText);
        if (searchText.equals("")) {
            return listFriends(request);
        }
        User loginUser = userService.getLoginUser(request);
        List<UserVO> firendList = friendService.listFriends(loginUser.getId(), request);
        List<Long> friendIdList = firendList.stream().map(UserVO::getId).collect(Collectors.toList());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", friendIdList);
        queryWrapper.like("username", searchText);
        List<User> list = userService.list(queryWrapper);
        List<UserVO> userVOList = list.stream().map(user -> {
            UserVO userVO = new UserVO();
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            userVO.setUserAccount(user.getUserAccount());
            userVO.setAvatarUrl(user.getAvatarUrl());
            userVO.setGender(user.getGender());
            userVO.setProfile(user.getProfile());
            userVO.setPhone(user.getPhone());
            userVO.setEmail(user.getEmail());
            userVO.setUserStatus(user.getUserStatus());
            userVO.setCreateTime(user.getCreateTime());
            userVO.setUpdateTime(user.getUpdateTime());
            userVO.setUserRole(user.getUserRole());
            userVO.setPlanetCode(user.getPlanetCode());
            userVO.setTags(user.getTags());
            return userVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(userVOList);
    }
}
