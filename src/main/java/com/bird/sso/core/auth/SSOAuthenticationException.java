package com.bird.sso.core.auth;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/9/16 16:00
 */
@Getter
public class SSOAuthenticationException extends AuthenticationException {

    public static final SSOAuthenticationException NO_LOGIN = new SSOAuthenticationException(
            5005, "尚未登录");
    public static final SSOAuthenticationException LOGIN_EXPIRE = new SSOAuthenticationException(
            5002, "登录过期,请重新登录~");
    public static final SSOAuthenticationException AUTH_FAIL = new SSOAuthenticationException(
            401, "用户凭据无效");
    public static final SSOAuthenticationException USER_NO_EXITS = new SSOAuthenticationException(
            5006, "用户尚未注册");
    public static final SSOAuthenticationException USER_LOCKED = new SSOAuthenticationException(
            5007, "用户已锁定");
    public static final SSOAuthenticationException USER_LOGIN_DIFF = new SSOAuthenticationException(
            5009, "当前账号已在其它设备登录，请重新登录~");
    public static final SSOAuthenticationException USER_DUPLICATE = new SSOAuthenticationException(
            5010, "用户数据重复！");
    public static final SSOAuthenticationException USER_PASS_ERR = new SSOAuthenticationException(
            50017, "账号或密码输入错误！");
    public static final SSOAuthenticationException USER_VER_ERR = new SSOAuthenticationException(
            50018, "验证码输入有误，请重新输入~");
    public static final SSOAuthenticationException USER_USERNAME_VER_ERR = new SSOAuthenticationException(
            50019, "账号或验证码输入错误！");
    public SSOAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }

    public SSOAuthenticationException(String msg) {
        super(msg);
    }

    /**
     * 异常信息
     */
    private String msg;

    /**
     * 具体异常码
     */
    private int code;


    public SSOAuthenticationException(int code, String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
        this.code = code;
        this.msg = String.format(msgFormat, args);
    }
}
