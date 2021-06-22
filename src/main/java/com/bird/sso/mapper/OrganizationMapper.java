package com.bird.sso.mapper;

import com.bird.sso.domain.SysOrganization;
import com.bird.sso.page.PageBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 10:44
 */
@Component
@Mapper
public interface OrganizationMapper extends SelfReferenceMapper<SysOrganization> {

    List<SysOrganization> selectHasRoles(@Param("appType") String appType);

    List<SysOrganization> selectByAppType(@Param("appType") String appType);

    List<SysOrganization> selectBasicByAppType(@Param("appType") String appType);

    List<SysOrganization> selectByAppType_Level(@Param("appType") String appType
            , @Param("level") Integer level);

    List<SysOrganization> listByRootId(@Param("appType") String appType,@Param("rootId") Long rootId);

    List<SysOrganization> pageUnionByPid(@Param("appType") String appType,@Param("pid") Long pid);

    List<SysOrganization> listUnionByPid(@Param("appType") String appType,@Param("pid") Long pid);
}
