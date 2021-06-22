package com.bird.sso.core.auth.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 14:31
 */
public class JWTAuthenticationToken extends AbstractAuthenticationToken {

    private UserDetails principal;
    private String credentials;
    private String token;


    public JWTAuthenticationToken(String token) {
        super(Collections.emptyList());
        this.token = token;
    }

    public JWTAuthenticationToken(String token,UserDetails principal) {
        super(Collections.emptyList());
        this.principal = principal;
        this.token = token;
    }

    public JWTAuthenticationToken(String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
    }

    public JWTAuthenticationToken(UserDetails principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
    }

    @Override
    public void setDetails(Object details) {
        super.setDetails(details);
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getToken() {
        return token;
    }

}