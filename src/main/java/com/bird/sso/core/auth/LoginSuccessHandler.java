package com.bird.sso.core.auth;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.conts.Constants;
import com.bird.sso.core.UserDetails;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.service.RoleQueryService;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/21 19:57
 */
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {


    private RedisTemplate redisTemplate;

    private RoleQueryService roleQueryService;


    public LoginSuccessHandler(RedisTemplate redisTemplate, RoleQueryService roleQueryService) {
        this.redisTemplate = redisTemplate;
        this.roleQueryService = roleQueryService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        log.info(">>>>>>>>>>>>>>>>>>>.GET INTO onAuthenticationSuccess. 认证成功 >>>>>>>>>>>>>>");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String loginSource = httpServletRequest.getHeader(Constants.LOGIN_SOURCE);

        if (StringUtils.isBlank(loginSource)) {
            throw SSOException.LOGIN_SOURCE;
        }

        log.info("######################loginSource={}", loginSource);
        UserDetails user = (UserDetails) authentication.getPrincipal();

        String appType = user.getUser().getAppType();
        List<Long> roleIds = roleQueryService.findRoleIdsByUserId(appType, user.getUser().getUserId());

        String menus = roleQueryService.findMenuPathByRoleIds(appType, loginSource, roleIds);

        user.getUser().setMenus(menus);
        String jwt = JWTHelper.generateToken(user, loginSource);

        redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.ACCESS_TOKEN, loginSource, user.getUser().getAppType(), user.getUser().getUserId())
                , jwt, 15, TimeUnit.DAYS);

        redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.ACCESS_APP_TYPE_TOKEN, user.getUser().getAppType(), loginSource), user.getUser().getAppType());

        //登录状态
        redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.LOGIN_SUCCESS, user.getUser().getAppType(), user.getUser().getUserId())
                , Boolean.TRUE, 15, TimeUnit.DAYS);

        // 这里创建的token只是单纯的token
        // 按照jwt的规定，最后请求的时候应该是 `Bearer token`
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        httpServletResponse.setHeader("Authorization", jwt);

        String ctx = httpServletRequest.getContextPath();
        ResponseCookie cookie = ResponseCookie.from(JWTHelper.HEADER_STRING, jwt) // key & value
                .httpOnly(Boolean.TRUE)        // 禁止js读取
                .secure(Boolean.FALSE)        // 在http下传输
                .path(StringUtils.isBlank(ctx) ? "/" : ctx)            // path
                .maxAge(Duration.ofDays(15))    // 1个小时候过期
                // .sameSite("None")    // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                .build();

        // 设置Cookie Header
        httpServletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", user.getUser().getUserId());
        httpServletResponse.getWriter().write(JSON.toJSONString(RES.of(ResultEnum.处理成功.code
                , params, ResultEnum.处理成功.name())));  //返回json数据
    }
}
