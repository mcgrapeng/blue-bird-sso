package com.bird.sso.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.bird.sso.core.Authority;
import com.bird.sso.web.controller.manage.organize.OrganizationForm;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 14:40
 */
@Data
public class SysOrganization extends SelfReference<SysOrganization> {
    // private Integer id;
    // private String code;
    // private String name;
    // private String isAvailable;
    //private String type;
//      private String appType;
//    private Integer level;
//    private Integer parentId;
//    private Integer ord;
    private String roles;
    // private Date createTime;
    // private String creator;
    // private String remark;

    /**
     * 最高一级的id
     */
    private Long rootId;

    //private List<SysOrganization> childrens;

    @JSONField(serialize = false)
    public Authority getAuthority() {
        Authority authority = new Authority(roles);
        return authority;
    }


    public static List<SysOrganization> transform2List(List<OrganizationForm> organizations) {
        if (CollectionUtils.isEmpty(organizations)) return Lists.newArrayList();

        return organizations.stream().map(x -> {
            SysOrganization sysOrganization = new SysOrganization();
            BeanUtils.copyProperties(x, sysOrganization);
            return sysOrganization;
        }).collect(Collectors.toList());
    }


    public static SysOrganization transform(OrganizationForm organization) {
        SysOrganization sysOrganization = new SysOrganization();
        BeanUtils.copyProperties(organization, sysOrganization);
        return sysOrganization;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelfReference<?> that = (SelfReference<?>) o;
        return Objects.equals(sid, that.sid) &&
                Objects.equals(appType, that.appType);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sid, appType);
    }
}
