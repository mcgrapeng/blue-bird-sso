package com.bird.sso.core.auth.jwt;

import com.bird.sso.api.enums.AppEnum;
import com.bird.sso.api.enums.ProfileEnum;
import com.bird.sso.conts.Constants;
import com.bird.sso.core.UserDetails;
import com.bird.sso.core.UserService;
import com.bird.sso.core.auth.SSOAuthenticationException;
import com.bird.sso.utils.WebUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 14:51
 */
@Slf4j
public class JWTAuthenticationProvider implements AuthenticationProvider {

    private UserService userService;

    private RedisTemplate redisTemplate;

    public JWTAuthenticationProvider(UserService userService, RedisTemplate redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String jwt = ((JWTAuthenticationToken) authentication).getToken();
        if (StringUtils.isBlank(jwt))
            throw SSOAuthenticationException.AUTH_FAIL;

        //从缓存获取密钥，解析失败，则已注销
        Claims claims = JWTHelper.parseToken(jwt);
        if (claims == null) {
            log.error(">>>>>>>>>>>>>>>>>..JWT token verify fail...>>>>>>>>>>>>>>>>>>");
            throw SSOAuthenticationException.AUTH_FAIL;
        }

        if (JWTHelper.isExpiration(jwt)) {
            log.error(">>>>>>>>>>>JWT Token expires");
            throw SSOAuthenticationException.LOGIN_EXPIRE;
        }

        Authentication authRequest = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails;
        if (null != authRequest) {
            userDetails = (UserDetails) authRequest.getPrincipal();
        } else {
            userDetails = userService.loadUserByUserId(JWTHelper.getAppType(jwt), JWTHelper.getUserId(jwt));
        }

        String profile = WebUtils.getHeader(Constants.LOGIN_PROFILE);

        if (StringUtils.isNotBlank(profile)
                && profile.equalsIgnoreCase(ProfileEnum.PROD.name())) {
            String oldJwt = (String) redisTemplate.opsForValue().get(String.format(Constants.RedisPrefix.ACCESS_TOKEN
                    , JWTHelper.getLoginSource(jwt),userDetails.getUser().getAppType(),userDetails.getUser().getUserId()));

            if(StringUtils.isBlank(oldJwt)){
                throw SSOAuthenticationException.LOGIN_EXPIRE;
            }

            if (!oldJwt.equals(jwt)) {
                throw SSOAuthenticationException.USER_LOGIN_DIFF;
            }
        }

        JWTAuthenticationToken token = new JWTAuthenticationToken(jwt, userDetails);
        return token;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(JWTAuthenticationToken.class);
    }

}
