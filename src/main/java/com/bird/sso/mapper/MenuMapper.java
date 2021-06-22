package com.bird.sso.mapper;

import com.bird.sso.domain.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Mapper
public interface MenuMapper extends SelfReferenceMapper<SysMenu>{

    void updateRele(Map<String,Object> params);

    List<SysMenu> selectColumnBySids(@Param("appType") String appType,@Param("sids") List<Long> sids);

    List<SysMenu> selectAvailableBySids(@Param("appType") String appType,@Param("clientType") String clientType,@Param("sids") List<Long> sids);

    List<SysMenu> selectUnpAvailableBySids(@Param("appType") String appType,@Param("clientType") String clientType,@Param("sids") Set<Long> sids);

    List<String> selectPathUnpAvailableBySids(@Param("appType") String appType,@Param("clientType") String clientType,@Param("sids") Set<Long> sids);
}
