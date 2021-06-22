package com.bird.sso.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/11/24 17:44
 */
@Configuration
public class SecurityContextConfig {

//    @PostConstruct
//    public void init() {
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
//    }


//    @Bean
//    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
//        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
//        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
//        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
//        methodInvokingFactoryBean.setArguments(new String[]{SecurityContextHolder.MODE_INHERITABLETHREADLOCAL});
//        return methodInvokingFactoryBean;
//    }
}
