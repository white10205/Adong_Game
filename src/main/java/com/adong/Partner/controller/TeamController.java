package com.adong.Partner.controller;

import com.adong.Partner.common.BaseResponse;
import com.adong.Partner.common.ErrorCode;
import com.adong.Partner.common.ResultUtils;
import com.adong.Partner.exception.BusinessException;
import com.adong.Partner.model.domain.Team;
import com.adong.Partner.model.domain.User;
import com.adong.Partner.model.domain.UserTeam;
import com.adong.Partner.model.dto.TeamQuery;
import com.adong.Partner.model.request.TeamAddRequest;
import com.adong.Partner.model.request.TeamJoinRequest;
import com.adong.Partner.model.request.TeamQuitTeam;
import com.adong.Partner.model.vo.TeamUserVO;
import com.adong.Partner.model.vo.UserVO;
import com.adong.Partner.service.TeamService;
import com.adong.Partner.service.UserService;
import com.adong.Partner.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 */
@RestController
@RequestMapping("/team")
@Slf4j
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;
    /**
     * 添加队伍
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        Team team = new Team();
        team.setTeamName(teamAddRequest.getTeamName());
        team.setDescription(teamAddRequest.getDescription());
        team.setExpireTime(teamAddRequest.getExpireTime());
        team.setMaxNum(teamAddRequest.getMaxNum());
        team.setAvatarUrl(teamAddRequest.getAvatarUrl());

        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    /**
     * 获取全部队伍
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeam(teamQuery, isAdmin);
        //未查询到队伍的情况
        if(teamList == null || teamList.size() == 0){
            return ResultUtils.success(new ArrayList<>());
        }
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}

        // 3、查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);

        // 队伍id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        //查询多少人加入了这个队伍
        teamList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });

//        //设置用户列表（用户信息）
//        teamList.forEach(team->{
//            QueryWrapper<UserTeam> userTeamQuery = new QueryWrapper<>();
//            userTeamQuery.eq("teamId", team.getId());
//            List<UserTeam> list = userTeamService.list(userTeamQuery);
//            List<Long> collect = list.stream().map(UserTeam::getUserId).collect(Collectors.toList());
//            QueryWrapper<User> userQuery = new QueryWrapper<>();
//            userQuery.in("id",collect);
//            List<User> userList = userService.list(userQuery);
//            List<UserVO> userVoList = userList.stream().map(user -> {
//                UserVO userVO = new UserVO();
//                BeanUtils.copyProperties(user, userVO);
//                return userVO;
//            }).collect(Collectors.toList());
//
//            team.setUserList(userVoList);
//        });
        return ResultUtils.success(teamList);
    }

    /**
     * 根据id获取队伍信息
     */
    @GetMapping("/getInfo")
    public BaseResponse<TeamUserVO> getTeamInfoById(Long teamId, HttpServletRequest request) {
        if (teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 1、查询队伍列表
        TeamUserVO team = teamService.getTeamInfoById(teamId);

        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamId);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            boolean hasJoin = hasJoinTeamIdSet.contains(teamId);
            team.setHasJoin(hasJoin);
        } catch (Exception e) {}

        // 3、查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.eq("teamId", teamId);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);

        // 队伍id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));


        team.setHasJoinNum(teamIdUserTeamList.getOrDefault(teamId, new ArrayList<>()).size());

        //设置加入该队伍的用户信息
        QueryWrapper<UserTeam> userTeamQuery = new QueryWrapper<>();
        userTeamQuery.eq("teamId", teamId);
        List<UserTeam> list = userTeamService.list(userTeamQuery);
        List<Long> collect = list.stream().map(UserTeam::getUserId).collect(Collectors.toList());
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.in("id",collect);
        List<User> userList = userService.list(userQuery);
        List<UserVO> userVoList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        team.setUserList(userVoList);

        //设置队长
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id",team.getUserId());
        User one = userService.getOne(userQueryWrapper);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(one,userVO);
        team.setCreateUser(userVO);

        return ResultUtils.success(team);
    }

    /**
     * 加入队伍
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 退出队伍
     */
    @PostMapping("/exit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitTeam teamQuitTeam, HttpServletRequest request){
        if (teamQuitTeam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userService.getLoginUser(request) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.exitTeam(teamQuitTeam, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 获取我创建的队伍
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamList = teamService.listMyCreateTeam(loginUser);
        return ResultUtils.success(teamList);
    }

    /**
     **获取我加入的队伍
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamUserVO> teamList = teamService.listMyJoinTeam(loginUser);
        return ResultUtils.success(teamList);
    }
}
