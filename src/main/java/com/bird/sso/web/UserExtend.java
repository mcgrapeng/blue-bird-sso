package com.bird.sso.web;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/1/20 14:47
 */
@Getter
@Setter
public class UserExtend {

    private Long orgId;

    private String orgName;

    private Long parentOrgId;

    private String parentOrgName;

    private String nickName;

    private String userEmail;

    private String userMobi;

    private String realName;

    private String headImg;
}
