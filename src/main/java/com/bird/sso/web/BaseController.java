package com.bird.sso.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 14:09
 */
@Slf4j
public class BaseController {

    public HttpServletRequest getRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        return ((ServletRequestAttributes) ra).getRequest();
    }

    public HttpServletResponse getResponse() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        return ((ServletRequestAttributes) ra).getResponse();
    }

    public String getParameter(String paraName) {
        return this.getRequest().getParameter(paraName);
    }


    public String getHeader(String headerName) {
        return this.getRequest().getHeader(headerName);
    }


    public String getIpAddress() {
        // String ip = this.getRequest().getRemoteAddr();
        // return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
        HttpServletRequest request = getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取服务器ip地址 * @return
     */
    public String getServerIpAddress() {
        InetAddress address;
        String serverIpAddress = null;
        try {
            address = InetAddress.getLocalHost(); // 获取的是本地的IP地址
            // PC-20140317PXKX/192.168.0.121
            serverIpAddress = address.getHostAddress();// 192.168.0.121
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }
        return serverIpAddress;
    }

}
