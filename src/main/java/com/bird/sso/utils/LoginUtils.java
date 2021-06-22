package com.bird.sso.utils;

import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.conts.Constants;
import com.bird.sso.core.auth.jwt.JWTHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/6/1 15:21
 */
@Component
@Slf4j
public class LoginUtils {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 重建token
     *
     * @param ssoUser
     */
    public void onAuthentication(SSOUser ssoUser) {
        log.info(">>>>>>>>>>>>>>>>>>>.GET INTO onAuthentication. token重建成功 >>>>>>>>>>>>>>");
        String loginSource = WebUtils.getLoginSource();
        String jwt = JWTHelper.generateToken(ssoUser, loginSource);

        redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.ACCESS_TOKEN, loginSource, ssoUser.getAppType(),ssoUser.getUserId())
                , jwt, 15, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.ACCESS_APP_TYPE_TOKEN, ssoUser.getAppType()), ssoUser.getAppType());

        // 这里创建的token只是单纯的token
        // 按照jwt的规定，最后请求的时候应该是 `Bearer token`
        HttpServletResponse httpServletResponse = WebUtils.getResponse();
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        httpServletResponse.setHeader("Authorization", jwt);

        String ctx = WebUtils.getRequest().getContextPath();
        ResponseCookie cookie = ResponseCookie.from(JWTHelper.HEADER_STRING, jwt) // key & value
                .httpOnly(Boolean.TRUE)        // 禁止js读取
                .secure(Boolean.FALSE)        // 在http下传输
                .path(StringUtils.isBlank(ctx) ? "/" : ctx)            // path
                .maxAge(Duration.ofDays(15))    // 1个小时候过期
                // .sameSite("None")    // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build();

        // 设置Cookie Header
        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    /**
     * 登出
     */
    public void logout() {
        SSOUser ssoUser = WebUtils.getSSOUser();
        String loginSource = WebUtils.getLoginSource();
        redisTemplate.delete(String.format(Constants.RedisPrefix.ACCESS_TOKEN, loginSource
                , ssoUser.getAppType(),ssoUser.getUserId()));
    }


    /**
     * 登出
     */
    public void logout(String appType, String loginSource, long userId) {
        redisTemplate.delete(String.format(Constants.RedisPrefix.ACCESS_TOKEN, loginSource
                ,appType, userId));
    }
}
