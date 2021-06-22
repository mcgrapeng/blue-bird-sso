package com.bird.sso.domain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.bird.sso.core.Authority;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SysUser {

    private Integer id;

    private Long userId;

    private String wxOpenId;

    private String wxUnionId;

    private String userName;

    private String nickName;

    private String userMobi;

    private String userPass;

    private String userEmail;

    private String userType;

    private String appType;

    private String realName;

    private String headImg;

    private String userSource;

    private Long orgId;

    private String orgName;

    private String parentOrgName;

    //拥有的角色
    //平铺存储
    @JSONField(serialize = false)
    private String roles;


    private List<String> roleNames;

    private String isPermitAll;

    private String status;

    private Date lastLoginTime;

    private String isBindMobile;

    private Integer loginTimes;

    private Date updateTime;

    private Date createTime;

    private String creator;

    private String editor;

    private String ext;

    private String remark;


    @JSONField(serialize = false)
    public Authority getAuthority() {
        Authority authority = new Authority(roles);
        return authority;
    }


    public List<String> getRoleNames() {

        if (StringUtils.isNotBlank(roles)) {

            List<SysRole> roles = JSONArray.parseArray(this.roles, SysRole.class);

            List<String> data = roles.stream().map(t -> t.getName())
                    .collect(Collectors.toList());

            this.roleNames = data;
        } else {
            roleNames = Lists.newArrayList();
        }
        return roleNames;
    }
}
