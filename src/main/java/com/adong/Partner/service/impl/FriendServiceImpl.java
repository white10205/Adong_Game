package com.adong.Partner.service.impl;

import com.adong.Partner.common.ErrorCode;
import com.adong.Partner.contant.RedisConstant;
import com.adong.Partner.exception.BusinessException;
import com.adong.Partner.mapper.UserMapper;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.vo.UserVO;
import com.adong.Partner.service.UserService;
import com.adong.Partner.utils.RedisUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adong.Partner.model.domain.Friend;
import com.adong.Partner.service.FriendService;
import com.adong.Partner.mapper.FriendMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author 沈仁东
* @description 针对表【friend(好友表)】的数据库操作Service实现
* @createDate 2024-05-14 09:38:44
*/
@Service
@Slf4j
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend>
    implements FriendService{

    @Resource
    UserMapper userMapper;

    @Resource
    UserService userService;

    @Resource
    FriendMapper friendMapper;
    @Resource
    RedissonClient redissonClient;

    /**
     * 添加好友
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "自己不能添加自己为好友'");
        }
        // 设置锁名称，锁范围是同一个人加同一个人为好友
        String addUserLock = RedisConstant.USER_ADD_KEY + userId + friendId;
        RLock lock = redissonClient.getLock(addUserLock);
        boolean result1 = false;
        boolean result2 = false;
        try{
            // 尝试获取锁
            if (lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)) {
                log.info(Thread.currentThread().getId() + "我拿到锁了");
                // 查询是否添加了该用户
                QueryWrapper<Friend> queryWrapper = new QueryWrapper();
                queryWrapper.eq("userId", userId);
                queryWrapper.eq("friendId", friendId);
                Long count1 = friendMapper.selectCount(queryWrapper);
                if (count1 > 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "已添加该用户");
                }
                // 查询是否添加了该用户
                queryWrapper = new QueryWrapper();
                queryWrapper.eq("userId", friendId);
                queryWrapper.eq("friendId", userId);
                Long count2 = friendMapper.selectCount(queryWrapper);
                if (count2 > 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "已添加该用户");
                }
                // 插入id: userId, friendId: friendId
                Friend friendByUserId = new Friend();
                friendByUserId.setUserId(userId);
                friendByUserId.setFriendId(friendId);
                // 插入id:friendId , friendId: userId（注意添加事务，即要么都添加要么都不添加）
                result1 = this.save(friendByUserId);
                Friend friendByFriendId = new Friend();
                friendByFriendId.setUserId(friendId);
                friendByFriendId.setFriendId(userId);
                // 写入数据库
                result2 = this.save(friendByFriendId);
            }
        } catch (InterruptedException e) {
            log.error("addUser error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info(Thread.currentThread().getId() + "锁已经被释放");
                lock.unlock();
            }
        }
        return result1 && result2;
    }

    /**
     * 好友列表
     */
    @Override
    public List<UserVO> listFriends(Long userId, HttpServletRequest request) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        List<Friend> friendList = friendMapper.selectList(queryWrapper);
        List<User> userList = friendList.stream().map(friend -> {
            User user = userMapper.selectById(friend.getFriendId());
            return user;
        }).collect(Collectors.toList());
        List<UserVO> userVOList = userList.stream().map(user -> {
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
        return userVOList;
    }

    @Override
    public boolean deleteFriend(long userId, Long friendId) {


        String addUserLock = RedisConstant.USER_ADD_KEY + userId + friendId;
        RLock lock = redissonClient.getLock(addUserLock);
        boolean result1 = false;
        boolean result2 = false;
        try{
            // 尝试获取锁
            if (lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)) {
                log.info(Thread.currentThread().getId() + "我拿到锁了");
                // 查询是否添加了该用户
                QueryWrapper<Friend> queryWrapper = new QueryWrapper();
                queryWrapper.eq("userId", userId);
                queryWrapper.eq("friendId", friendId);
                Long count1 = friendMapper.selectCount(queryWrapper);
                if (count1 < 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "并未添加该用户为好友");
                }
                // 查询是否添加了该用户
                queryWrapper = new QueryWrapper();
                queryWrapper.eq("userId", friendId);
                queryWrapper.eq("friendId", userId);
                Long count2 = friendMapper.selectCount(queryWrapper);
                if (count2 < 0) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户并未添加你为好友");
                }

                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userId",userId);
                queryWrapper.eq("friendId",friendId);
                result1 = this.remove(queryWrapper);

                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userId",friendId);
                queryWrapper.eq("friendId",userId);
                result2 = this.remove(queryWrapper);

            }
        } catch (InterruptedException e) {
            log.error("DeleteUser error", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                log.info(Thread.currentThread().getId() + "锁已经被释放");
                lock.unlock();
            }
        }
        return result1 && result2;
    }


}




