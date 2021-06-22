package com.bird.sso.core.auth.jwt;

import com.alibaba.fastjson.JSON;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/21 20:24
 */
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private RequestMatcher requiresAuthenticationRequestMatcher;
    private List<RequestMatcher> permissiveRequestMatchers;
    private AuthenticationManager authenticationManager;
    private AuthenticationFailureHandler failureHandler;
    private AuthenticationSuccessHandler successHandler;

    //private AuthenticationSuccessHandler successHandler = new JWTRefreshSuccessHandler();
    // private AuthenticationFailureHandler failureHandler = new LoginFailureHandler();

    public JWTAuthenticationFilter() {
        this.requiresAuthenticationRequestMatcher = new RequestHeaderRequestMatcher("Authorization");
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(authenticationManager, "authenticationManager must be specified");
        Assert.notNull(successHandler, "AuthenticationSuccessHandler must be specified");
        Assert.notNull(failureHandler, "AuthenticationFailureHandler must be specified");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (permissiveRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if(!requiresAuthentication(request, response)){
            throw SSOException.AUTH_FAIL;
        }


        String token = JWTHelper.getToken(request);
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){
            token = WebUtils.getCookie(request,JWTHelper.HEADER_STRING).getValue();
        }

        JWTAuthenticationToken authToken = new JWTAuthenticationToken(token);
        Authentication authenticate;
        try {
            authenticate = this.getAuthenticationManager().authenticate(authToken);
        } catch (AuthenticationException failed) {
            log.error(failed.getMessage(), failed);
            unsuccessfulAuthentication(request,
                    response, failed);
            return;
        }

        if (null != authenticate) {
            successfulAuthentication(request,
                    response, authenticate);
        }
        filterChain.doFilter(request, response);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        getFailureHandler().onAuthenticationFailure(request, response, failed);
    }

    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }

    protected AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    protected boolean requiresAuthentication(HttpServletRequest request,
                                             HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, JWTHelper.HEADER_STRING);
        if(ObjectUtils.isEmpty(cookie)){
            cookie = WebUtils.getCookie(request, JWTHelper.l_HEADER_STRING);
        }
        log.info(">>>>>>>>>>>>>cookie={}", JSON.toJSONString(cookie));
        return (requiresAuthenticationRequestMatcher.matches(request) &&
                org.apache.commons.lang3.StringUtils.isNotBlank(request.getHeader(JWTHelper.HEADER_STRING)))
                || ObjectUtils.isNotEmpty(cookie) && org.apache.commons.lang3.StringUtils.isNotBlank(cookie.getValue());
    }


    protected boolean permissiveRequest(HttpServletRequest request) {
        if (permissiveRequestMatchers == null)
            return false;
        for (RequestMatcher permissiveMatcher : permissiveRequestMatchers) {
            if (permissiveMatcher.matches(request))
                return true;
        }
        return false;
    }

    public void setPermissiveUrl(String... urls) {
        if (permissiveRequestMatchers == null)
            permissiveRequestMatchers = new ArrayList<>();
        for (String url : urls)
            permissiveRequestMatchers.add(new AntPathRequestMatcher(url));
    }

    public void setAuthenticationSuccessHandler(
            AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(
            AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }

    protected AuthenticationSuccessHandler getSuccessHandler() {
        return successHandler;
    }

    protected AuthenticationFailureHandler getFailureHandler() {
        return failureHandler;
    }

}
