package com.bird.sso.mapper;

import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 10:43
 */
@Component
@Mapper
public interface RoleMapper extends SelfReferenceMapper<SysRole> {

    List<SysRole> selectColumnBySids(@Param("appType") String appType, @Param("sids") List<Long> sids);

    List<String> selectMenusBySids(@Param("appType") String appType, @Param("sids") List<Long> sids);

    List<SysRole> selectDataScopeBySids(@Param("appType") String appType, @Param("sids") List<Long> sids);

    List<SysRole> selectColumnByCodes(@Param("appType") String appType, @Param("codes") List<String> code);

    List<SysRole> selectByAppType_ClientType_Codes(@Param("appType") String appType,  @Param("clientType")  String clientType,  @Param("codes") List<String> code);

    List<SysRole> selectTreeColumnBySids(@Param("appType") String appType, @Param("sids") List<Long> sids);

    List<SysRole> selectALLRolesByAppType(@Param("appType") String appType);

    List<SysRole> selectBySids(@Param("appType") String appType
            , @Param("clientType") String clientType, @Param("sids") List<Long> sids);

    List<SysRole> selectHasMenus(@Param("appType") String appType);

}
