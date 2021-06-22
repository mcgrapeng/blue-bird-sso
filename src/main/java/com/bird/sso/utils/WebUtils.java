package com.bird.sso.utils;

import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.core.UserDetails;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/15 9:43
 */
public final class WebUtils {

    public static SSOUser getSSOUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        SSOUser user = principal.getUser();
        if (null == user) {
            throw new SSOException(ResultEnum.尚未登录.code, ResultEnum.尚未登录.name());
        }
        return user;
    }


    /**
     * 获取真实
     *
     * @return
     */
    public static String getRealIpAddress() {
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        } else {
            return request.getRemoteAddr();
        }
    }

    /**
     * 获取当前网络ip
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                ipAddress = inet.getHostAddress();
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    public static HttpServletRequest getRequest() {
        RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) ra).getRequest();
    }

    public static HttpServletResponse getResponse() {
        RequestAttributes ra = RequestContextHolder.currentRequestAttributes();
        return ((ServletRequestAttributes) ra).getResponse();
    }

    public static String getHeader(String headerName) {
        return getRequest().getHeader(headerName);
    }


    /**
     * 根据cookie名称获得cookie
     *
     * @param request
     * @param name    cookie的名称
     * @return
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {
                if (c.getName().equals(name)) {
                    return c;
                }
            }
        }
        return null;
    }


    /**
     * 保存cookie 保存在根目录下
     *
     * @param request
     * @param response
     * @param name     cookie名称
     * @param value    cookie的值
     * @param expiry   过期时间（可以为空）
     * @param domain   域名（可以为空）
     * @return
     */
    public static Cookie addCookie(HttpServletRequest request,
                                   HttpServletResponse response, String name
            , String value,
                                   Integer expiry, String domain) {
        Cookie cookie = new Cookie(name, value);
        if (expiry != null) {
            cookie.setMaxAge(expiry);
        }
        if (StringUtils.isNotBlank(domain)) {
            cookie.setDomain(domain);
        }
        String ctx = request.getContextPath();
        cookie.setPath(StringUtils.isBlank(ctx) ? "/" : ctx);
        cookie.setHttpOnly(Boolean.TRUE);
        response.addCookie(cookie);
        return cookie;
    }


    /**
     * 清除cookie
     *
     * @param request
     * @param response
     * @param name     cookie名称
     * @param domain
     */
    public static void cancleCookie(HttpServletRequest request,
                                    HttpServletResponse response
            , String name, String domain) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        String ctx = request.getContextPath();
        cookie.setPath(StringUtils.isBlank(ctx) ? "/" : ctx);
        if (StringUtils.isNotBlank(domain)) {
            cookie.setDomain(domain);
        }
        response.addCookie(cookie);
    }

    public static String getHeaderAppType() {
        return WebUtils.getHeader("app");
    }


    public static String getLoginSource() {
        return getHeader(JWTHelper.CLAIM_KEY_LOGIN_SOURCE);
    }

}
