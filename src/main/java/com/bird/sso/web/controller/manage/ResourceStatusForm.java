package com.bird.sso.web.controller.manage;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/6/3 10:59
 */
@Setter
@Getter
public class ResourceStatusForm {
    @NotBlank(message = "业务类型不能为空")
    private String appType;

    @NotNull(message = "资源ID不能为空")
    private Long sid;

    @NotBlank(message = "状态不能为空")
    private String isAvailable;
}
