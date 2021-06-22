package com.bird.sso.core.auth.jwt;

import com.bird.sso.core.auth.LoginFailureHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 14:04
 */
public class JWTAuthenticationConfigurer<T extends JWTAuthenticationConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {

    private JWTAuthenticationFilter authFilter;

    public JWTAuthenticationConfigurer() {
        this.authFilter = new JWTAuthenticationFilter();
    }

    @Override
    public void configure(B http) {
        authFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        authFilter.setAuthenticationFailureHandler(new LoginFailureHandler());

        JWTAuthenticationFilter filter = postProcess(authFilter);
        http.addFilterBefore(filter, LogoutFilter.class);
    }

    //设置匿名用户可访问url
    public JWTAuthenticationConfigurer<T, B> permissiveRequestUrls(String ... urls){
        authFilter.setPermissiveUrl(urls);
        return this;
    }

    public JWTAuthenticationConfigurer<T, B> jwtValidSuccessHandler(AuthenticationSuccessHandler successHandler){
        authFilter.setAuthenticationSuccessHandler(successHandler);
        return this;
    }

}
