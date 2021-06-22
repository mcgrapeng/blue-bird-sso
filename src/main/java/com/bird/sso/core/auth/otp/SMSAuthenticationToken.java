package com.bird.sso.core.auth.otp;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 12:10
 */
public class SMSAuthenticationToken extends AbstractAuthenticationToken {


    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    /**
     * 在 UsernamePasswordAuthenticationToken 中该字段代表登录的用户名，
     * 在这里就代表登录的手机号码
     */
    private final Object principal;

    private  String code;

    private String appType;


    public SMSAuthenticationToken(Object principal){
        super(null);
        this.principal = principal;
        setAuthenticated(false);
    }


    /**
     * 构建一个没有鉴权的 SMSAuthenticationToken
     */
    public SMSAuthenticationToken(Object principal , String code) {
        super(null);
        this.principal = principal;
        this.code = code;
        setAuthenticated(false);
    }


    public SMSAuthenticationToken(Object principal, String code, String appType) {
        super(null);
        this.principal = principal;
        this.code = code;
        this.appType = appType;
    }

    /**
     * 构建拥有鉴权的 SMSAuthenticationToken
     */
    public SMSAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities, String code) {
        super(authorities);
        this.principal = principal;
        this.code = code;
        // must use super, as we override
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public String getCode() {
        return code;
    }

    public String getAppType() {
        return appType;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                    "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}
