package com.bird.sso.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;


@Slf4j
public final class HttpUtils {


    private final static String CONTENT_TYPE_TEXT_JSON = "application/json";
    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";

    // 设置连接超时时间，单位毫秒。
    private static final int CONNECT_TIMEOUT = 10000;

    // 请求获取数据的超时时间(即响应时间)，单位毫秒。
    private static final int SOCKET_TIMEOUT = 10000;

    /**
     * 发送GET请求
     *
     * @param url
     * @param req
     * @return 字符串
     */
    public static String get(String url, Map<String, Object> req) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            /**
             * 创建HttpClient对象
             */
            client = HttpClients.createDefault();
            /**
             * 创建URIBuilder
             */
            URIBuilder uriBuilder = new URIBuilder(url);

            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            Iterator iterator = req.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                nameValuePairList.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }

            /**
             * 设置参数
             */
            if (CollectionUtils.isNotEmpty(nameValuePairList)) {
                uriBuilder.addParameters(nameValuePairList);
            }
            /**
             * 创建HttpGet
             */
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            /**
             * 设置请求头部编码
             */
            httpGet.setHeader(new BasicHeader("Content-Type"
                    , "application/json; charset=utf-8"));
            /**
             * 设置返回编码
             */
            httpGet.setHeader(new BasicHeader("Accept"
                    , "application/json;charset=utf-8"));
            /**
             * 请求服务
             */
            response = client.execute(httpGet);
            /**
             * 获取响应吗
             */
            int statusCode = response.getStatusLine().getStatusCode();

            if (ResultEnum.处理成功.code == statusCode) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");
                return result;
            } else {
                log.error("HttpClientService-statusCode: {}", statusCode);
            }
        } catch (Exception e) {
            log.error("HttpClientService-Exception: {}", e);
        } finally {
            try {
                response.close();
                client.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 发送POST请求
     *
     * @param url
     * @param req
     * @return 字符串
     */
    public static String post(String url, Map<String, Object> req) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try {
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);

            post.setConfig(RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build());

            packageJsonParam(req, post);

            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (ResultEnum.处理成功.code == statusCode) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                return result;
            } else {
                log.error("HttpClientService-statusCode：{}", statusCode);
            }
        } catch (Exception e) {
            log.error("HttpClientService-Exception：{}", e);
        } finally {
            try {
                response.close();
                client.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }


    public static String httpsGet(String url, Map<String, Object> req) {

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try {
            PoolingHttpClientConnectionManager connManager = connectionManagerBuilder();
            client = HttpClients.custom().setConnectionManager(connManager).build();

            URIBuilder uriBuilder = new URIBuilder(url);
            List<NameValuePair> nameValuePairList = Lists.newArrayList();
            if(MapUtils.isNotEmpty(req)){
                Iterator iterator = req.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> elem = (Map.Entry<String, String>) iterator.next();
                    nameValuePairList.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
                }
                if (CollectionUtils.isNotEmpty(nameValuePairList)) {
                    uriBuilder.addParameters(nameValuePairList);
                }
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader(new BasicHeader("Content-Type", "application/json; charset=utf-8"));
            httpGet.setHeader(new BasicHeader("Accept", "application/json;charset=utf-8"));
            //设置参数
            httpGet.setConfig(RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build());

            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();

            if (ResultEnum.处理成功.code == statusCode) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");
                return result;
            } else {
                log.error("HttpClientService-errorCode: {}, errorMsg{}", statusCode, "GET请求失败！");
            }
        } catch (Exception e) {
            log.error("HttpClientService-Exception: {}", e);
        } finally {
            try {
                response.close();
                client.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }


    public static String httpsPost(String url, Map<String, Object> req) {
        return httpsPost(url, null, req);
    }

    public static String httpsPost(String url, Map<String, Object> header, Map<String, Object> req) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            PoolingHttpClientConnectionManager connManager = connectionManagerBuilder();
            client = HttpClients.custom().setConnectionManager(connManager).build();
            HttpPost post = new HttpPost(url);
            post.setConfig(RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build());

            packageJsonParam(req, post);

            packageHeader(header, post);

            response = client.execute(post);

            if(ObjectUtils.isEmpty(response)){
                log.error("#########################守望接口超时，url={}，header={}，req={}"
                ,url,JSONObject.toJSONString(header),JSONObject.toJSONString(req));
                return null;
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (ResultEnum.处理成功.code == statusCode) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                return result;
            } else {
                log.error("HttpClientService-statusCode：{}", statusCode);
            }
        } catch (Exception e) {
            log.error("HttpClientService-Exception：{}", e);
        } finally {
            try {
                response.close();
                client.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }


    /**
     * Description: 封装请求头
     *
     * @param header
     * @param httpMethod
     */
    private static void packageHeader(Map<String, Object> header, HttpRequestBase httpMethod) {
        // 封装请求头
        if (MapUtils.isNotEmpty(header)) {
            Set<Map.Entry<String, Object>> entrySet = header.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                httpMethod.setHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }


    /**
     * Description: 封装json请求参数
     *
     * @param params
     * @param httpMethod
     * @throws UnsupportedEncodingException
     */
    public static void packageJsonParam(Map<String, Object> params, HttpEntityEnclosingRequestBase httpMethod)
            throws UnsupportedEncodingException {
        // 封装请求参数
        if (MapUtils.isNotEmpty(params)) {
            JSONObject jsonParam = new JSONObject();
            Set<Map.Entry<String, Object>> entrySet = params.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                jsonParam.put(entry.getKey(), entry.getValue());
            }
            StringEntity entity = new StringEntity(jsonParam.toString(), ENCODING);
            entity.setContentEncoding(ENCODING);
            entity.setContentType(CONTENT_TYPE_TEXT_JSON);
            // 设置到请求的http对象中
            httpMethod.setEntity(entity);
        }
    }


    public static PoolingHttpClientConnectionManager connectionManagerBuilder() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[]{trustManager}, null);

        // 设置协议http和https对应的处理socket链接工厂的对象
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        return connManager;
    }


    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[]{trustManager}, null);
        return sslContext;
    }

    public static void main(String[] args) {
//        Map<String, Object> req = Maps.newHashMap();
//        req.put("blockCode", "1");
//        req.put("page", 1);
//        req.put("updateTime", "2020-05-27 15:18:23.000");
//        Map<String, Object> header = Maps.newHashMap();
//        header.put(HJ_TIMESTAMP_KEY, System.currentTimeMillis());
//        header.put(HJ_REQUEST_ID_KEY, UUID.randomUUID().toString().replaceAll("-", ""));
//        header.put(HJ_ACCESS_ID_KEY, ACCESS_ID);
//
//        req.putAll(header);
//        header.put(HJ_TOKEN, RequestTokenUtils.token(req));
//        String url = "https://openapi.lookdoor.cn:8086/openapi/v2/sync/facePicture.json";
//        String ret = HttpUtils.httpsPost(url, header, req);
    }


}