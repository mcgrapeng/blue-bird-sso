package com.bird.sso.service.manage;

import com.bird.sso.mapper.manage.SysApplicationMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/5/17 14:54
 */
@Service
public class ApplicationService {

    @Autowired
    private SysApplicationMapper mapper;




}
