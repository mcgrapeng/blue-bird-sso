package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 10:23
 */
@Deprecated
@Slf4j
@Service
public class ParentChildReferenceService {


    @Autowired
    private RoleQueryService roleQueryService;

    @Autowired
    private RoleMapper roleMapper;


    /**
     * 移除当前角色下的所有子角色
     *
     * @param hasRole 拥有的角色ID
     * @param sid     当前要新增的角色 sid
     */
    public void removeChildRole(List<SysRole> hasRole, long sid, String appType) {
        log.info(">>>>>>>>>>removeChildRole>>>>>>>>>>>>>,sid={}，appType={}，hasRole={}"
                , sid, appType,JSON.toJSONString(hasRole));
        List<SysRole> childs = hasRole.stream().filter(
                role -> Objects.equals(role.getPid(), sid) && StringUtils.equals(appType, role.getAppType())).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(childs)) {
            hasRole.removeAll(childs);
            childs.forEach(t -> {
                removeChildRole(hasRole, t.getSid(), t.getAppType());
            });
        } else {
            childs = roleQueryService.findRoleByPid(appType, sid);
            if (CollectionUtils.isNotEmpty(childs)) {
                childs.forEach(t -> {
                    removeChildRole(hasRole, t.getSid(), t.getAppType());
                });
            }
        }
    }


    /**
     * 是否存在父角色
     *
     * @param hasRole 角色ID集合
     * @param pid     当前角色pid
     * @return
     */
    public boolean isExistsParentRole(List<SysRole> hasRole, long pid, String appType) {
        log.info(">>>>>>>>>>isExistsParentRole>>>>>>>>>>>>>,pid={}，appType={}，hasRole={}"
                , pid, appType,JSON.toJSONString(hasRole));
        List<SysRole> exists = hasRole.stream().filter(
                role -> Objects.equals(role.getSid(), pid) && StringUtils.equals(appType, role.getAppType())
        ).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(exists)) {
            return Boolean.TRUE;
        }

        if (CollectionUtils.isEmpty(exists) && pid == 0) {
            return Boolean.FALSE;
        }

        SysRole role = roleQueryService.findRole(pid, appType);
        if (null == role) {
            return Boolean.FALSE;
        }
        return isExistsParentRole(hasRole, role.getPid(), role.getAppType());
    }


    /**
     * 检查添加的角色是否包含父角色
     *
     * @param roles
     */
    public void checkParentChild(List<SysRole> roles) {
        roles.forEach(t -> {
            if (isExistsParentRole(roles, t.getPid(), t.getAppType())) {
                throw SSOException.CONFLICT_ROLE;
            }
        });
    }


    /**
     * 递归为父角色授权
     *
     * @param pid
     * @param filter
     */
    public void assignPermissionForParent(long pid, String appType, List<SysMenu> filter) {
        SysRole roleParent = roleQueryService.findRole(pid, appType);
        if (null != roleParent) {
            roleParent.addMenu(filter);
            roleMapper.update(roleParent);
            assignPermissionForParent(roleParent.getPid(), roleParent.getAppType(), filter);
        }
    }
}
