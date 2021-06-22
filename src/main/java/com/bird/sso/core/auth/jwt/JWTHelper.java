package com.bird.sso.core.auth.jwt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.api.domain.SSORole;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.core.UserDetails;
import com.bird.sso.core.auth.SSOAuthenticationException;
import com.bird.sso.domain.SysUser;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.utils.WebUtils;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/22 9:19
 */
@Slf4j
public class JWTHelper {

    private static final Long EXPIRATION_TIME = 86400L * 15; //15天
    //private static final Long EXPIRATION_TIME =  15L;
    public final static String TOKEN_PREFIX = "Bearer ";
    public final static String HEADER_STRING = "Authorization";
    public final static String l_HEADER_STRING = "authorization";
    public final static String l_REQUEST_ID = "bird-request-id";


    public static final String CLAIM_KEY_LOGIN_SOURCE = "source";
    private static final String CLAIM_KEY_APP = "app";
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_REALNAME = "name";
    public static final String CLAIM_KEY_ID = "id";
    public static final String CLAIM_KEY_ORG_ID = "orgId";
    public static final String CLAIM_KEY_ORG_NAME = "orgName";
    public static final String CLAIM_KEY_HEAD_IMG = "headImg";
    public static final String CLAIM_KEY_STATUS = "status";
    public static final String CLAIM_KEY_ORG_PARENT_NAME = "parentOrgName";
    public static final String CLAIM_KEY_CREATED = "created";
    public static final String CLAIM_KEY_EXP = "exp";
    public static final String CLAIM_VERSION = "version";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_MENU = "menu";


    private static final String SECRET = "bird";


    public static Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage(), e);
            throw SSOAuthenticationException.LOGIN_EXPIRE;
        } catch (UnsupportedJwtException
                | MalformedJwtException | SignatureException
                | IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            throw SSOAuthenticationException.AUTH_FAIL;
        }
    }

    public static String generateToken(SSOUser loginUser, String loginSource) {
        Map<String, Object> claims = Maps.newHashMap();
        claims.put(CLAIM_KEY_APP, loginUser.getAppType());
        claims.put(CLAIM_KEY_USERNAME, loginUser.getUserName());
        claims.put(CLAIM_KEY_REALNAME, loginUser.getRealName());
        claims.put(CLAIM_KEY_LOGIN_SOURCE, loginSource);
        Date date = new Date();
        Date exp = new Date(date.getTime() + EXPIRATION_TIME * 1000);
        claims.put(CLAIM_KEY_CREATED, date);
        claims.put(CLAIM_KEY_EXP, exp);
        claims.put(CLAIM_KEY_ID, loginUser.getUserId());
        claims.put(CLAIM_KEY_STATUS, loginUser.getStatus());
        claims.put(CLAIM_KEY_ORG_ID, loginUser.getOrgId());
        claims.put(CLAIM_KEY_ORG_NAME, loginUser.getOrgName());
        claims.put(CLAIM_KEY_ORG_PARENT_NAME, loginUser.getParentOrgName());
        claims.put(CLAIM_KEY_HEAD_IMG, loginUser.getHeadImg());
        claims.put(CLAIM_VERSION, SnowflakeIdWorker.build(7).nextId());
        claims.put(CLAIM_ROLE, loginUser.getRoles());
        claims.put(CLAIM_MENU, loginUser.getMenus());

        return Jwts.builder()
                .setSubject(loginUser.getUserName())
                .setClaims(claims)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }


    public static String generateToken(UserDetails loginUser, String loginSource) {
        Map<String, Object> claims = Maps.newHashMap();
        claims.put(CLAIM_KEY_LOGIN_SOURCE, loginSource);
        claims.put(CLAIM_KEY_APP, loginUser.getUser().getAppType());
        claims.put(CLAIM_KEY_USERNAME, loginUser.getUser().getUserName());
        claims.put(CLAIM_KEY_REALNAME, loginUser.getUser().getRealName());
        Date date = new Date();
        Date exp = new Date(date.getTime() + EXPIRATION_TIME * 1000);
        claims.put(CLAIM_KEY_CREATED, date);
        claims.put(CLAIM_KEY_EXP, exp);
        claims.put(CLAIM_KEY_ID, loginUser.getUser().getUserId());
        claims.put(CLAIM_KEY_STATUS, loginUser.getUser().getStatus());
        claims.put(CLAIM_KEY_ORG_ID, loginUser.getUser().getOrgId());
        claims.put(CLAIM_KEY_ORG_NAME, loginUser.getUser().getOrgName());
        claims.put(CLAIM_KEY_ORG_PARENT_NAME, loginUser.getUser().getParentOrgName());
        claims.put(CLAIM_KEY_HEAD_IMG, loginUser.getUser().getHeadImg());
        claims.put(CLAIM_VERSION, SnowflakeIdWorker.build(4).nextId());
        claims.put(CLAIM_ROLE, loginUser.getUser().getRoles());
        claims.put(CLAIM_MENU, loginUser.getUser().getMenus());

        return Jwts.builder()
                .setSubject(loginUser.getUser().getUserName())
                .setClaims(claims)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }


    public static String generateWxToken(SysUser loginUser, String loginSource) {
        Map<String, Object> claims = Maps.newHashMap();
        claims.put(CLAIM_KEY_LOGIN_SOURCE, loginSource);
        claims.put(CLAIM_KEY_APP, loginUser.getAppType());
        claims.put(CLAIM_KEY_USERNAME, loginUser.getWxOpenId());
        claims.put(CLAIM_KEY_REALNAME, loginUser.getNickName());
        claims.put(CLAIM_KEY_ORG_ID, loginUser.getOrgId());
        Date date = new Date();
        Date exp = new Date(date.getTime() + EXPIRATION_TIME * 1000);
        claims.put(CLAIM_KEY_CREATED, date);
        claims.put(CLAIM_KEY_EXP, exp);
        claims.put(CLAIM_KEY_ID, loginUser.getUserId());
        return Jwts.builder()
                .setSubject(loginUser.getWxOpenId())
                .setClaims(claims)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }


    /**
     * 获取用户ID
     *
     * @param token
     * @return
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object o = claims.get(CLAIM_KEY_ID);
        Long userId;
        if (o instanceof Integer) {
            userId = ((Integer) o).longValue();
        } else {
            userId = (Long) o;
        }
        return userId;
    }


    public static Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        return expiration;
    }


    public static String getLoginSource(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_LOGIN_SOURCE);
    }


    public static String getToken(HttpServletRequest request) {
        String jwt = request.getHeader(HEADER_STRING);
        if (StringUtils.isBlank(jwt)) {
            Cookie cookie = WebUtils.getCookie(request, JWTHelper.HEADER_STRING);
            if (ObjectUtils.isEmpty(cookie)) {
                cookie = WebUtils.getCookie(request, JWTHelper.l_HEADER_STRING);
                jwt = cookie.getValue();
            }
        }

        if (StringUtils.isBlank(jwt)) {
            throw SSOAuthenticationException.AUTH_FAIL;
        }
        return StringUtils.removeStart(jwt, TOKEN_PREFIX);
    }


    public static String getLoginSource() {
        String token = getToken(WebUtils.getRequest());
        String loginSource = getLoginSource(token);
        return loginSource;
    }

    /**
     * 获取token
     *
     * @return
     */
    public static String getAuthorization() {
        HttpServletRequest request = WebUtils.getRequest();
        String jwt = request.getHeader(HEADER_STRING);
        String authInfo = StringUtils.isBlank(jwt)
                ? request.getHeader(l_HEADER_STRING) : jwt;

        if (StringUtils.isBlank(authInfo)) {
            Cookie cookie = WebUtils.getCookie(request, JWTHelper.HEADER_STRING);
            authInfo = cookie.getValue();
        }
        return StringUtils.removeStart(authInfo, TOKEN_PREFIX);
    }

    /**
     * 获取状态
     *
     * @param token
     * @return
     */
    public static String getStatus(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_STATUS);
    }


    public static String getMenus(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_MENU);
    }


    public static String getRoles(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_ROLE);
    }


    /**
     * 获取组织
     *
     * @param token
     * @return
     */
    public static String getOrgName(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_ORG_NAME);
    }

    public static String getParentOrgName(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_ORG_PARENT_NAME);
    }


    /**
     * 获取头像
     *
     * @param token
     * @return
     */
    public static String getHeadImg(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_HEAD_IMG);
    }


    public static String getRealName(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_REALNAME);
    }


    public static Long getOrgId(String token) {
        Claims claims = parseToken(token);
        Object o = claims.get(CLAIM_KEY_ORG_ID);
        Long orgId;
        if (o instanceof Integer) {
            orgId = ((Integer) o).longValue();
        } else {
            orgId = (Long) o;
        }
        return orgId;
    }


    /**
     * 获取用户名
     *
     * @param token
     * @return
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }


    public static String getAppType(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get(CLAIM_KEY_APP);
    }


    public static List<String> buildRoleCodes(String token) {
        String roles = getRoles(token);
        JSONArray array = JSONArray.parseArray(roles);
        if (CollectionUtils.isEmpty(array) || array.size() == 0) {
            return Lists.newArrayList();
        }
        return array.stream().map((t) -> {
            return ((JSONObject) t).getString("code");
        }).collect(Collectors.toList());
    }


    /**
     * 拥有的角色
     *
     * @param token
     * @return
     */
    public static List<SSORole> hasRole(String token) {
        String roles = getRoles(token);
        return JSONArray.parseArray(roles, SSORole.class);
    }


    /**
     * 是否过期
     *
     * @param token
     * @return
     */
    public static boolean isExpiration(String token) {
        return getExpirationDate(token).before(new Date());
    }


    public static Boolean validateToken(String token, String username) {
        return (username.equals(getUsername(token)) && !isExpiration(token));
    }


    private static String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }
}
