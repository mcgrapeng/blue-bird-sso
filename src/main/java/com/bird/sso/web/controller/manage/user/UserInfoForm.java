package com.bird.sso.web.controller.manage.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author 张朋
 * @version 1.0
 * @desc 用户基本信息（全）
 * @date 2020/5/18 15:04
 */
@Getter
@Setter
public class UserInfoForm {

    @NotNull(message = "账号标识不能为空", groups = {UserInfoUpd.class})
    private Long userId;

    @NotBlank(message = "账号信息不能为空", groups = {UserInfoAdd.class})
    private String username;

    @NotBlank(message = "密码不能为空", groups = {UserInfoAdd.class})
    private String password;

    @NotBlank(message = "应用类型不能为空", groups = {UserInfoAdd.class, UserInfoUpd.class})
    private String appType;


    private String userType;

    /**
     * 所属组织ID
     */
    //@NotBlank(message = "所属组织不能为空", groups = {UserInfoAdd.class})
    @NotNull(message = "所属组织不能为空", groups = {UserInfoUpd.class})
    private Long orgId;

    /**
     * 所属组织
     */
    @NotBlank(message = "所属组织不能为空", groups = {UserInfoAdd.class})
    private String orgName;


    private Long parentOrgId;

    private String parentOrgName;

    /**
     * 用户昵称
     */
    private String nickName;


    /**
     * 用户角色
     */
    private String roles;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空", groups = {UserInfoAdd.class})
    private String realName;

    /**
     * 用户头像
     */
    private String headImg;


    public interface UserInfoAdd {
    }

    public interface UserInfoUpd {
    }
}
