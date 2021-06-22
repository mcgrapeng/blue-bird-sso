package com.bird.sso.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SelfReferenceMapper<T> {

    void insert(T data);

    void update(T data);

    void updateBy(Map<String, Object> params);

    void delete(@Param("appType") String appType,Integer key);

    int deleteReference(@Param("appType") String appType , @Param("sid")  long sid);

    T getBy(Map<String, Object> params);

    List<T> selectBy(Map<String, Object> params);

    int count(Map<String, Object> params);

    List<T> selectByIds(@Param("appType") String appType,@Param("ids") List<Integer> ids);

}
