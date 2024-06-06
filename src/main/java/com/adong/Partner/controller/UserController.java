package com.adong.Partner.controller;

import com.adong.Partner.common.BaseResponse;
import com.adong.Partner.common.ErrorCode;
import com.adong.Partner.common.ResultUtils;
import com.adong.Partner.contant.RedisConstant;
import com.adong.Partner.contant.UserConstant;
import com.adong.Partner.exception.BusinessException;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.request.UserLoginRequest;
import com.adong.Partner.model.request.UserRegisterRequest;
import com.adong.Partner.model.vo.UserVO;
import com.adong.Partner.service.FriendService;
import com.adong.Partner.service.UserService;
import com.adong.Partner.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.adong.Partner.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private FriendService friendService;

    @Resource
    RedisUtil redisUtil;
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        log.info("用户注册，{}",userRegisterRequest);
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.userRegister(userRegisterRequest);
        return ResultUtils.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        log.info("用户登录，{}",userLoginRequest);
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     *用户退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null) {
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.userLogout(request);
        return ResultUtils.success("成功退出登录！");
    }
    /**
     * 根据标签查询用户信息
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<UserVO>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList,
                                                        HttpServletRequest request) {
        log.info("根据标签查用户信息，{}",tagNameList);

        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<UserVO> userList = userService.searchUsersByTagsBySQL(tagNameList,request);
        return ResultUtils.success(userList);
    }

    /**
     * 获取当前用户登录信息
     */
    @GetMapping("/current")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request){
        log.info("获取当前登录用户信息！");
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId=currentUser.getId();
        User user = userService.getById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户更新信息
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        log.info("更新用户信息！,{}",user);

        //校验参数是否为空
        if (user == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验权限
        User loginUser = userService.getLoginUser(request);

        //更新信息
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 首页推荐
     */
    @GetMapping("/recommend")
    public BaseResponse<List<UserVO>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request){

        log.info("首页推荐查询,{},{}",pageSize,pageNum);
        User loginUser = userService.getLoginUser(request);
        /*String redisKey = RedisConstant.USER_RECOMMEND_KEY + ":" + loginUser.getId();

        //如果有缓存，直接读缓存
        List<UserVO> userVOListRedis = new Gson().fromJson((String) redisUtil.get(redisKey), new TypeToken<List<UserVO>>() {
        }.getType());
        if(!CollectionUtils.isEmpty(userVOListRedis)){
            return ResultUtils.success(userVOListRedis);
        }*/

        //若没有缓存,查询数据库，并将数据写入缓存
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        //已经是好友的用户不进入推荐
        List<UserVO> firendList = friendService.listFriends(loginUser.getId(), request);
        List<Long> list = firendList.stream().map(userVO -> {
            return userVO.getId();
        }).collect(Collectors.toList());

        //首页不要将自己推荐进去
        list.add(loginUser.getId());

        queryWrapper.notIn("id",list);

        IPage<User> page = new Page<>(pageNum, pageSize);
        IPage<User> userIPage = userService.page(page, queryWrapper);

        // 将User转换为UserVO
        List<UserVO> userVOList = userIPage.getRecords().stream()
                .map(user -> {
                    // 创建UserVO对象并设置属性
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
                })
                .collect(Collectors.toList());
        // 写缓存
        /*try{
            redisUtil.setnx(redisKey, new Gson().toJson(userVOList), 10000);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }*/
        return ResultUtils.success(userVOList);
    }

    /**
     * 根据账号查找用户
     */
    @GetMapping("/searchByAccount")
    public BaseResponse<List<UserVO>> searchByAccount(@RequestParam(required = false) String searchText, HttpServletRequest request){
        log.info("根据账号查找用户信息,{}",searchText);
        if(searchText.equals("")){
            return recommendUsers(8,1,request);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username",searchText);
        List<User> res = userService.list(queryWrapper);
        List<UserVO> userVOList= res.stream().map(user -> {
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

    /**
     * 根据id查询用户信息
     */
    @GetMapping("/{id}")
    public BaseResponse<UserVO> getUserInfoById(@PathVariable  Long id , HttpServletRequest request){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",id);
        User user = userService.getOne(queryWrapper);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 更改用户标签
     */
    @PostMapping("update/tags")
    public BaseResponse<Integer> updateUserTags(@RequestBody(required = false) List<String> tagNameList,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        int res = userService.updateUserTags(tagNameList,loginUser.getId());

        return ResultUtils.success(res);
    }
}
