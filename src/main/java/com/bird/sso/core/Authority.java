package com.bird.sso.core;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.bird.sso.domain.SysRole;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/10 10:33
 */
@Getter
@Setter
public class Authority {

    private String roles;

    public Authority(String roles) {
        this.roles = roles;
    }

    public SysRole getRole(List<SysRole> roles, Long sid) {
        SysRole role = null;
        if (CollectionUtils.isNotEmpty(roles)) {
            List<SysRole> filter = roles.stream().filter(t -> String.valueOf(t.getSid()).equals(String.valueOf(sid))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filter)) {
                role = filter.get(0);
            }
        }
        return role;
    }


    public List<Long> listRoleIds() {
        List<SysRole> roles = listRole();
        if (CollectionUtils.isNotEmpty(roles)) {
            List<Long> roleIds = roles.stream()
                    .map(x -> x.getSid()).collect(Collectors.toList());
            return roleIds;
        }
        return Lists.newArrayList();
    }

    public List<SysRole> listRole() {
        List<SysRole> roles = Lists.newArrayList();
        if (StringUtils.isNotBlank(this.roles)) {
            JSONArray arr = JSONArray.parseArray(this.roles);
            return arr.toJavaList(SysRole.class);
        }
        return roles;
    }


    public List<String> listRoleCode() {
        if (StringUtils.isNotBlank(this.roles)) {
            List<SysRole> roles = JSONArray.parseArray(this.roles, SysRole.class);
            return roles.stream().map(t -> t.getCode()).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private Set<SysRole> listUniqueRole() {
        return new HashSet<>(listRole());
    }


    public void coverRole(List<SysRole> roles) {
        this.roles = JSONArray.toJSONString(roles);
    }

    public void addRole(List<SysRole> roles) {
        if (!CollectionUtils.isEmpty(roles)) {
            Set<SysRole> persistRoles = listUniqueRole();
            if (!CollectionUtils.isEmpty(persistRoles)) {
                persistRoles.addAll(roles);
                this.roles = JSONArray.toJSONString(persistRoles);
            } else {
                this.roles = JSONArray.toJSONString(roles);
            }
        } else {
            this.roles = JSONArray.toJSONString(roles);
        }
    }

    public void addRole(SysRole role) {
        if (ObjectUtils.isNotEmpty(role)) {
            addRole(Lists.newArrayList(role));
        }
    }

    public void clear() {
        roles = null;
    }

}
