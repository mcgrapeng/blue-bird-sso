package com.bird.sso.web.controller.manage.role;

import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.enums.RoleEnum;
import com.bird.sso.api.enums.RoleTypeEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/2/19 15:18
 */
@Getter
@Setter
public class UserRoleForm {

    @NotNull(message = "唯一标识不能为空", groups = {RoleUpd.class})
    private Long sid;

    @NotBlank(message = "请输入角色名称", groups = {RoleAdd.class, RoleUpd.class})
    private String name;

    @NotBlank(message = "请输入代码", groups = {RoleAdd.class})
    private String code;

    private String isAvailable;

    private Long pid = 0L;

    @NotBlank(message = "请选择应用类型", groups = {RoleAdd.class, RoleUpd.class})
    private String appType;

    /**
     * 角色分类（系统、超级、普通）
     */
    private String type = RoleTypeEnum.NORMAL.name();

    /**
     * 是否为默认角色
     */
    private String isDefault = PublicEnum.N.name();


    public interface RoleAdd {
    }

    public interface RoleUpd {
    }
}
