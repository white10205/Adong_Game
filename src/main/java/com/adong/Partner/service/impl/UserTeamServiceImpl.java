package com.adong.Partner.service.impl;

import com.adong.Partner.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.adong.Partner.service.UserTeamService;
import com.adong.Partner.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 沈仁东
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-05-13 22:22:36
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




