package com.adong.Partner.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 */
@Data
public class TeamJoinRequest implements Serializable {
    /**
     * 队伍的id
     */
    private Long teamId;
}
