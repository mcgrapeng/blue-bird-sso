package com.bird.sso.web.controller.manage.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/6/2 17:58
 */
@Getter
@Setter
public class UserAssignForm {

    @NotNull(message = "参数不合法,缺失【sid】")
    private Long sid;

    @NotBlank(message = "参数不合法,缺失【appType】")
    private String appType;

    @NotBlank(message = "参数不合法,缺失【code】")
    private String code;

    @NotBlank(message = "参数不合法,缺失【name】")
    private String name;
}
