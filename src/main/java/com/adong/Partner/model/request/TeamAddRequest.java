package com.adong.Partner.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TeamAddRequest implements Serializable {
    /**
     * 队伍名称
     */
    private String teamName;

    /**
     *  描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队伍创建者id
     */
    private Long userId;

    /**
     * 队伍logo
     */
    private String avatarUrl;
}
