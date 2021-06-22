package com.bird.sso.web.controller.manage.organize;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/19 19:43
 */
@Getter
@Setter
public class UserOrganize implements Serializable {

    @NotNull(message = "唯一标识不能为空", groups = {Upd.class})
    private Long sid;

    @NotBlank(message = "名称不能为空", groups = {Add.class, Upd.class})
    private String name;


    private Long pid;

    private String parentName;

    private List<OrganizationForm> childrens;


    public interface Add {
    }

    public interface Upd {
    }

}
