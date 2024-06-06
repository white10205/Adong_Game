package com.adong.Partner.service;

import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.request.UserRegisterRequest;
import com.adong.Partner.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 沈仁东

*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    void userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户退出登录
     */
    void userLogout(HttpServletRequest request);

    /**
     * 数据脱敏
     */
     User getSafetyUser(User originUser);

    /**
     * 根据用户标签查询用户(SQL版)
     */
     List<UserVO> searchUsersByTagsBySQL(List<String> tagNameList,HttpServletRequest request);

    /**
     * 获取当前用户的信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户更新信息
     */
    Integer updateUser(User user,User loginUser);

    /**
     *是否为管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 更新用户标签
     */
    int updateUserTags(List<String> tagNameList, Long id);
}
