package com.adong.Partner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adong.Partner.model.domain.Message;
import com.adong.Partner.service.MessageService;
import com.adong.Partner.mapper.MessageMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.adong.Partner.contant.RedisConstant.MESSAGE_BLOG_NUM_KEY;
import static com.adong.Partner.contant.RedisConstant.MESSAGE_LIKE_NUM_KEY;

/**
* @author 沈仁东
* @description 针对表【message】的数据库操作Service实现
* @createDate 2024-05-14 09:40:17
*/
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
    implements MessageService{
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public long getMessageNum(Long userId) {
        LambdaQueryWrapper<Message> messageLambdaQueryWrapper = new LambdaQueryWrapper<>();
        messageLambdaQueryWrapper.eq(Message::getTo_id, userId).eq(Message::getIs_read, 0);
        return this.count(messageLambdaQueryWrapper);
    }



    @Override
    public Boolean hasNewMessage(Long userId) {
        String likeNumKey = MESSAGE_LIKE_NUM_KEY + userId;
        Boolean hasLike = stringRedisTemplate.hasKey(likeNumKey);
        if (Boolean.TRUE.equals(hasLike)) {
            String likeNum = stringRedisTemplate.opsForValue().get(likeNumKey);
            assert likeNum != null;
            if (Long.parseLong(likeNum) > 0) {
                return true;
            }
        }
        String blogNumKey = MESSAGE_BLOG_NUM_KEY + userId;
        Boolean hasBlog = stringRedisTemplate.hasKey(blogNumKey);
        if (Boolean.TRUE.equals(hasBlog)) {
            String blogNum = stringRedisTemplate.opsForValue().get(blogNumKey);
            assert blogNum != null;
            return Long.parseLong(blogNum) > 0;
        }
        return false;
    }
}




