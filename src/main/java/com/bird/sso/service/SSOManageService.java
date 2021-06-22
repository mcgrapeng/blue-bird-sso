package com.bird.sso.service;

import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;

import java.util.List;
import java.util.Map;

/**
 * 张鹏
 * @param <N>  主键类型
 * @param <T>  实体类型
 */
public interface SSOManageService<N, T> {


    @Deprecated
    T findInfo(int id);

    @Deprecated
    T findInfo(String appType, int id);

    List<T> findInfoByPid(String appType, long pid);

    T findInfoBySid(String appType, long sid);

    void addInfo(List<T> data);

    void addInfo(T data);

    void updInfo(List<T> data);

    void updInfo(T data);

    void delInfo(String appType, long pid, long sid);

    void delInfo(String appType, long sid);

    PageBean<T> page(PageParam pageParam, Map<String, Object> params);

    List<T> list(Map<String, Object> params);

}
