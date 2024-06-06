package com.adong.Partner.service.impl;


import com.adong.Partner.model.domain.User;
import com.adong.Partner.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceImplTest {
    @Resource
    private  UserService userService;

}