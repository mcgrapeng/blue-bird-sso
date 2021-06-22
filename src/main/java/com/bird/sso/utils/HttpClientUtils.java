package com.bird.sso.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/8/18 15:59
 */
public final class HttpClientUtils {

    /** 链接超时时间 **/
    private static final Integer CONNECT_TIME_OUT = 40 * 1000;

    /** 数据传输超时时间 **/
    private static final Integer SOCKET_TIME_OUT = 40 * 1000;


    public static HttpClient getHttpClient(){
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setConnectTimeout(CONNECT_TIME_OUT);
        configBuilder.setSocketTimeout(SOCKET_TIME_OUT);

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.setDefaultRequestConfig(configBuilder.build());
        HttpClient httpClient = clientBuilder.build();

        return httpClient;
    }
}
