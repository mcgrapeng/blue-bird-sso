package com.bird.sso.domain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.bird.sso.api.domain.SSORole;
import com.bird.sso.enums.MenuTypeEnum;
import com.bird.sso.web.controller.manage.role.UserRoleForm;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 14:31
 */
@NoArgsConstructor
@Data
public class SysRole extends SelfReference<SysRole> {
    // private Integer id;
    // private String code;
    // private String name;
//    private Integer level;
//    private Integer parentId;

    private String type;
    //平铺存储，树状展示
    @JSONField(serialize = false)
    private String menus;

    //平铺存储，KV展示
    @JSONField(serialize = false)
    private String opers;


    //管理组织，json
    @JSONField(serialize = false)
    private String orgs;

    /**
     * 是否为默认角色
     */
    private String isDefault;

    private String roleType;

    private String dataScope;


    public SysRole(SSORole role) {
        this.type = role.getType();
        this.appType = role.getAppType();
        this.clientType = role.getClientType();
        this.sid = role.getSid();
        this.pid = role.getPid();
        this.code = role.getCode();
    }

    public SysRole(Long sid, String appType, Long pid) {
        this.sid = sid;
        this.appType = appType;
        this.pid = pid;
    }

    public SysRole(Long sid, String appType) {
        this.sid = sid;
        this.appType = appType;
    }


    public SysRole(Long sid, String appType, Long pid, String type) {
        this.sid = sid;
        this.appType = appType;
        this.pid = pid;
        this.type = type;
    }


    public SysRole(Long sid, String appType, Long pid, String type, String clientType) {
        this.sid = sid;
        this.appType = appType;
        this.pid = pid;
        this.type = type;
        this.clientType = clientType;
    }


    public List<Long> listPermissionIds() {
        List<Long> permIds = listPermission().stream()
                .map(x -> x.getSid()).collect(Collectors.toList());
        return permIds;
    }


    public SysMenu getMenu(Long sid) {
        List<SysMenu> menus = listPermission();
        SysMenu menu = null;
        if (CollectionUtils.isNotEmpty(menus)) {
            for (SysMenu m : menus) {
                if (m.getSid() == sid) {
                    menu = m;
                }
            }
        }
        return menu;
    }


    public SysMenu getMenu(List<SysMenu> menus, Long sid) {
        SysMenu menu = null;
        if (CollectionUtils.isNotEmpty(menus)) {
            List<SysMenu> filter = menus.stream().filter(t ->
                    String.valueOf(t.getSid()).equals(String.valueOf(sid))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filter)) {
                menu = filter.get(0);
            }
        }
        return menu;
    }


    public List<SysMenu> listPermission() {
        List<SysMenu> data = Lists.newArrayList();
        if (StringUtils.isNotBlank(this.menus)) {
            JSONArray array = JSONArray.parseArray(this.menus);
            List<SysMenu> menus = array.toJavaList(SysMenu.class);
            data.addAll(menus);
        }
        return data;
    }


    public List<SysMenu> listMenu(String type) {
        if (type.equals(MenuTypeEnum.MENU.name())) {
            if (StringUtils.isNotBlank(this.menus)) {
                JSONArray array = JSONArray.parseArray(this.menus);
                List<SysMenu> menus = array.toJavaList(SysMenu.class);
                return menus;
            }
        } else {
            if (StringUtils.isNotBlank(this.opers)) {
                JSONArray array = JSONArray.parseArray(this.opers);
                List<SysMenu> opers = array.toJavaList(SysMenu.class);
                return opers;
            }
        }
        return Lists.newArrayList();
    }


    private Set<SysMenu> listUniqueMenu(String type) {
        return new HashSet<>(listMenu(type));
    }


    public void addMenu(List<SysMenu> menus) {
        if (!CollectionUtils.isEmpty(menus)) {
            Set<SysMenu> sysMenus = listUniqueMenu(MenuTypeEnum.MENU.name());
            if (!CollectionUtils.isEmpty(sysMenus)) {
                sysMenus.addAll(menus);
                this.menus = JSONArray.toJSONString(sysMenus, SerializerFeature.DisableCircularReferenceDetect);
            } else {
                this.menus = JSONArray.toJSONString(menus, SerializerFeature.DisableCircularReferenceDetect);
            }
        } else {
            this.menus = JSONArray.toJSONString(Lists.newArrayList());
        }
    }


    /**
     * 覆盖菜单
     *
     * @param menus
     */
    public void coverMenu(List<SysMenu> menus) {
        if (!CollectionUtils.isEmpty(menus)) {
            this.menus = JSONArray.toJSONString(menus, SerializerFeature.DisableCircularReferenceDetect);
        } else {
            this.menus = JSONArray.toJSONString(Lists.newArrayList());
        }
    }


    public void addMenu(SysMenu menu) {
        addMenu(Lists.newArrayList(menu));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelfReference<?> that = (SelfReference<?>) o;
        return Objects.equals(sid, that.sid) &&
                StringUtils.equals(appType, that.appType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sid, appType);
    }


    public List<Authority> listOrgs() {
        if (StringUtils.isBlank(this.orgs)) {
            return Lists.newArrayList();
        }
        return JSONArray.parseArray(this.orgs, Authority.class);
    }


    public void appendDataAuthority(Authority authority) {
        if (StringUtils.isBlank(orgs)) {
            this.orgs = JSONArray.toJSONString(Lists.newArrayList(authority));
        } else {
            List<Authority> authorities = JSONArray.parseArray(orgs, Authority.class);
            Map<String, Authority> groupBy = authorities.stream().collect(Collectors.toMap(Authority::getCode, a -> a, (k1, k2) -> k1));
            Authority exits = groupBy.get(authority.getCode());
            if (ObjectUtils.isEmpty(exits)) {
                exits = new Authority();
                exits.setData(authority.getData());
                exits.setCode(authority.getCode());
                exits.setScope(authority.getScope());
                groupBy.put(exits.getCode(), exits);
            } else {
                exits.addData(authority.getCode(), authority.getData());
            }

            List<Authority> authorityList = groupBy.values().stream().collect(Collectors.toList());
            this.orgs = JSONArray.toJSONString(authorityList);
        }
    }


    public static List<SysRole> transform(List<UserRoleForm> roles) {
        if (CollectionUtils.isEmpty(roles)) return Lists.newArrayList();
        return roles.stream().map(t -> {
            SysRole r = new SysRole();
            com.bird.sso.utils.bean.BeanUtils.copyPropertiesIgnoreNull(t, r);
            return r;
        }).collect(Collectors.toList());
    }


    @Setter
    @Getter
    public static class Authority {
        private String code;
        private String scope;
        private List<Long> data;


        public void addData(String code, List<Long> data) {
            if (CollectionUtils.isEmpty(data)) return;
            if (!code.equals(this.code)) return;
            this.data.addAll(data);
            this.data = this.data.stream().distinct().collect(Collectors.toList());
        }

        public List<Long> getData() {
            return CollectionUtils.isEmpty(data) ? Lists.newArrayList() : data;
        }
    }

}
