package com.bird.sso.web.wx;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/8/18 10:44
 */
@Getter
@Setter
public class WXLoginAuth {

    @NotBlank(message = "CODE不能为空", groups = {UserLogin.class})
    private String code;

    @NotBlank(message = "应用类型不能为空", groups = {UserLogin.class,UserInfo.class})
    private String appType;


    @NotBlank(message = "IV不能为空", groups = {UserInfo.class})
    private String iv;

    @NotBlank(message = "密文数据不能为空", groups = {UserInfo.class})
    private String encryptedData;

    @NotBlank(message = "token不能为空", groups = {UserInfo.class})
    private String token;


    public interface UserInfo{};

    public interface UserLogin{};

}
