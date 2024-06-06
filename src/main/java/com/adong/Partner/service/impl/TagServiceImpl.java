package com.adong.Partner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.adong.Partner.model.domain.Tag;
import com.adong.Partner.service.TagService;
import com.adong.Partner.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author 沈仁东
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-04-25 19:57:44
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




