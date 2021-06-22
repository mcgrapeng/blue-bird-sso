package com.bird.sso.core.auth;

import com.alibaba.fastjson.JSON;
import com.bird.RES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/21 20:16
 */
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        log.info(">>>>>>>>>>>>>>>>>>>认证失败>>>>>>>>>>>>>>>>>>>>>>");
        SecurityContextHolder.clearContext();
        httpServletResponse.setStatus(HttpStatus.OK.value()); //避免前端提示
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");

        SSOAuthenticationException exception = (SSOAuthenticationException) e;
        httpServletResponse.getWriter().write(JSON.toJSONString(RES.of(exception.getCode()
                , exception.getMsg())));  //返回json数据
    }
}
