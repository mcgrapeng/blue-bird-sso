package com.bird.sso.core.auth.basic;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/6 19:07
 */
public class QHUsernamePasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

   private String appType;


    public QHUsernamePasswordAuthenticationToken(String appType,Object principal, Object credentials) {
        super(principal, credentials);
        this.appType = appType;
    }

    public QHUsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public QHUsernamePasswordAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public String getAppType() {
        return appType;
    }
}
