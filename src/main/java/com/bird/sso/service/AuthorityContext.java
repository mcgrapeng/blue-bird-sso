package com.bird.sso.service;

import com.google.common.collect.Lists;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.utils.SpringContextUtils;
import com.bird.sso.web.UserAssign;
import com.bird.sso.web.controller.manage.user.UserAssignBatchForm;
import com.bird.sso.web.controller.manage.user.UserAssignForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 11:41
 */
@Slf4j
@Component
public class AuthorityContext {

    /**
     * 根据角色查询权限
     *
     * @param strategy
     * @param roleId
     * @return
     */
    public List<SysMenu> hasAuthorities(Strategy strategy, long roleId) {
        return getAuthorityService(strategy).hasAuthorities(roleId);
    }

    public List<SysMenu> hasAuthorities(Strategy strategy) {
        return getAuthorityService(strategy).hasAuthorities();
    }

    public List<SysMenu> hasAuthorities(Strategy strategy, String appType, String authorizeClientType) {
        return getAuthorityService(strategy).hasAuthorities(appType, authorizeClientType);
    }


    public void assignBatchCoverAuthorities(Strategy strategy, UserAssignBatchForm assign, String appType) {
        getAuthorityService(strategy).assignCoverAuthorities(assign, appType);
    }

    public void assignAuthorities(Strategy strategy, UserAssign role, String appType, long id) {
        assignAuthorities(strategy, Lists.newArrayList(role), appType, id);
    }


    public void assignAuthorities(Strategy strategy, List<UserAssign> roles, String appType, long id) {
        getAuthorityService(strategy).assignAuthorities(roles, appType, id, Boolean.FALSE);
    }

    public void assignAuthorities(Strategy strategy, List<UserAssign> roles, String appType, long id,boolean isIgnoreException) {
        getAuthorityService(strategy).assignAuthorities(roles, appType, id, isIgnoreException);
    }


    public void assignCoverAuthorities(Strategy strategy, List<UserAssign> roles, String appType, long id) {
        getAuthorityService(strategy).assignCoverAuthorities(roles, appType, id);
    }


    private IAuthorityService getAuthorityService(Strategy strategy) {
        IAuthorityService iAuthorityService = null;
        if (strategy.equals(Strategy.USER)) {
            iAuthorityService = SpringContextUtils.getBean(UserAuthorityService.class);
        } else if (strategy.equals(Strategy.ORG)) {
            iAuthorityService = SpringContextUtils.getBean(OrgAuthorityService.class);
        } else {
            log.error(">>>>>>>>>>>>>>>>>>>策略获取异常>>>>>>>>>>>>>>>>>>");
        }
        return iAuthorityService;
    }


    public enum Strategy {
        ORG,
        USER;
    }

}
