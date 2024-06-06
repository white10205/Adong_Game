package com.adong.Partner.service.impl;

import com.adong.Partner.common.ErrorCode;
import com.adong.Partner.contant.UserConstant;
import com.adong.Partner.exception.BusinessException;
import com.adong.Partner.mapper.FriendMapper;
import com.adong.Partner.model.request.UserRegisterRequest;
import com.adong.Partner.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.service.UserService;
import com.adong.Partner.mapper.UserMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.adong.Partner.contant.UserConstant.ADMIN_ROLE;
import static com.adong.Partner.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author 沈仁东
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private  UserMapper userMapper;

    /**
     * 盐值，用以混淆密码
     */
    private static final String SALT="ADong";
    @Override
    @Transactional( rollbackFor = Exception.class)
    public void userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String username = userRegisterRequest.getUsername();
        Integer gender = userRegisterRequest.getGender();
        String phone = userRegisterRequest.getPhone();
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            // todo 修改为自定义异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短，至少要4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短，至少要8位");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setGender(gender);
        user.setPhone(phone);
        user.setUsername(username);
        //默认头像
//        user.setAvatarUrl("");
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加失败");
        }
        // 设置星球编号
        long userId = user.getId();
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPlanetCode(String.valueOf(userId));
        boolean updateResult = this.updateById(updateUser);
        if (!updateResult) {
            log.info("{}用户星球编号设置失败", userId);
        }
    }

    /**
     * 用户登录
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码不能为空");
        }
        if(userAccount.length() < 4){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户名至少由4位组成");
        }
        if(userPassword.length() < 8){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"密码至少为8位");
        }
        // 账户不能包含特殊字符
        String validPattern="[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher= Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户名包含特殊字符");
        }
        // 2.加密
        String encryptPassword= DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if(user==null){
            log.info("用户名或密码错误");
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户名或密码错误");
        }
        // 3.用户脱敏
        User safetyUser=getSafetyUser(user);
        // 4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);

        return safetyUser;
    }

    /**
     * 用户退出登录
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }


    /**
     * 用户脱敏
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }


    /**
     * 根据标签查用户信息（SQL版）
     */
    @Override
    public List<UserVO> searchUsersByTagsBySQL(List<String> tagNameList,HttpServletRequest request) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(user -> {
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
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request){
        if(request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    /**
     * 用户更新信息

     */
    @Override
    public Integer updateUser(User user, User loginUser) {
        long userId = user.getId();

        //如果是管理员则允许更新任意用户信息
        if(userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户信息
        // 如果不是管理员，只允许更新当前用户信息
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        return userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user=(User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public int updateUserTags(List<String> tagNameList, Long id) {
        String str = "[";
        for(int i = 0 ; i < tagNameList.size() ; i ++){
            if(i != tagNameList.size() - 1){
                str += tagNameList.get(i) + ",";
            }else {
                str += tagNameList.get(i) ;
            }
        }
        str += "]";
        User user = new User();
        user.setTags(str);
        user.setId(id);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id",id);
        return userMapper.updateById(user);
    }

}




