package com.bird.sso.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 13:57
 */
public class LogoutHandler implements org.springframework.security.web.authentication.logout.LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        clearToken(authentication);
    }

    protected void clearToken(Authentication authentication) {
        UserDetails user = (UserDetails)authentication.getPrincipal();
        //修改密钥
        SecurityContextHolder.clearContext();
    }
}
