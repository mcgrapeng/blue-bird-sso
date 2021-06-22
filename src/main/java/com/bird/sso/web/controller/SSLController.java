package com.bird.sso.web.controller;

import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.enums.DomainConifgEnum;
import com.bird.sso.utils.DateUtils;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/12/18 18:32
 */
@Slf4j
@RequestMapping("/sso/ssl")
@Controller
public class SSLController {


    /**
     * 证书过期校验
     *
     * @return
     */
    @RequestMapping(value = {"/open-query/check"}, method = RequestMethod.GET)
    @ResponseBody
    public RES<Boolean> checkSSL(@RequestParam(value = "_app_type", required = false)
                                         String appType) {

        if (StringUtils.isBlank(appType)) {
            throw new SSOException("参数不合法！");
        }

        boolean isExpire = Boolean.TRUE;
        try {
            trustEveryone();
            URL url = new URL(StringUtils.join("https://", DomainConifgEnum.getDomain(appType)));
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            for (Certificate certificate : connection.getServerCertificates()) {
                //第一个就是服务器本身证书，后续的是证书链上的其他证书
                X509Certificate x509Certificate = (X509Certificate) certificate;
                Principal subjectDN = x509Certificate.getSubjectDN();
                Date notBefore = x509Certificate.getNotBefore();//有效期开始时间
                Date notAfter = x509Certificate.getNotAfter();//有效期结束时间
                if (DateUtils.compare(notAfter)) {
                    isExpire = Boolean.FALSE;
                    break;
                }
            }
            connection.disconnect();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return RES.of(ResultEnum.处理成功.code, isExpire, ResultEnum.处理成功.name());
    }


    /**
     * 忽略证书校验
     */
    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
