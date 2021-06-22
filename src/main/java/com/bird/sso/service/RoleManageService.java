package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.core.TreeHandler;
import com.bird.sso.core.UserService;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.RoleMapper;
import com.bird.sso.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 15:52
 */
@Slf4j
@Service
public class RoleManageService extends AbsManageService<RoleMapper, SysRole> {

    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private UserQueryService userQueryService;


    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    private OrgAuthorityService orgAuthorityService;


    @Autowired
    private UserManageService userManageService;


    @Autowired
    private ParentChildReferenceService parentChildReferenceService;


    @Transactional(rollbackFor = Exception.class)
    public void updateRole(List<SysRole> data) {
        if (CollectionUtils.isEmpty(data)) return;

        super.updInfo(data);

        executor.execute(() -> {
            String appType = data.get(0).getAppType();
            List<SysUser> users
                    = userQueryService.listAuthoritiesUser(appType);

            if (CollectionUtils.isEmpty(users)) return;

            for (SysUser u : users) {
                List<SysRole> oldRoles = u.getAuthority().listRole();
                List<SysRole> filter = oldRoles.stream().filter(t -> data.contains(t))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(filter)
                        && oldRoles.removeAll(filter)) {
                    oldRoles.addAll(data);
                }
                u.setRoles(JSON.toJSONString(oldRoles));
                userManageService.updateUser(u);
            }
        });
    }


    /**
     * 设置默认
     *
     * @param sid
     * @param appType
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(long sid, String appType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("isDefault", PublicEnum.Y.name());
        SysRole sysRole = mapper.getBy(params);

        if (ObjectUtils.isNotEmpty(sysRole)) {
            if (sysRole.getSid() == sid) return;
            sysRole.setIsDefault(PublicEnum.N.name());
            mapper.update(sysRole);
        }

        params.put("sid", sid);
        mapper.updateBy(params);
    }


    /**
     * 改变状态
     *
     * @param sid
     * @param appType
     */
    public void changeStatus(long sid, String appType, String isAvailable) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("sid", sid);
        params.put("isAvailable", isAvailable);

        mapper.updateBy(params);

        if (isAvailable.equals(PublicEnum.N.name())) {
            executor.execute(() -> {
                //清理角色中的菜单
                getAuthoritiesAndSet(appType, Lists.newArrayList(sid));
            });
        }
    }


    /**
     * 角色授权
     *
     * @param roleId
     * @param menus
     * @param appType
     * @param method
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermission(String method, Long roleId, List<SysMenu> menus, String appType) {
        log.info(">>>>>>>>>>assignPermission>>>>>>>>>>>>>权限分配,method={},roleId={}，appType={}，menus={}"
                , method, roleId, appType, JSON.toJSONString(menus));
        if (CollectionUtils.isEmpty(menus)) return;

        List<SysMenu> unique;
        if (CollectionUtils.size(menus) > 1) {
            unique = menus.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> p.getAppType() + "_"
                                    + p.getClientType() + "_" + p.getPid()
                                    + "_" + p.getSid()))), ArrayList::new));
        } else {
            unique = menus;
        }

        //数据合法性校验
        // List<SysMenu> check = check(unique);

        //授权
        SysRole role = findInfoBySid(appType, roleId);

        if (ObjectUtils.isEmpty(role)) {
            throw SSOException.ROLE_IS_NOT_EXIST;
        }

        if (Method.APPEND.name().equals(method)) {
            assignAppendPermission(role, unique);
        } else {
            assignCoverPermission(role, unique);
        }
    }


    @Override
    protected String getName() {
        return "角色信息";
    }


    /**
     * 删除角色后，重新设置用户授予的角色
     */
    @Override
    protected void getAuthoritiesAndSet(String appType, List<Long> sids) {
        userAuthorityService.getAuthoritiesAndSet(appType, sids);
    }


    /**
     * 拥有的角色
     *
     * @return
     */
    public List<SysRole> hasAuthorities() {
        SSOUser loginUser = WebUtils.getSSOUser();
        String username = loginUser.getUserName();
        String appType = loginUser.getAppType();
        SysUser user = userQueryService.findUserByUserName(appType, username);
        return user.getAuthority().listRole();
    }

    /**
     * 父、子ID全部传过来 !!!!!!
     * 为角色分配权限
     * {
     * “sid”:1122,
     * "appType":"",
     * "clientType":"",
     * "type":""
     * "pid":11212,
     * }
     * <p>
     * <p>
     * 覆盖权限
     *
     * @param menus
     */
    private void assignCoverPermission(SysRole role, List<SysMenu> menus) {
        role.addMenu(menus);
        mapper.update(role);
    }


    /**
     * 父、子ID全部传过来 !!!!!!
     * 为角色分配权限
     * {
     * “sid”:1122,
     * "appType":"",
     * "clientType":"",
     * "type":""
     * "pid":11212,
     * }
     * <p>
     * <p>
     * 追加权限
     *
     * @param menus
     */
    private void assignAppendPermission(SysRole role, List<SysMenu> menus) {
        role.coverMenu(menus);
        mapper.update(role);
    }


    public enum Method {
        APPEND,
        COVER;
    }


    /**
     * 张朋
     * 授权数据合法性校验
     *
     * @param menus
     */
    @Deprecated
    private List<SysMenu> check(List<SysMenu> menus) {
        //数据合法性校验
        List<SysMenu> check = menus.stream().filter(t -> t.getPid() == 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(check)) {
            throw SSOException.MENU_IS_MISSING;
        }

        List<SysMenu> rMenus = Lists.newArrayList();
        TreeHandler of = TreeHandler.of(menus);
        List<SysMenu> legal = of.builTree();
        recoverMenus(rMenus, legal);
        if (rMenus.size() != menus.size()) {
            of.destroy();
            throw SSOException.MENU_IS_MISSING;
        }
        of.destroy();
        return rMenus;
    }

    /**
     * 平铺
     *
     * @param all
     * @param legal
     */
    @Deprecated
    private void recoverMenus(List<SysMenu> all, List<SysMenu> legal) {
        if (CollectionUtils.isNotEmpty(legal)) {
            for (SysMenu t : legal) {
                List<SysMenu> childs = t.getChildrens();
                t.setChildrens(null);
                t.setPid(null);
                all.add(t);
                recoverMenus(all, childs);
            }
        }
    }

}
