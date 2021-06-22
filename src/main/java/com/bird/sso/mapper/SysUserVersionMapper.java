package com.bird.sso.mapper;

import com.bird.sso.domain.SysUserVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper
public interface SysUserVersionMapper {

    void insert(SysUserVersion record);

    List<SysUserVersion> selectByUserId(@Param("appType") String appType,@Param("userId") long userId);
}