package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.MenuMapper;
import com.bird.sso.mapper.RoleMapper;
import com.bird.sso.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/8 9:58
 */
@Service
@Slf4j
public class RoleQueryService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private RecursionParent recursionParent;

    @Autowired
    private RecursionChild recursionChild;

    @Autowired
    private UserQueryService userQueryService;

    /**
     * 获取默认角色(单端)
     *
     * @param appType
     * @param clientTYpe
     * @return
     */
    public SysRole findDefaultRole(String appType, String clientTYpe) {
        log.info(">>>>>>>>>>>>>>>>>hasAuthorities>>>>>>获取角色（默认）,appType={}"
                , appType);
        Map<String, Object> params = Maps.newHashMap();
        params.put("isDefault", PublicEnum.Y.name());
        params.put("clientType", clientTYpe);
        params.put("appType", StringUtils.isBlank(appType)
                ? WebUtils.getSSOUser().getAppType() : appType);
        return roleMapper.getBy(params);
    }


    /**
     * 获取默认角色
     *
     * @param appType
     * @return
     */
    public SysRole findDefaultRole(String appType) {
        log.info(">>>>>>>>>>>>>>>>>hasAuthorities>>>>>>获取角色（默认）,appType={}"
                , appType);
        Map<String, Object> params = Maps.newHashMap();
        params.put("isDefault", PublicEnum.Y.name());
        params.put("appType", appType);
        return roleMapper.getBy(params);
    }


    /**
     * 根据角色代码查询角色
     *
     * @param appType
     * @param code
     * @return
     */
    public SysRole findRoleByCode(String appType, String code) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("code", code);
        params.put("appType", appType);
        return roleMapper.getBy(params);
    }


    /**
     * 获取系统所有的授权角色
     *
     * @return
     */
    public List<SysRole> listAuthoritiesRole(String appType) {
        return roleMapper.selectALLRolesByAppType(appType);
    }


    /**
     * 获取系统所有的授权角色
     *
     * @return
     */
    public List<SysRole> listAuthoritiesRoleContainMenus(String appType) {
        return roleMapper.selectHasMenus(appType);
    }


    /**
     * 根据角色代码查询角色
     *
     * @param appType
     * @param codes
     * @return
     */
    public List<SysRole> findRoleByCodes(String appType, List<String> codes) {
        return roleMapper.selectColumnByCodes(appType, codes);
    }


    /**
     * 根据角色代码查询角色
     *
     * @param appType
     * @param clientType
     * @param codes
     * @return
     */
    public List<SysRole> findRoleByCodes(String appType, String clientType, List<String> codes) {
        return roleMapper.selectByAppType_ClientType_Codes(appType, clientType, codes);
    }


    /**
     * 根据角色ID查询角色
     *
     * @param appType
     * @param sids
     * @return
     */
    public List<SysRole> findRoleBySids(String appType, List<Long> sids) {
        return roleMapper.selectTreeColumnBySids(appType, sids);
    }


    public List<SysRole> findRoleDataScopeBySids(String appType, List<Long> sids) {
        return roleMapper.selectDataScopeBySids(appType, sids);
    }


    /**
     * 获取角色
     *
     * @param sid
     * @param appType
     * @return
     */
    public SysRole findRole(long sid, String appType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sid", sid);
        params.put("appType", appType);
        return roleMapper.getBy(params);
    }


    /*
       获取父角色
     * @param pid
     * @return
     */
    public List<SysRole> findRoleByPid(String appType, long pid) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pid", pid);
        params.put("appType", appType);
        return roleMapper.selectBy(params);
    }

    /**
     * 获取级联角色（包含自己）
     *
     * @param roleId
     * @return
     */
    public List<SysRole> listRecursionRoleContainSelf(String appType, long roleId) {
        return recursionParent.build()
                .recursionParentRole(appType, roleId);
    }


    public List<SysRole> listRecursionRole(String appType, long parentId) {
        return recursionParent.build()
                .recursionParentRole(appType, parentId);
    }


    /**
     * 获取菜单
     *
     * @param sids
     * @param clientType
     * @return
     */
    public List<SysMenu> listDuplicateMenu(Set<Long> sids, String clientType) {
        return menuMapper
                .selectUnpAvailableBySids(WebUtils.getSSOUser().getAppType()
                        , clientType, sids);
    }

    /**
     * 获取菜单路径
     *
     * @param sids
     * @param clientType
     * @return
     */
    public List<String> listDuplicateMenuPath(String appType, Set<Long> sids, String clientType) {
        return menuMapper
                .selectPathUnpAvailableBySids(appType
                        , clientType, sids);
    }


    /**
     * 获取级联子角色（不含自己）
     *
     * @param roleId
     * @return
     */
    public List<SysRole> listRecursionChildRole(String appType, long roleId) {
        return recursionChild.build().recursionChildRole(appType, roleId);
    }


    /**
     * 根据角色获取资源路径
     *
     * @param appType
     * @param loginSource
     * @param roleIds
     * @return
     */
    public String findMenuPathByRoleIds(String appType, String loginSource, List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) return "[]";
        List<String> roleMenus = roleMapper.selectMenusBySids(appType, roleIds);
        if (CollectionUtils.isEmpty(roleMenus)) return "[]";

        Set<Long> mids = Sets.newHashSet();
        roleMenus.stream().forEach(menu -> {
            List<SysMenu> menus = JSONArray.parseArray(menu, SysMenu.class);
            if (CollectionUtils.isNotEmpty(menus)) {
                List<Long> m = menus.stream().map(t -> t.getSid()).collect(Collectors.toList());
                mids.addAll(m);
            }
        });

        if (CollectionUtils.isEmpty(mids)) return "[]";

        List<String> menus = listDuplicateMenuPath(appType, mids, loginSource);

        log.info(">>>>>>>menus>>>>>>>>>menus={}", JSON.toJSONString(menus));
        if (CollectionUtils.isNotEmpty(menus)) {
            List<SimpleGrantedAuthority> grantedAuthorities = menus.stream().filter(t -> StringUtils.isNotBlank(t))
                    .map(t -> {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(t);
                        return authority;
                    }).collect(Collectors.toList());

            return JSON.toJSONString(grantedAuthorities);
        }
        return "[]";
    }


    public List<Long> findRoleIdsByUserId(String appType, Long userId) {
        SysUser user = userQueryService.findUserByUserId(appType, userId);
        return user.getAuthority().listRoleIds();
    }

    @Component
    class RecursionParent implements ApplicationContextAware {
        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        public ParentNodes build() {
            ParentNodes build = applicationContext.getBean(ParentNodes.class);
            return build;
        }

        @Component
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        class ParentNodes {

            private List<SysRole> data = Lists.newArrayList();

            public List<SysRole> recursionParentRole(String appType, long parentRoleId) {
                if (parentRoleId == 0) {
                    return data;
                }
                SysRole role = findRole(parentRoleId, appType);
                data.add(role);
                return recursionParentRole(role.getAppType(), role.getPid());
            }
        }
    }


    @Component
    class RecursionChild implements ApplicationContextAware {
        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        public ChildNodes build() {
            ChildNodes build = applicationContext.getBean(ChildNodes.class);
            return build;
        }

        @Component
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        class ChildNodes {
            private List<SysRole> data = Lists.newArrayList();

            public List<SysRole> recursionChildRole(String appType, long roleId) {
                List<SysRole> childs = findRoleByPid(appType, roleId);
                if (CollectionUtils.isNotEmpty(childs)) {
                    data.addAll(childs);
                    childs.stream().forEach(t -> {
                        recursionChildRole(t.getAppType(), t.getSid());
                    });
                }
                return data;
            }
        }
    }


}
