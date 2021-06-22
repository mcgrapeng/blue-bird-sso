package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.bird.sso.core.TreeHandler;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.domain.SysRole;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.MenuMapper;
import com.bird.sso.mapper.OrganizationMapper;
import com.bird.sso.mapper.RoleMapper;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.UserAssign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 11:23
 */
@Deprecated
@Slf4j
@Service
public class OrgAuthorityService extends AuthorityTemplateService {

    @Autowired
    private RoleMapper roleMapper;


    @Autowired
    private MenuMapper menuMapper;


    @Autowired
    private OrganizationMapper organizationMapper;


    @Autowired
    private OrganizeQueryService organizationQueryService;

    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private UserQueryService userQueryService;


    @Override
    protected List<SysRole> hasAuthorities(String appType,long id,String authorizeClientType) {
        //获取当前用户所属组织拥有的角色
        SysOrganization organization = organizationQueryService.getBasicOrganization(appType, id);
        if (null == organization) {
            return Lists.newArrayList();
        }
        List<SysRole> exist = organization.getAuthority().listRole();
        return exist;
    }


    @Override
    protected List<SysRole> hasAuthorities(String appType, long id) {
        return null;
    }

    @Override
    protected void setAuthorities(String appType, long id, List<UserAssign> roles) {
        SysOrganization organization = new SysOrganization();
        organization.setAppType(appType);
        organization.setSid(id);
        organization.setRoles(JSON.toJSONString(roles));
        organizationMapper.update(organization);
    }

    @Override
    protected void setAuthorities(String appType, List<Long> userIds, List<UserAssign> roles) {

    }

    @Override
    public List<SysMenu> hasAuthorities() {

        List<SysMenu> sysMenus = userAuthorityService.hasAuthorities();
        if (CollectionUtils.isNotEmpty(sysMenus)) {
            return sysMenus;
        }

        SysUser user = userQueryService.findUserByUserId(WebUtils.getSSOUser().getAppType()
                , WebUtils.getSSOUser().getUserId());

        SysOrganization organization = organizationQueryService.getBasicOrganization(WebUtils.getSSOUser().getAppType()
                , user.getOrgId());

        if (null == organization) {
            return Lists.newArrayList();
        }

        List<Long> roleIds = organization.getAuthority().listRoleIds();
        if (CollectionUtils.isEmpty(roleIds)) {
            return Lists.newArrayList();
        }

        List<Long> menuIds = Lists.newArrayList();
        List<SysRole> roles = roleMapper.selectBySids(WebUtils.getSSOUser().getAppType(), JWTHelper.getLoginSource(),roleIds);
        roles.stream().forEach(t -> {
            List<Long> l = t.listPermissionIds();
            if (CollectionUtils.isNotEmpty(l)) {
                menuIds.addAll(l);
            }
        });

        if (CollectionUtils.isEmpty(menuIds)) {
            return Lists.newArrayList();
        }

        String clientType = JWTHelper.getLoginSource();
        sysMenus = menuMapper.selectAvailableBySids(WebUtils.getSSOUser().getAppType(),clientType, menuIds);

        List<SysMenu> filter = sysMenus.stream().filter(sysMenu -> sysMenu.getClientType().equals(clientType)).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(filter)){
            return Lists.newArrayList();
        }

        TreeHandler of = TreeHandler.of(filter);
        List<SysMenu> tree = of.builTree();
        of.destroy();
        sysMenus.clear();
        return tree;
    }

    @Override
    public List<SysMenu> hasAuthorities(long roleId) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void getAuthoritiesAndSet(String appType, List<Long> sids) {
//        log.info(">>>>>>>>>>>>>>>>>>>>>组织--角色信息>>>getAuthoritiesAndSet start >>>>>>>>>>>>>>>>>>>>>> sids = {} ", JSON.toJSONString(sids));
//        if (CollectionUtils.isEmpty(sids)) return;
//
//        List<SysOrganization> sysOrganizations = organizationQueryService.listAuthoritiesRole(appType);
//        if (CollectionUtils.isEmpty(sysOrganizations)) return;
//
//
//        for (SysOrganization organization : sysOrganizations) {
//
//            List<SysRole> oldRoles = organization.getAuthority().listRole();
//            if (CollectionUtils.isEmpty(oldRoles)) continue;
//
//
//            //重置用户授予的角色
//            for (Long sid : sids) {
//                //找出要删除的当前节点
//                SysRole role = organization.getAuthority().getRole(oldRoles, sid);
//                if (null != role) {
//                    oldRoles.remove(role);
//                }
//
//                //删除当前节点的所有子节点
//                List<SysRole> roles = roleQueryService.listRecursionChildRole(appType, sid);
//                oldRoles.removeAll(roles);
//            }
//            organization.getAuthority().coverRole(oldRoles);
//            organizationMapper.update(organization);
//        }
    }



}
