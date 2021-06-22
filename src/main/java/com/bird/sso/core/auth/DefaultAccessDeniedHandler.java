package com.bird.sso.core.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/23 20:19
 */
@Slf4j
public class DefaultAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        StringBuffer msg = new StringBuffer("请求: ");
        msg.append(httpServletRequest.getRequestURI()).append(" 权限不足，无法访问系统资源.");
        log.info(msg.toString());
      //  ResultUtil.writeJavaScript(httpServletResponse,ErrorCodeEnum.AUTHORITY,msg.toString());


       /* boolean ajaxRequest = HttpUtils.isAjaxRequest(httpServletRequest);
        if (ajaxRequest){
            //如果是ajax请求 则返回403错
            ResultUtil.writeJavaScript(httpServletResponse,ErrorCodeEnum.AUTHORITY,msg.toString());
        }else {
            // 非ajax请求 则跳转到指定的403页面
            //此处省略...................
        }*/
    }
}
