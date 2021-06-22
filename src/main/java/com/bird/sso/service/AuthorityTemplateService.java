package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.common.mq.birdKafkaExecutor;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.thread.ContextAwarePoolExecutor;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.UserAssign;
import com.bird.sso.web.controller.manage.user.UserAssignBatchForm;
import com.bird.sso.web.controller.manage.user.UserAssignForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 10:55
 */
@Slf4j
public abstract class AuthorityTemplateService implements IAuthorityService {

    @Autowired
    protected RoleQueryService roleQueryService;

    @Autowired
    protected ParentChildReferenceService parentChildReferenceService;

    @Autowired
    protected UserQueryService userQueryService;

    @Autowired
    protected ContextAwarePoolExecutor executor;


    @Autowired
    protected MenuManageService menuManageService;


    @Autowired
    protected birdKafkaExecutor kafkaExecutor;

    /**
     * @param appType 应用类型
     * @param id      用户ID、组织ID
     * @return
     */
    protected abstract List<SysRole> hasAuthorities(String appType, long id, String clientTypeHasRole);

    protected abstract List<SysRole> hasAuthorities(String appType, long id);


    protected abstract void setAuthorities(String appType, long id, List<UserAssign> roles);

    protected abstract void setAuthorities(String appType, List<Long> userIds, List<UserAssign> roles);

    protected abstract void getAuthoritiesAndSet(String appType, List<Long> sids);


    @Override
    public void assignAuthorities(UserAssign role, String appType, long id, boolean isIgnoreException) {
        assignAuthorities(Lists.newArrayList(role), appType, id, null, isIgnoreException);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAuthorities(List<UserAssign> roles, String appType, long id, boolean isIgnoreException) {
        assignAuthorities(roles, appType, id, null, isIgnoreException);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignAuthorities(List<UserAssign> roles, String appType, long id) {
        assignAuthorities(roles, appType, id, null, Boolean.TRUE);
    }


    @Override
    public void assignCoverAuthorities(UserAssignBatchForm assign, String appType) {
        assignCoverAuthorities(assign.getRoles(), assign.getUserIds(), appType);
    }


    @Override
    public void assignCoverAuthorities(List<UserAssign> roles, String appType, long id) {
        List<UserAssign> unique;

        if (CollectionUtils.isEmpty(roles)) {
            throw SSOException.NULL;
        }

        if (CollectionUtils.size(roles) > 1) {
            unique = roles.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> p.getAppType() + "_" + p.getSid()
                            ))), ArrayList::new));
        } else {
            unique = roles;
        }
        setAuthorities(appType, id, unique);
    }


    @Override
    public void assignDefaultAuthorities(String appType, long id) {
        log.info(">>>>>>>>>>>>>>>>>assignDefaultAuthorities>>>>>>角色分配(默认),appType={},id={}"
                , appType, id);
        SysRole  defaultRole = roleQueryService.findDefaultRole(appType);

        if (null != defaultRole) {
            UserAssign userAssign = new UserAssign();
            BeanUtils.copyProperties(defaultRole, userAssign);
            setAuthorities(appType, id, Lists.newArrayList(userAssign));
        }
    }

    /**
     * 根据角色代码授予角色
     *
     * @param appType
     * @param userId
     * @param roleCode
     */
    @Override
    public void assignCoverAuthorities(String appType, long userId, List<String> roleCode) {
        log.info(">>>>>>>>>>>>>>>>assignAuthorities>>>>>>>角色分配,roleCode={},appType={},id={}"
                , JSON.toJSONString(roleCode), appType, userId);

        if (CollectionUtils.isEmpty(roleCode)) {
            throw SSOException.NULL;
        }
        List<String> unique;
        if (CollectionUtils.size(roleCode) > 1) {
            unique = roleCode.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(o -> o))), ArrayList::new));
        } else {
            unique = roleCode;
        }

        List<SysRole> roles = roleQueryService.findRoleByCodes(appType, null, unique);
        setAuthorities(appType, userId, roles.stream().map(t -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(t, assign);
            return assign;
        }).collect(Collectors.toList()));
    }


    /**
     * 授予全部角色
     */
    @Override
    public void assignAuthorities() {
        List<SysRole> roles = roleQueryService.listAuthoritiesRole(WebUtils.getSSOUser().getAppType());
        if (CollectionUtils.isEmpty(roles)) return;

        setAuthorities(WebUtils.getSSOUser().getAppType(), WebUtils.getSSOUser().getUserId()
                , roles.stream().map(t -> {
                    UserAssign assign = new UserAssign();
                    BeanUtils.copyProperties(t, assign);
                    return assign;
                }).collect(Collectors.toList()));
    }

    @Override
    public void assignAuthorities(String appType, long userId) {
        List<SysRole> roles = roleQueryService.listAuthoritiesRole(appType);
        if (CollectionUtils.isEmpty(roles)) return;

        setAuthorities(appType, userId, roles.stream().map(t -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(t, assign);
            return assign;
        }).collect(Collectors.toList()));
    }


    @Override
    public List<SysMenu> hasAuthorities(String appType, String authorizeClientType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("clientType", authorizeClientType);
        params.put("appType", appType);
        params.put("isAvailable", PublicEnum.Y.name());
        return menuManageService.list(params);
    }


    /**
     * "appType":"应用类型"
     * "sid" :"角色sid"
     * "name" : "角色名称"
     * "code":"角色代码"
     *
     * @param roles
     * @param appType           追加授权
     * @param id                用户ID  、组织ID
     * @param isIgnoreException 是否过滤
     */
    @Transactional(rollbackFor = Exception.class)
    protected void assignAuthorities(List<UserAssign> roles, String appType, long id, String authorizeClientType, boolean isIgnoreException) {
        log.info(">>>>>>>>>>>>>>>>assignAuthorities>>>>>>>角色分配,roles={},appType={},id={},authorizeClientType={},isIgnoreException={}"
                , JSON.toJSONString(roles), appType, id, authorizeClientType, isIgnoreException);
        if (CollectionUtils.isEmpty(roles)) return;


        List<UserAssign> unique;
        if (CollectionUtils.size(roles) > 1) {
            //去重、过滤
            unique = roles.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> p.getAppType() + "_" + p.getSid()
                            ))), ArrayList::new));
        } else {
            unique = roles;
        }

        //获取拥有的角色
        List<SysRole> exist = hasAuthorities(appType, id, authorizeClientType);

        List<UserAssign> assigns;
        if (CollectionUtils.isNotEmpty(exist)) {
            assigns = exist.stream().map(t -> {
                UserAssign assign = new UserAssign();
                BeanUtils.copyProperties(t, assign);
                return assign;

            }).collect(Collectors.toList());

            List<UserAssign> filter = unique.stream().filter(
                    role -> {
                        if (!isIgnoreException) {
                            if (assigns.contains(role)) {
                                throw new SSOException(SSOException.ROLE_IS_EXIST.getCode(), "[" + role.getName() + "]," + SSOException.ROLE_IS_EXIST.getMsg());
                            }
                        } else {
                            if (assigns.contains(role)) {
                                return Boolean.FALSE;
                            }
                        }
                        return Boolean.TRUE;
                    }
            ).collect(Collectors.toList());

            assigns.addAll(filter);
        } else {
            assigns = unique;
        }

        setAuthorities(appType, id, assigns);
    }


    /**
     * 批量用户授予批量相同角色（覆盖）(双端)
     *
     * @param roles
     * @param userIds
     * @param appType
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignCoverAuthorities(List<UserAssignForm> roles, List<Long> userIds, String appType) {
        log.info(">>>>>>>>>>>>>>>>assignAuthorities>>>>>>>角色分配,roles={},userIds={},appType={}"
                , JSON.toJSONString(roles), JSON.toJSONString(userIds), appType);
        if (CollectionUtils.isEmpty(roles) || CollectionUtils.isEmpty(userIds) || StringUtils.isBlank(appType)) return;

        List<UserAssignForm> unique;
        if (CollectionUtils.size(roles) > 1) {
            //去重、过滤
            unique = roles.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> p.getAppType() + "_" + p.getSid()
                            ))), ArrayList::new));

        } else {
            unique = roles;
        }

        setAuthorities(appType, userIds, unique.stream().map(t -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(t, assign);
            return assign;
        }).collect(Collectors.toList()));
    }

}
