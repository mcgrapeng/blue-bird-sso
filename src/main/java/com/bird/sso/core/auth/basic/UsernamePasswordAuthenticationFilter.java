package com.bird.sso.core.auth.basic;

import com.alibaba.fastjson.JSONObject;
import com.bird.sso.api.enums.AppEnum;
import com.bird.sso.core.auth.SSOAuthenticationException;
import com.bird.sso.utils.rsa.v2.RSA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author 张朋
 * @version 1.0
 * @desc 认证过滤器
 * @date 2020/4/21 11:45
 */
@Slf4j
public class UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {


    @Override
    public void afterPropertiesSet() {
        Assert.notNull(getAuthenticationManager(), "authenticationManager must be specified");
        Assert.notNull(getSuccessHandler(), "AuthenticationSuccessHandler must be specified");
        Assert.notNull(getFailureHandler(), "AuthenticationFailureHandler must be specified");
    }

    /**
     * 拦截url为 "/login" 的POST请求
     */

    public UsernamePasswordAuthenticationFilter() {
        super(new AntPathRequestMatcher("/sso/login", "POST"));
    }


    /**
     * @return org.springframework.security.core.Authentication
     * @Author 张朋
     * @Description
     * @Date 12:04 2020/4/21
     * @Param [request, response]
     **/
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        String body = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
        JSONObject loginUser = JSONObject.parseObject(body);
        String username = loginUser.getString("username");
        String password = loginUser.getString("password");
        String appType = loginUser.getString("appType");
        if (StringUtils.isEmpty(username)
                || StringUtils.isEmpty(password)
                || StringUtils.isEmpty(appType)) {
            log.error("##############username or password or appType is empty~#################");
            throw SSOAuthenticationException.USER_PASS_ERR;
        }
        String decryptPwd;
        try {

            if (AppEnum.APP_UF.name().equals(appType)) {
                decryptPwd = password;
            }else {
                decryptPwd = RSA.decrypt(password,RSA.getPrivateKey(RSA.PRIVATE_KEY));
            }
        } catch (Exception e) {
            log.error("密码解析错误，请输入正确的密码~", e);
            throw SSOAuthenticationException.USER_PASS_ERR;
        }
        QHUsernamePasswordAuthenticationToken authRequest = new QHUsernamePasswordAuthenticationToken(
                appType, username, decryptPwd);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

}