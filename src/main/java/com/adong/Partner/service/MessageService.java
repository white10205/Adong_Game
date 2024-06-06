package com.adong.Partner.service;

import com.adong.Partner.model.domain.Message;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 沈仁东
* @description 针对表【message】的数据库操作Service
* @createDate 2024-05-14 09:40:17
*/
public interface MessageService extends IService<Message> {
    /**
     * 获取消息编号
     *
     * @param userId 用户id
     * @return long
     */
    long getMessageNum(Long userId);


    /**
     * 有新消息
     *
     * @param userId 用户id
     * @return {@link Boolean}
     */
    Boolean hasNewMessage(Long userId);
}
