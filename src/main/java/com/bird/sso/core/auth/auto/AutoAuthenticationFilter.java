package com.bird.sso.core.auth.auto;

import com.alibaba.fastjson.JSONObject;
import com.bird.sso.core.auth.SSOAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author 张朋
 * @version 1.0
 * @desc  登录即注册
 * @date 2020/5/11 12:09
 */
@Slf4j
public class AutoAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "username";
    public static final String SPRING_SECURITY_FORM_VERCODE_KEY = "vercode";
    public static final String SPRING_SECURITY_FORM_APPTYPE_KEY = "appType";

    private String mobileParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
    private String vercodeParameter = SPRING_SECURITY_FORM_VERCODE_KEY;
    private String appTypeParameter = SPRING_SECURITY_FORM_APPTYPE_KEY;

    /**
     * 是否仅 POST 方式
     */
    private boolean postOnly = Boolean.TRUE;

    public AutoAuthenticationFilter() {
        // 短信登录的请求 post 方式的 /sms/login
        super(new AntPathRequestMatcher("/sso/auto/sms/login", "POST"));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new SSOAuthenticationException(
                    "Authentication method not supported: " + request.getMethod());
        }

        String body = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
        JSONObject loginUser = JSONObject.parseObject(body);

        String mobile = loginUser.getString(mobileParameter);
        String code = loginUser.getString(vercodeParameter);
        String appType = loginUser.getString(appTypeParameter);

        if (StringUtils.isEmpty(mobile)
                || StringUtils.isEmpty(code)
                || StringUtils.isEmpty(appType)) {
            log.error("##############mobile or code or appType is empty~#################");
            throw SSOAuthenticationException.AUTH_FAIL;
        }

        AutoAuthenticationToken authRequest = new AutoAuthenticationToken(mobile, code, appType);

        // setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected String obtainMobile(HttpServletRequest request) {
        return request.getParameter(mobileParameter);
    }

    protected void setDetails(HttpServletRequest request, AutoAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

}
