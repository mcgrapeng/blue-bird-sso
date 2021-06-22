package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.api.enums.RoleTypeEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.domain.SysRole;
import com.bird.sso.enums.DataScopeEnum;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.controller.manage.range.UserRangeForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/12/3 16:05
 */
@Slf4j
@Service
public class RangeManageService {


    @Autowired
    private RoleManageService roleManageService;


    @Autowired
    private RoleQueryService roleQueryService;


    @Autowired
    private OrganizeQueryService organizeQueryService;


    @Autowired
    private UserAuthorityService userAuthorityService;


    /**
     * 当前角色拥有的数据权限
     *
     * @param sid
     * @return
     */
    public List<Long> hasDataPermission(long sid) {
        SysRole role = roleQueryService.findRole(sid, WebUtils.getSSOUser().getAppType());
        if (ObjectUtils.isEmpty(role)) {
            throw SSOException.ROLE_IS_NOT_EXIST;
        }
        String orgs = role.getOrgs();
        if (StringUtils.isBlank(orgs) || StringUtils.equals("[]", orgs)) return Lists.newArrayList();
        return JSONArray.parseArray(orgs, Long.class);
    }


    public List<Long> hasDataPermission(String code) {
        SysRole role = roleQueryService.findRoleByCode(WebUtils.getSSOUser().getAppType(), code);
        if (ObjectUtils.isEmpty(role)) {
            throw SSOException.ROLE_IS_NOT_EXIST;
        }
        String orgs = role.getOrgs();
        if (StringUtils.isBlank(orgs) || StringUtils.equals("[]", orgs)) return Lists.newArrayList();
        return JSONArray.parseArray(orgs, Long.class);
    }


    @Transactional(rollbackFor = Exception.class)
    public void assignDataPermission(String type, UserRangeForm rangeForm) {
        if (type.equals(DataScopeEnum.ALL.name())) {
            doAssignDataPermission(rangeForm.getAppType()
                    , rangeForm.getRoleId());
        } else if (type.equals(DataScopeEnum.DEP.name())) {
            doAssignCurrentDataPermission(rangeForm.getAppType()
                    , rangeForm.getRoleId());
        } else if (type.equals(DataScopeEnum.DER.name())) {
            doAssignCurrentDataPermission(rangeForm.getAppType(), rangeForm.getRoleId()
                    , rangeForm.getOrgId());
        } else {
            doAssignCustomDataPermission(rangeForm.getAppType(), rangeForm.getRoleId()
                    , rangeForm.getRange());
        }
    }


    /**
     * 为当前角色授予所有组织的数据权限
     *
     * @param appType
     * @param sid
     */
    private void doAssignDataPermission(String appType, Long sid) {
        if (ObjectUtils.isEmpty(sid) || StringUtils.isBlank(appType)) return;
        //获取应用下全部组织
        List<SysOrganization> organizations = organizeQueryService.listByAppType(appType);
        if (CollectionUtils.isEmpty(organizations)) return;
        SysRole r = new SysRole(sid, appType);
        r.setOrgs(JSON.toJSONString(Lists.newArrayList(organizations.stream()
                .map(t -> t.getSid()).collect(Collectors.toList()))));
        roleManageService.updInfo(r);
    }


    /**
     * 为当前角色授予本组织的数据权限
     *
     * @param appType
     * @param sid
     */
    private void doAssignCurrentDataPermission(String appType, Long sid) {
        if (ObjectUtils.isEmpty(sid) || StringUtils.isBlank(appType)) return;

        Long orgId = WebUtils.getSSOUser().getOrgId();
        SysRole r = new SysRole(sid, appType);
        r.setOrgs(JSON.toJSONString(Lists.newArrayList(orgId)));
        roleManageService.updInfo(r);
    }


    /**
     * 为当前角色授予本组织及以下组织的数据权限
     *
     * @param appType
     * @param orgId
     */
    private void doAssignCurrentDataPermission(String appType, Long roleId, Long orgId) {
        if (ObjectUtils.isEmpty(roleId) || ObjectUtils.isEmpty(orgId) || StringUtils.isBlank(appType)) return;
        List<SysOrganization> organizations = organizeQueryService.listUnionByPid(appType, orgId);
        SysRole r = new SysRole(roleId, appType);
        r.setOrgs(JSON.toJSONString(Lists.newArrayList(organizations.stream()
                .map(t -> t.getSid()).collect(Collectors.toList()))));
        roleManageService.updInfo(r);
    }


    /**
     * 为当前角色分配数据权限（自定义）
     *
     * @param appType
     * @param sid
     * @param ranges
     */
    private void doAssignCustomDataPermission(String appType, long sid, List<Long> ranges) {
        if (ObjectUtils.isEmpty(sid) || CollectionUtils.isEmpty(ranges) || StringUtils.isBlank(appType)) return;
        SysRole r = new SysRole(sid, appType);
        r.setOrgs(JSON.toJSONString(Lists.newArrayList(ranges)));
        roleManageService.updInfo(r);
    }
}
