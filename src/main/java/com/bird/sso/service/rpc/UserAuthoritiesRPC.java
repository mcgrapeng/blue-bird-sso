package com.bird.sso.service.rpc;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.bird.sso.api.IUserAuthoritiesService;
import com.bird.sso.api.domain.SSORole;
import com.bird.sso.domain.SysRole;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.RoleQueryService;
import com.bird.sso.service.UserAuthorityService;
import com.bird.sso.service.UserQueryService;
import com.bird.sso.web.UserAssign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/21 12:01
 */
@org.apache.dubbo.config.annotation.Service(interfaceName = "com.bird.sso.api.IUserAuthoritiesService", protocol = "dubbo", version = "1.0", retries = 3
        , timeout = 60000, loadbalance = "random", executes = 200, actives = 0, cluster = "failover")
@Slf4j
public class UserAuthoritiesRPC implements IUserAuthoritiesService {


    @Autowired
    private UserAuthorityService userAuthorityService;


    @Autowired
    private UserQueryService userQueryService;


    @Autowired
    private RoleQueryService roleQueryService;


    @Override
    public void assignAuthorities(List<SSORole> roles, String appType, long id) {
        log.info("############assignAuthorities###########,appType={},id={},roles={}"
                , appType, id, JSON.toJSONString(roles));
        if (ObjectUtils.isEmpty(id) || StringUtils.isBlank(appType) || CollectionUtils.isEmpty(roles)) {
            return;
        }
        userAuthorityService.assignAuthorities(roles.stream().map(x -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(x, assign);
            return assign;
        }).collect(Collectors.toList()), appType, id);
    }

    @Override
    public void assignCoverAuthorities(List<SSORole> roles, String appType, long id) {
        log.info("############assignCoverAuthorities###########,appType={},id={},roles={}"
                , appType, id, JSON.toJSONString(roles));
        if (ObjectUtils.isEmpty(id) || StringUtils.isBlank(appType) || CollectionUtils.isEmpty(roles)) {
            return;
        }
        userAuthorityService.assignCoverAuthorities(roles.stream().map(x -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(x, assign);
            return assign;
        }).collect(Collectors.toList()), appType, id);
    }

    @Override
    public void assignAuthorities(String appType, String userName, List<Long> roleIds) {
        SysUser user = userQueryService.findUserByUserName(appType, userName);
        List<SysRole> roles = roleQueryService.findRoleBySids(appType, roleIds);
        userAuthorityService.assignAuthorities(roles.stream().map(x -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(x, assign);
            return assign;
        }).collect(Collectors.toList()), user.getAppType(), user.getUserId());
    }

    @Override
    public void assignAuthorities(String appType, String authorizeClientType, String userName, List<Long> roleIds) {
        SysUser user = userQueryService.findUserByUserName(appType, userName);
        List<SysRole> roles = roleQueryService.findRoleBySids(appType, roleIds);
        userAuthorityService.assignAuthorities(roles.stream().map(x -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(x, assign);
            return assign;
        }).collect(Collectors.toList()), user.getAppType(), user.getUserId());
    }


    @Override
    public void assignInitialAuthorities(String appType, String userName, List<SSORole> roles) {
        log.info("############assignInitialAuthorities###########,appType={},userName={},roles={}"
                , appType, userName, JSON.toJSONString(roles));
        if (StringUtils.isBlank(appType) || StringUtils.isBlank(userName) || CollectionUtils.isEmpty(roles)) {
            return;
        }

        SysUser user = userQueryService.findUserByUserName(appType, userName);

        userAuthorityService.assignCoverAuthorities(roles.stream().map(x -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(x, assign);
            return assign;
        }).collect(Collectors.toList()), appType, user.getUserId());
    }

    @Override
    public void assignInitialAuthorities(String appType, String authorizeClientType, String userName, List<SSORole> roles) {
        log.info("############assignInitialAuthorities###########,appType={},userName={},roles={}"
                , appType, userName, JSON.toJSONString(roles));
        if (StringUtils.isBlank(appType) || StringUtils.isBlank(userName) || StringUtils.isBlank(authorizeClientType) || CollectionUtils.isEmpty(roles)) {
            return;
        }

        SysUser user = userQueryService.findUserByUserName(appType, userName);
        userAuthorityService.assignCoverAuthorities(roles.stream().map(x -> {
            UserAssign assign = new UserAssign();
            BeanUtils.copyProperties(x, assign);
            return assign;
        }).collect(Collectors.toList()), appType, user.getUserId());
    }

    @Override
    public void assignInitialAuthorities(String appType, String userName, SSORole role) {
        assignInitialAuthorities(appType, userName, Lists.newArrayList(role));
    }

    @Override
    public void assignDefaultAuthorities(String appType, long userId, String authorizeClientType) {
        log.info("############assignDefaultAuthorities###########,appType={},userId={},authorizeClientType={}"
                , appType, userId, authorizeClientType);
        userAuthorityService.assignDefaultAuthorities(appType, userId);
    }

    @Override
    public void assignDefaultAuthorities(String appType, long userId) {
        userAuthorityService.assignDefaultAuthorities(appType, userId);
    }


    @Override
    public String getMaxRoleCode(String appType, String authorizeClientType, long userId) {
        SysUser user = userQueryService.findUserByUserId(appType, userId);
        List<SysRole> roles = user.getAuthority().listRole();
        if (CollectionUtils.isNotEmpty(roles)) {
            List<SysRole> data = roles.stream().filter(ssoRole -> ssoRole.getClientType()
                    .equals(authorizeClientType)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(data)) {
                return data.get(0).getCode();
            }
        }
        return null;
    }
}
