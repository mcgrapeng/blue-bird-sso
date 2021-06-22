package com.bird.sso.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.bird.sso.api.domain.SSOUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/21 20:00
 */
public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {

    private SSOUser loginUser;

    public UserDetails(SSOUser loginUser) {
        this.loginUser = loginUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("USER");
        List<SimpleGrantedAuthority> authorities = Lists.newArrayList(grantedAuthority);
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return getUser().getUserPass();
    }

    @Override
    public String getUsername() {
        return getUser().getUserName();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return Boolean.TRUE;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return Boolean.TRUE;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE;
    }

    public SSOUser getUser() {
        return loginUser;
    }
}
