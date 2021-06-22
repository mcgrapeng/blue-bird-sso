package com.bird.sso.mapper;

import com.bird.sso.domain.AppVersion;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 10:44
 */
@Component
@Mapper
public interface AppVersionMapper {

    void insert(AppVersion appVersion);

    void update(AppVersion appVersion);

    AppVersion getBy(Map<String, Object> params);

    AppVersion getMaxVersion(Map<String, Object> params);

    AppVersion getNextVersion(Map<String, Object> params);
}
