package com.bird.sso.mapper;

import com.bird.sso.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 10:42
 */
@Component
@Mapper
public interface UserMapper {
    void insert(SysUser user);

    void update(SysUser user);

    void updateByUserName(SysUser user);

    void updateMapByUserId(Map<String, Object> params);

    void updateMapByUserName(Map<String, Object> params);

    void updateUserName(Map<String, Object> params);

    @Deprecated
    SysUser getBy(Map<String, Object> params);

    List<SysUser> selectBy(Map<String, Object> params);

    SysUser getByUserName(Map<String, Object> params);

    SysUser getByOpenId(Map<String, Object> params);

    SysUser getByUserId(Map<String, Object> params);

    SysUser getById(int id);

    List<SysUser> selectHasRoles(@Param("appType") String appType);

    int count(Map<String, Object> params);

    List<SysUser> listManager(@Param("appType") String appType, @Param("orgIds") List<Long> orgIds);
}
