package com.bird.sso.mapper.manage;

import com.bird.sso.domain.SysApplication;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/5/17 14:51
 */
@Mapper
@Component
public interface SysApplicationMapper {


    void insert(SysApplication application);

    void update(SysApplication application);


    List<SysApplication> selectBy(Map<String, Object> params);


    SysApplication getByAppId(Map<String, Object> params);


    int count(Map<String, Object> params);


}
