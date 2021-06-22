package com.bird.sso.core.auth.jwt;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.bird.RES;
import com.bird.sso.conts.Constants;
import com.bird.sso.core.UserDetails;
import com.bird.sso.core.UserService;
import com.bird.sso.service.RoleQueryService;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 13:46
 */
public class JWTRefreshSuccessHandler implements AuthenticationSuccessHandler {

    private static final int tokenRefreshInterval = 60 * 60 * 24 * 15;  //刷新间隔15天

    private UserService jwtUserService;

    private RedisTemplate redisTemplate;

    private RoleQueryService roleQueryService;


    public JWTRefreshSuccessHandler(UserService jwtUserService, RoleQueryService roleQueryService, RedisTemplate redisTemplate) {
        this.jwtUserService = jwtUserService;
        this.redisTemplate = redisTemplate;
        this.roleQueryService = roleQueryService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String jwt = ((JWTAuthenticationToken) authentication).getToken();
        boolean shouldRefresh = shouldTokenRefresh(JWTHelper.getExpirationDate(jwt));
        if (shouldRefresh) {
            String appType = JWTHelper.getAppType(jwt);
            UserDetails userDetails = jwtUserService.loadUserByUserId(appType
                    , JWTHelper.getUserId(jwt));

            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            String loginSource = WebUtils.getHeader(Constants.LOGIN_SOURCE);

            userDetails.getUser().setMenus(roleQueryService.findMenuPathByRoleIds(appType, loginSource
                    , userDetails.getUser().listRoleIds()));
            String newJwt = JWTHelper.generateToken(userDetails, loginSource);

            redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.ACCESS_TOKEN
                    , loginSource, userDetails.getUser().getAppType(),userDetails.getUser().getUserId())
                    , jwt, 15, TimeUnit.DAYS);

            redisTemplate.opsForValue().set(String.format(Constants.RedisPrefix.ACCESS_APP_TYPE_TOKEN
                    , userDetails.getUser().getAppType(),loginSource), userDetails.getUser().getAppType());

            response.setHeader("Authorization", newJwt);


            String ctx = request.getContextPath();
            ResponseCookie cookie = ResponseCookie.from(JWTHelper.HEADER_STRING, jwt) // key & value
                    .httpOnly(Boolean.TRUE)        // 禁止js读取
                    .secure(Boolean.TRUE)        // 在http下不传输
                    .domain(null)// 域名
                    .path(StringUtils.isBlank(ctx) ? "/" : ctx)            // path
                    .maxAge(Duration.ofDays(15))    // 1个小时候过期
                    .sameSite("None")    // 大多数情况也是不发送第三方 Cookie，但是导航到目标网址的 Get 请求除外
                    .build();

            // 设置Cookie Header
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());


            Map<String, Object> params = Maps.newHashMap();
            params.put("userId", userDetails.getUser().getUserId());

            response.getWriter().write(JSON.toJSONString(RES.of(ResultEnum.处理成功.code
                    , params, ResultEnum.处理成功.name())));  //返回json数据
        }
    }

    protected boolean shouldTokenRefresh(Date issueAt) {
        LocalDateTime issueTime = LocalDateTime.ofInstant(issueAt.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(tokenRefreshInterval).isAfter(issueTime);
    }
}