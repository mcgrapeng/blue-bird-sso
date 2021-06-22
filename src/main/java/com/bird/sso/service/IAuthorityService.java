package com.bird.sso.service;

import com.bird.sso.domain.SysMenu;
import com.bird.sso.web.UserAssign;
import com.bird.sso.web.controller.manage.user.UserAssignBatchForm;
import com.bird.sso.web.controller.manage.user.UserAssignForm;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 10:51
 */
public interface IAuthorityService {


    /**
     * 授予默认角色
     *
     * @param appType
     * @param id
     */
   // void assignDefaultAuthorities(String appType, long id, String authorizeClientType);

    /**
     * 授予默认角色(双端)
     *
     * @param appType
     * @param id
     */
    void assignDefaultAuthorities(String appType, long id);


    /**
     * 授予指定角色（追加）（双端）
     *
     * @param role
     * @param appType
     * @param id
     * @param isIgnoreException 是否过滤异常
     */
    void assignAuthorities(UserAssign role, String appType, long id, boolean isIgnoreException);

   // void assignAuthorities(UserAssign role, String appType, String authorizeClientType, long id, boolean isIgnoreException);


    /**
     * 批量授予角色（追加）（双端）
     *
     * @param roles
     * @param appType
     * @param id                用户ID、组织ID
     * @param isIgnoreException 是否过滤异常
     */
    void assignAuthorities(List<UserAssign> roles, String appType, long id, boolean isIgnoreException);


    //void assignAuthorities(List<UserAssign> roles, String appType, String authorizeClientType, long id, boolean isIgnoreException);


    /**
     * 批量授权（覆盖）（双端）
     *
     * @param roles
     * @param appType
     * @param id
     */
    void assignCoverAuthorities(List<UserAssign> roles, String appType, long id);


    void assignCoverAuthorities(UserAssignBatchForm assign, String appType);


    //void assignCoverAuthorities(List<UserAssign> roles, String appType, String authorizeClientType, long id);


    /**
     * 批量授权（追加）（双端）
     *
     * @param roles
     * @param appType
     * @param id
     */
    void assignAuthorities(List<UserAssign> roles, String appType, long id);


    //void assignAuthorities(List<UserAssign> roles, String appType, String authorizeClientType, long id);


    /**
     * 批量授权（覆盖）（双端）
     *
     * @param appType
     * @param userId
     * @param roleCode
     */
    void assignCoverAuthorities(String appType, long userId, List<String> roleCode);


   // void assignCoverAuthorities(String appType, String authorizeClientType, long userId, List<String> roleCode);


    /**
     * 授予所有权限（双端）
     */
    void assignAuthorities();

    void assignAuthorities(String appType, long userId);


    /**
     * 当前用户拥有的菜单
     *
     * @return
     */
    List<SysMenu> hasAuthorities();

    /**
     * 当前用户所属某个角色拥有的菜单
     *
     * @param roleId
     * @return
     */
    List<SysMenu> hasAuthorities(long roleId);


    /**
     * 当前客户端所拥有的菜单
     *
     * @param authorizeClientType
     * @return
     */
    List<SysMenu> hasAuthorities(String appType, String authorizeClientType);

}
