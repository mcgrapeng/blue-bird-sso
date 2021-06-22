package com.bird.sso.config;


import com.bird.common.tools.VerifyCodeService;
import com.bird.sso.core.LogoutHandler;
import com.bird.sso.core.OptionsRequestFilter;
import com.bird.sso.core.UserService;
import com.bird.sso.core.auth.DefaultAccessDeniedHandler;
import com.bird.sso.core.auth.DefaultAuthenticationEntryPoint;
import com.bird.sso.core.auth.LoginSuccessHandler;
import com.bird.sso.core.auth.auto.AutoAuthenticationProvider;
import com.bird.sso.core.auth.auto.AutoLoginConfigurer;
import com.bird.sso.core.auth.basic.LoginConfigurer;
import com.bird.sso.core.auth.basic.QHDaoAuthenticationProvider;
import com.bird.sso.core.auth.jwt.JWTAuthenticationConfigurer;
import com.bird.sso.core.auth.jwt.JWTAuthenticationProvider;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.core.auth.jwt.JWTRefreshSuccessHandler;
import com.bird.sso.core.auth.otp.SMSAuthenticationProvider;
import com.bird.sso.core.auth.otp.SMSLoginConfigurer;
import com.bird.sso.service.RoleQueryService;
import com.bird.sso.service.UserAuthorityService;
import com.bird.sso.thread.ContextAwarePoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Bean(name = "passwordEncoder")
    public PasswordEncoder bCryptPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }


    /*@Bean(name = "md5PasswordEncoder")
    public PasswordEncoder md5PasswordEncoder() {
        return  new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5");
    }*/


    //该bean是springmvc启动的时候实例化的一个对象，纳入到容器中
    //@Autowired
    //private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private VerifyCodeService verifyCodeService;

//    @Autowired
//    private RSAHandler rsaHandler;


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserAuthorityService userAuthorityService;


    @Autowired
    private birdProfileProperties propertySource;

    @Autowired
    private ContextAwarePoolExecutor executor;

    @Autowired
    private RoleQueryService roleQueryService;

    @Override
    public void configure(WebSecurity http){
        //对请求进行认证  url认证配置顺序为：1.先配置放行不需要认证的 permitAll()
        // 2.然后配置 需要特定权限的 hasRole() 3.最后配置 anyRequest().authenticated()
        http.ignoring()
                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/*.txt",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.png",
                        "/**/*.jpeg",
                        "/**/*.jpg"
                ).antMatchers(HttpMethod.OPTIONS);
//                .antMatchers(HttpMethod.OPTIONS) //option 请求默认放行
//                // 对于获取token的rest api要允许匿名访问
//                .antMatchers("/open-api/**", "/open-query/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 开启允许iframe 嵌套
        http.headers().frameOptions().disable();
        //对请求进行认证  url认证配置顺序为：1.先配置放行不需要认证的 permitAll() 2.然后配置 需要特定权限的 hasRole() 3.最后配置 anyRequest().authenticated()
        http.authorizeRequests()
//                .antMatchers(
//                        HttpMethod.GET,
//                        "/",
//                        "/*.txt",
//                        "/*.html",
//                        "/favicon.ico",
//                        "/**/*.html",
//                        "/**/*.css",
//                        "/**/*.js"
//                ).permitAll()
//                .antMatchers(HttpMethod.OPTIONS).permitAll() //option 请求默认放行
                // 对于获取token的rest api要允许匿名访问
                .antMatchers("/sso/auth/**","/sso/register/**","/sso/sms/**","/sso/login","/sso/password-forget"
                        ,"/sso/vercode/**","/sso/user-exist/**","/**/open-query/**","/sso/wx/**","/sso/auto/**").permitAll()
                //匿名（）表达式主要是指用户的状态（登录或不登录）。基本上，直到一个用户被“认证”，它是一个“匿名用户”。这就像每个人都有一个“默认角色”。
                //.antMatchers(HttpMethod.OPTIONS, "/**").anonymous()
                // .antMatchers("/api/**").hasAnyRole("USER")
                // .antMatchers("/manage/**").hasAnyRole("ADMIN")
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
                .and().exceptionHandling()
                // 认证配置当用户请求了一个受保护的资源，但是用户没有通过登录认证，则抛出登录认证异常，MyAuthenticationEntryPointHandler类中commence()就会调用
                //.authenticationEntryPoint(authenticationEntryPoint())
                //用户已经通过了登录认证，在访问一个受保护的资源，但是权限不够，则抛出授权异常，MyAccessDeniedHandler类中handle()就会调用
                //.accessDeniedHandler(accessDeniedHandler())
                .and().csrf().disable()           // 禁用 Spring Security 自带的跨域处理
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .formLogin().disable()
                .cors()//支持跨域
                .and()//添加header设置，支持跨域和ajax请求
                .headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                new Header("Access-control-Allow-Origin", "*"),
                new Header("Access-Control-Allow-Credentials", "true"),
                new Header("Access-Control-Allow-Methods", "OPTIONS,GET,POST,DELETE,PUT"),
                new Header("Access-Control-Expose-Headers", "Authorization",JWTHelper.CLAIM_KEY_LOGIN_SOURCE))))
                .and() //拦截OPTIONS请求，直接返回header
                //加入自定义OptionsRequestFilter替代原有Filter
                .addFilterAfter(new OptionsRequestFilter(), CorsFilter.class)
                .apply(new LoginConfigurer<>()).loginSuccessHandler(loginSuccessHandler())
                .and()
                .apply(new SMSLoginConfigurer<>()).loginSuccessHandler(loginSuccessHandler())
                .and()
                .apply(new AutoLoginConfigurer<>()).loginSuccessHandler(loginSuccessHandler())
                .and()
                .apply(new JWTAuthenticationConfigurer<>()).jwtValidSuccessHandler(jwtRefreshSuccessHandler())
                .permissiveRequestUrls("/logout","/**/sso/auth/**","/**/sso/register/**","/**/sso/sms/**","/**/sso/login","/**/sso/password-forget"
                ,"/**/sso/vercode/**","/**/sso/user-exist/**","/**/open-query/**","/**/sso/wx/**","/**/sso/auto/sms/login/**")
                .and()
                //使用默认的logoutFilter
                .logout()
//		        .logoutUrl("/logout")   //默认就是"/logout"
                .addLogoutHandler(tokenClearLogoutHandler())//logout时清除token
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());//logout成功后返回200

        // 禁用缓存
        http.headers().cacheControl();
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider()).authenticationProvider(jwtAuthenticationProvider())
                .authenticationProvider(smsAuthenticationProvider()).authenticationProvider(autoAuthenticationProvider());
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    /**
     * 注册  认证权限不足处理 bean
     *
     * @return
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new DefaultAccessDeniedHandler();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new DefaultAuthenticationEntryPoint();
    }


    @Bean("jwtAuthenticationProvider")
    protected AuthenticationProvider jwtAuthenticationProvider() {
        return new JWTAuthenticationProvider(userService,redisTemplate);
    }

    @Bean("daoAuthenticationProvider")
    protected AuthenticationProvider daoAuthenticationProvider() {
        //这里会默认使用BCryptPasswordEncoder比对加密后的密码，注意要跟createUser时保持一致
        QHDaoAuthenticationProvider daoProvider = new QHDaoAuthenticationProvider(userService);
        //daoProvider.setUserDetailsService(userService);
        daoProvider.setHideUserNotFoundExceptions(Boolean.FALSE);
        return daoProvider;
    }

    @Bean("smsAuthenticationProvider")
    protected SMSAuthenticationProvider smsAuthenticationProvider() {
        return new SMSAuthenticationProvider(userService, verifyCodeService);
    }


    @Bean("autoAuthenticationProvider")
    protected AutoAuthenticationProvider autoAuthenticationProvider() {
        userService.setPasswordEncoder(bCryptPasswordEncoder());
        return new AutoAuthenticationProvider(userService, verifyCodeService,userAuthorityService,executor);
    }

    @Bean
    protected LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(redisTemplate,roleQueryService);
    }


    @Bean
    protected JWTRefreshSuccessHandler jwtRefreshSuccessHandler() {
        return new JWTRefreshSuccessHandler(userService,roleQueryService,redisTemplate);
    }


    @Bean
    protected LogoutHandler tokenClearLogoutHandler() {
        return new LogoutHandler();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE","OPTION"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);//支持安全证书。跨域携带cookie需要配置这个
        configuration.addExposedHeader("Authorization");
        configuration.addExposedHeader(JWTHelper.CLAIM_KEY_LOGIN_SOURCE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

