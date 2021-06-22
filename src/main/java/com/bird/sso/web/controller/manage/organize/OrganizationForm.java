package com.bird.sso.web.controller.manage.organize;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/19 19:43
 */
@Getter
@Setter
public class OrganizationForm{

    @NotBlank(message = "请选择应用类型", groups = {Add.class, UpdAvailable.class, Upd.class})
    private String appType;

    @NotNull(message = "唯一标识不能为空", groups = {UpdAvailable.class, Upd.class})
    private Long sid;

    @NotNull(message = "父级标识不能为空", groups = {Add.class, Upd.class})
    private Long pid;

    //@NotBlank(message = "代码不能为空", groups = {Add.class, Upd.class})
    private String code;

    @NotBlank(message = "名称不能为空", groups = {Add.class, Upd.class})
    private String name;

    @NotBlank(message = "父级名称不能为空", groups = {Add.class, Upd.class})
    private String parentName;

    @NotBlank(message = "可用状态不能为空", groups = {UpdAvailable.class})
    @JSONField(serialize = false)
    private String isAvailable;


    private List<OrganizationForm> childrens;


    public interface Add {
    }

    public interface Upd {
    }

    public interface UpdAvailable {
    }

}
