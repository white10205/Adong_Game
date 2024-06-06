package com.adong.Partner.contant;

/**
 * 聊天常量
 *
 * @author Adong
 * @date 2024/6/5
 */
public final class ChatConstant {
    private ChatConstant() {
    }

    /**
     * 私聊
     */
    public static final int PRIVATE_CHAT = 1;

    /**
     * 队伍群聊
     */

    public static final int TEAM_CHAT = 2;
    /**
     * 大厅聊天
     */
    public static final int HALL_CHAT = 3;

    /**
     * 缓存聊天大厅
     */
    public static final String CACHE_CHAT_HALL = "Adong:chat:chat_records:chat_hall";

    /**
     * 缓存私人聊天
     */
    public static final String CACHE_CHAT_PRIVATE = "Adong:chat:chat_records:chat_private:";

    /**
     * 缓存聊天团队
     */
    public static final String CACHE_CHAT_TEAM = "Adong:chat:chat_records:chat_team:";
}
