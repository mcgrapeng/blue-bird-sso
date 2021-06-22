package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.bird.sso.api.enums.ClientEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.core.TreeHandler;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.domain.SelfReference;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.MenuMapper;
import com.bird.sso.mapper.RoleMapper;
import com.bird.sso.mapper.UserMapper;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.UserAssign;
import com.bird.sso.web.controller.manage.user.UserAssignForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 11:11
 */
@Slf4j
@Service
public class UserAuthorityService extends AuthorityTemplateService {

    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private UserQueryService userQueryService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private MenuMapper menuMapper;


    @Autowired
    private RoleQueryService roleQueryService;

    @Override
    public List<SysRole> hasAuthorities(String appType, long id) {
        return hasAuthorities(appType, id, null);
    }


    @Override
    public List<SysRole> hasAuthorities(String appType, long id, String clientTypeHasRole) {
        log.info(">>>>>>>>>>>>>>>>>hasAuthorities>>>>>>用户拥有的权限（角色）,roles,appType={},id={}"
                , appType, id);

        //获取拥有的角色
        SysUser user = userQueryService.findUserByUserId(appType
                , id);
        if (ObjectUtils.isEmpty(user)) {
            throw SSOException.USER_NO_EXITS;
        }
//        List<SysRole> exist = user.getAuthority().listRole();
        //防止缓存
        List<Long> existSids = user.getAuthority().listRoleIds();
        if (CollectionUtils.isEmpty(existSids)) {
            return Lists.newArrayList();
        }

        List<SysRole> exist;
        if (StringUtils.isBlank(clientTypeHasRole)) {
            exist = roleMapper.selectColumnBySids(appType, existSids);
        } else {
            exist = roleMapper.selectBySids(appType, clientTypeHasRole, existSids);
        }

        if (CollectionUtils.isEmpty(exist)) {
            return Lists.newArrayList();
        }
        return exist;
    }


    @Override
    protected void setAuthorities(String appType, long id, List<UserAssign> roles) {
        log.info(">>>>>>>>>>>>>>>>>setAuthorities>>>>>>用户设置权限,roles={},appType={},id={}"
                , JSON.toJSONString(roles), appType, id);

        if (CollectionUtils.isEmpty(roles)) {
            return;
        }

        SysUser user = new SysUser();
        user.setAppType(appType);
        user.setUserId(id);
        user.setRoles(JSON.toJSONString(roles));

        userMapper.update(user);
    }

    @Override
    protected void setAuthorities(String appType, List<Long> userIds, List<UserAssign> roles) {
        log.info(">>>>>>>>>>>>>>>>>setAuthorities>>>>>>用户设置权限,roles={},appType={},userIds={}"
                , JSON.toJSONString(roles), appType, JSON.toJSONString(userIds));
        List<SysUser> users = userQueryService.listUserByUserIds(appType, userIds);

        if (CollectionUtils.isEmpty(users) || CollectionUtils.isEmpty(roles)) {
            return;
        }

        users.stream().forEach(t -> {
            SysUser user = new SysUser();
            user.setAppType(appType);
            user.setUserId(t.getUserId());
            user.setRoles(JSON.toJSONString(roles));
            userMapper.update(user);
        });
    }


    @Override
    public List<SysMenu> hasAuthorities() {
        String clientType = WebUtils.getHeader(JWTHelper.CLAIM_KEY_LOGIN_SOURCE);
        log.info("#########################用户拥有的权限" +
                ",userPasswordForget##########clientType={}", clientType);

        SysUser user = userQueryService.findUserByUserName(WebUtils.getSSOUser().getAppType(),
                WebUtils.getSSOUser().getUserName());
        List<Long> roleIds = user.getAuthority().listRoleIds();
        if (CollectionUtils.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }

        List<SysRole> roles = roleMapper.selectBySids(WebUtils.getSSOUser().getAppType()
                , clientType, roleIds);
        if (CollectionUtils.isEmpty(roles)) {
            return Lists.newArrayList();
        }


        Set<Long> menuIds = Sets.newHashSet();
        roles.stream().forEach(t -> {
            List<Long> l = t.listPermissionIds();
            if (CollectionUtils.isNotEmpty(l)) {
                menuIds.addAll(l);
            }
        });


        if (CollectionUtils.isEmpty(menuIds)) {
            return Lists.newArrayList();
        }

        List<SysMenu> sysMenus = menuMapper.selectUnpAvailableBySids(WebUtils.getSSOUser().getAppType()
                , clientType, menuIds);
        if (CollectionUtils.isEmpty(sysMenus)) {
            return Lists.newArrayList();
        }

        if (CollectionUtils.size(sysMenus) > 1) {
            sysMenus.sort(Comparator.comparingInt(SelfReference::getOrd));
        }

        List<SysMenu> tree;
        if (JWTHelper.getLoginSource().equals(ClientEnum.PC.name())) {
            TreeHandler of = TreeHandler.of(sysMenus);
            tree = of.builTree();
            of.destroy();
            sysMenus.clear();
        } else {
            tree = sysMenus;
        }
        return tree;
    }


    @Override
    public List<SysMenu> hasAuthorities(long roleId) {
        SysRole role = roleQueryService.findRole(roleId, WebUtils.getSSOUser().getAppType());
        if (ObjectUtils.isEmpty(role)) {
            throw SSOException.ROLE_IS_NOT_EXIST;
        }

        List<Long> menuIds = role.listPermissionIds();

        if (CollectionUtils.isEmpty(menuIds)) {
            return Lists.newArrayList();
        }

        String clientType = JWTHelper.getLoginSource();

        List<SysMenu> sysMenus = menuMapper.selectAvailableBySids(WebUtils.getSSOUser().getAppType(), clientType, menuIds);
        if (CollectionUtils.isEmpty(sysMenus)) {
            return Lists.newArrayList();
        }

        if (CollectionUtils.size(sysMenus) > 1) {
            sysMenus.sort(Comparator.comparingInt(SelfReference::getOrd));
        }
        return sysMenus;
    }


    public List<SysMenu> hasMenus(String appType, String clientType, long userId) {
        SysUser user = userQueryService.findUserByUserId(appType,
                userId);
        List<Long> roleIds = user.getAuthority().listRoleIds();
        if (CollectionUtils.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }

        List<Long> menuIds = Lists.newArrayList();
        List<SysRole> roles = roleMapper.selectBySids(appType, JWTHelper.getLoginSource(), roleIds);
        roles.stream().forEach(t -> {
            List<Long> l = t.listPermissionIds();
            if (CollectionUtils.isNotEmpty(l)) {
                menuIds.addAll(l);
            }
        });

        if (CollectionUtils.isEmpty(menuIds)) {
            return Lists.newArrayList();
        }

        List<SysMenu> sysMenus;
        if (StringUtils.isBlank(clientType)) {
            sysMenus = menuMapper.selectColumnBySids(appType, menuIds);
        } else {
            sysMenus = menuMapper.selectAvailableBySids(appType, clientType, menuIds);
        }
        return sysMenus;
    }


    /**
     * 用户角色清理
     *
     * @param appType
     * @param sids
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void getAuthoritiesAndSet(String appType, List<Long> sids) {
        log.info(">>>>>>>>>>>>>>>>>>>>>用户--角色信息>>>getAuthoritiesAndSet start >>>>>>>>>>>>>>>>>>>>>> sids = {} ", JSON.toJSONString(sids));
        if (CollectionUtils.isEmpty(sids)) return;

        List<SysUser> users
                = userQueryService.listAuthoritiesUser(appType);

        if (CollectionUtils.isEmpty(users)) return;


        List<SysRole> removeRoles = sids.stream().map(t -> {
            SysRole r = new SysRole();
            r.setSid(t);
            r.setAppType(appType);
            return r;
        }).collect(Collectors.toList());

        for (SysUser u : users) {
            List<SysRole> oldRoles = u.getAuthority().listRole();
            if (CollectionUtils.isEmpty(oldRoles)) continue;
            if (oldRoles.removeAll(removeRoles)) {
                u.setRoles(JSONArray.toJSONString(oldRoles));
                userMapper.update(u);
            }
        }
    }
}
