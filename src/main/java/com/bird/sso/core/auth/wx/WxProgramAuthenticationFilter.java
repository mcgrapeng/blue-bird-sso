//package com.bird.sso.core.auth.wx;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
///**
// * @author 张朋
// * @version 1.0
// * @desc
// * @date 2020/8/19 12:37
// */
//public class WxProgramAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
//
//    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "code";
//    public static final String SPRING_SECURITY_FORM_VERCODE_KEY = "vercode";
//    public static final String SPRING_SECURITY_FORM_APPTYPE_KEY = "appType";
//
//    private String mobileParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
//    private String vercodeParameter = SPRING_SECURITY_FORM_VERCODE_KEY;
//    private String appTypeParameter = SPRING_SECURITY_FORM_APPTYPE_KEY;
//
//    /**
//     * 是否仅 POST 方式
//     */
//    private boolean postOnly = Boolean.TRUE;
//
//    public WxProgramAuthenticationFilter() {
//        // 短信登录的请求 post 方式的 /sms/login
//        super(new AntPathRequestMatcher("/sso/wx/login", "POST"));
//    }
//
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
//        return null;
//    }
//}
