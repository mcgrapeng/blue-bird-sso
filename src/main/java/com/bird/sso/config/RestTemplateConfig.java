package com.bird.sso.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClientHttpRequestFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/10/12 20:24
 */

@Configuration
public class RestTemplateConfig {

    //最好是用不注释的方法，在注入的同时设置连接时间，这种注释的也可以，但是没有设置超时时间
    /*@Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder.build();
    }*/

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate(SpringClientFactory factory) {
        RibbonClientHttpRequestFactory ribbonClientHttpRequestFactory = new RibbonClientHttpRequestFactory(factory);
        return new RestTemplate(ribbonClientHttpRequestFactory);
    }

}
