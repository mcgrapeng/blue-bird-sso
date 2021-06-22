package com.bird.sso.core.auth.basic;

import com.bird.sso.core.auth.LoginFailureHandler;
import com.bird.sso.core.auth.LoginSuccessHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/21 20:17
 */
public class LoginConfigurer <T extends LoginConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {

    private UsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

    public LoginConfigurer() {
        this.usernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter();
    }
    @Override
    public void configure(B http) throws Exception {
        //设置Filter使用的AuthenticationManager,这里取公共的即可
        usernamePasswordAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        usernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(new LoginFailureHandler());
        //不将认证后的context放入session  基于token，所以不需要session
        usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());

        UsernamePasswordAuthenticationFilter filter = postProcess(usernamePasswordAuthenticationFilter);
        //指定Filter的位置
        http.addFilterAfter(filter, LogoutFilter.class);
    }


    //设置成功的Handler，这个handler定义成Bean，所以从外面set进来
    public LoginConfigurer<T,B> loginSuccessHandler(LoginSuccessHandler authSuccessHandler){
        usernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(authSuccessHandler);
        return this;
    }
}
