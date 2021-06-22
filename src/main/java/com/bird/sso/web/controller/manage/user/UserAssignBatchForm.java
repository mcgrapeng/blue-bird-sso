package com.bird.sso.web.controller.manage.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/6/2 17:58
 */
@Getter
@Setter
public class UserAssignBatchForm {

    @Size(min = 1, message = "用户列表不能为空")
    private List<Long> userIds;

    @Size(min = 1, message = "角色列表不能为空")
    private List<UserAssignForm> roles;
}
