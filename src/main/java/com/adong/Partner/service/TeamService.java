package com.adong.Partner.service;

import com.adong.Partner.model.domain.Team;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.dto.TeamQuery;
import com.adong.Partner.model.request.TeamJoinRequest;
import com.adong.Partner.model.request.TeamQuitTeam;
import com.adong.Partner.model.vo.TeamUserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 沈仁东
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-04-25 20:06:21
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍和及队伍用户
     */
    List<TeamUserVO> listTeam(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 根据id查询队伍信息
     */
    TeamUserVO getTeamInfoById(Long teamId);

    /**
     * 添加队伍
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     */
    boolean exitTeam(TeamQuitTeam teamQuitTeam, User loginUser);

    /**
     * 获取我创建的队伍
     */
    List<TeamUserVO> listMyCreateTeam(User loginUser);

    /**
     * 获取我加入的队伍
     */
    List<TeamUserVO> listMyJoinTeam(User loginUser);
}
