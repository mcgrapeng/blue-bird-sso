package com.bird.sso.core.auth;

import com.alibaba.fastjson.JSON;
import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/24 19:52
 */
public class DefaultAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * 当用户尝试访问需要权限才能的REST资源而不提供Token或者Token过期时，
     * 将调用此方法发送401响应以及错误信息
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
//        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());

        response.getWriter().write(JSON.toJSONString(RES.of(HttpServletResponse.SC_UNAUTHORIZED
                ,SSOException.AUTH_FAIL.getMsg())));  //返回json数据
    }
}