package com.bird.sso.core.auth.otp;

import com.bird.sso.core.auth.basic.LoginConfigurer;
import com.bird.sso.core.auth.LoginFailureHandler;
import com.bird.sso.core.auth.LoginSuccessHandler;
import com.bird.sso.core.auth.basic.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 13:53
 */
public class SMSLoginConfigurer<T extends LoginConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {

    private SMSAuthenticationFilter smsAuthenticationFilter;
    public SMSLoginConfigurer() {
        this.smsAuthenticationFilter = new SMSAuthenticationFilter();
    }

    @Override
    public void configure(B http) {

        smsAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        smsAuthenticationFilter.setAuthenticationFailureHandler(new LoginFailureHandler());
        //不将认证后的context放入session  基于token，所以不需要session
        smsAuthenticationFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());

        SMSAuthenticationFilter smsfilter = postProcess(smsAuthenticationFilter);
        //指定Filter的位置
        http.addFilterAfter(smsfilter, UsernamePasswordAuthenticationFilter.class);
    }


    //设置成功的Handler，这个handler定义成Bean，所以从外面set进来
    public SMSLoginConfigurer<T,B> loginSuccessHandler(LoginSuccessHandler authSuccessHandler){
        smsAuthenticationFilter.setAuthenticationSuccessHandler(authSuccessHandler);
        return this;
    }
}
