package com.adong.Partner.service;

import com.adong.Partner.model.domain.Friend;
import com.adong.Partner.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 沈仁东
* @description 针对表【friend(好友表)】的数据库操作Service
* @createDate 2024-05-14 09:38:44
*/
public interface FriendService extends IService<Friend> {

    /**
     * 添加好友
     */
    boolean addFriend(long userId, long friendId);

    /**
     * 查看好友列表
     */
    List<UserVO> listFriends(Long userId, HttpServletRequest request);

    /**
     * 删除好友
     */
    boolean deleteFriend(long userId, Long friendId);
}
