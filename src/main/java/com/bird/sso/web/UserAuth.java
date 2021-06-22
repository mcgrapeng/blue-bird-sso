package com.bird.sso.web;

import com.bird.sso.api.enums.PublicEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/18 15:04
 */
@Getter
@Setter
public class UserAuth {

    @NotBlank(message = "手机号不能为空", groups = {UserReg.class, UserPass.class
            , UserMobile.class, UserForgetPass.class,UserVerForgetPass.class,UserSpecReg.class})
    private String username;
    @NotBlank(message = "原始密码不能为空", groups = {UserReg.class, UserPass.class
            , UserUpdatePass.class, UserForgetPass.class,UserVerForgetPass.class,UserCheckPass.class,UserSpecReg.class})
    private String password;
    @NotBlank(message = "新密码不能为空", groups = {UserUpdatePass.class,UserVerUpdatePass.class})
    private String newPassword;
    @NotBlank(message = "验证码不能为空", groups = {UserReg.class,UserVerForgetPass.class
            ,UserVerUpdatePass.class,UserSpecReg.class,UserMobile.class})
    private String vercode;

    @NotBlank(message = "应用类型不能为空", groups = {UserReg.class, UserPass.class
            , UserForgetPass.class,UserVerForgetPass.class,UserSpecReg.class})
    private String appType;


    private String userType;

    private List<String> roleCode;

    @NotBlank(message = "角色代码不能为空", groups = {UserSpecReg.class})
    public interface UserSpecReg{};

    public interface UserMobile{};

    public interface UserReg{};

    public interface UserPass{};

    public interface UserUpdatePass{};
    public interface UserVerUpdatePass{};

    public interface UserForgetPass{};

    public interface UserCheckPass{};

    public interface UserVerForgetPass{};

}
