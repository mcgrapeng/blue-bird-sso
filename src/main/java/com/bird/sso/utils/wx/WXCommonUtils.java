package com.bird.sso.utils.wx;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/8/18 11:23
 */

import com.alibaba.fastjson.JSONObject;
import com.bird.sso.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.*;

/**
 * 微信工具
 */
@Slf4j
public final class WXCommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(WXCommonUtils.class);

    private static final String HTTP_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";
    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final String HTTP_USER_AGENT = "wxpay sdk java v1.0 ";
    private static final int TIMEOUT = 10000;

    private WXCommonUtils() {

    }

    //appId、timeStamp、nonceStr、package、signType

    /**
     * 获取MD5签名
     *
     * @param paramMap 签名参数（sign不参与签名）
     * @param key      签名密钥
     * @return MD5签名结果
     */
    public final static String md5Sign(Map<String, Object> paramMap, String key) {
        String payParam = getSignTemp(paramMap, key);
        String sign = MD5.encode32ToUpperCase(payParam);
        logger.info("MD5签名结果：{}", sign);
        return sign;
    }

    /**
     * 获取HMAC-SHA256签名
     *
     * @param paramMap 签名参数（sign不参与签名）
     * @param key      签名密钥
     * @return HMAC-SHA256签名结果
     */
    public final static String sha256Sign(Map<String, Object> paramMap, String key) {
        try {
            String payParam = getSignTemp(paramMap, key);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] array = sha256_HMAC.doFinal(payParam.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            String sign = sb.toString().toUpperCase();
            logger.info("HMAC-SHA256签名结果：{}", sign);
            return sign;
        } catch (Exception e) {
            logger.error("HMAC-SHA256签名异常：{}", e);
            return null;
        }
    }

    /**
     * 获取签名参数字符串
     *
     * @param paramMap 签名参数（sign字段不参与签名）
     * @param payKey   签名密钥
     * @return 待签名字符串
     */
    private final static String getSignTemp(Map<String, Object> paramMap, String payKey) {
        ArrayList<String> keyList = new ArrayList<>(paramMap.keySet());
        Collections.sort(keyList);

        StringBuilder signParam = new StringBuilder();
        for (String key : keyList) {
            if (!"sign".equals(key) && null != paramMap.get(key)) {
                signParam.append(key).append("=").append(paramMap.get(key)).append("&");
            }
        }
        signParam.delete(signParam.length() - 1, signParam.length());
        logger.info("签名原文：{}", signParam.toString());

        signParam.append("&key=").append(payKey);
        return signParam.toString();
    }

    /**
     * 生产随机数
     *
     * @return
     */
    public final static String createNonceStr() {
        StringBuilder nonceStr = new StringBuilder();
        Random random = new Random();
        for (int i = 0, lenght = 31; i < lenght; i++) {
            nonceStr.append(random.nextInt(10));
        }
        logger.info("微信服务商随机字符串:[{}]", nonceStr);
        return nonceStr.toString();
    }

    /**
     * Map转Xml
     *
     * @param paramMap 待转换参数
     * @return
     */
    public final static String mapToXml(final Map<String, Object> paramMap) {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<xml>");
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            xmlBuilder.append("<").append(entry.getKey()).append(">").append(entry.getValue()).append("</").append(entry.getKey()).append(">");
        }
        xmlBuilder.append("</xml>");
        logger.info("微信服务商--Map转XML结果:{}", xmlBuilder.toString());
        return xmlBuilder.toString();
    }

    /**
     * Xml转Map
     *
     * @param resultStr 带转换字符串
     * @return
     */
    public final static Map<String, Object> xmlToMap(final String resultStr) {
        if (resultStr == null || StringUtils.isBlank(resultStr)) {
            logger.error("微信服务商--待解析的XML报文为空！");
            return null;
        }
        try {
            Map<String, Object> resultMap = new HashMap<>();
            Document doc = DocumentHelper.parseText(resultStr);
            List<Element> list = doc.getRootElement().elements();
            for (Element element : list) {
                resultMap.put(element.getName(), element.getText());
            }
            return resultMap;
        } catch (DocumentException e) {
            logger.error("微信服务商--解析XML失败！{}", e);
            return null;
        }
    }

    /**
     * post请求（带证书）
     *
     * @param mchId       (商户号)证书的key
     * @param keyStoreUrl 证书的路径
     * @param data        发送的数据
     * @param requestUrl  发送的路径
     * @return 请求结果
     */
    public final static String requestPostSSL(final String mchId, final String keyStoreUrl, final String data, final String requestUrl) {
        logger.info("官方微信--请求商户号:{},证书路径:{},请求地址:{},请求参数:{}", mchId, keyStoreUrl, requestUrl, data);
        FileInputStream instream = null;
        SSLContext sslcontext = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            instream = new FileInputStream(new File(keyStoreUrl));
            keyStore.load(instream, mchId.toCharArray());// 这里写密码..默认是你的MCHID
            sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mchId.toCharArray()).build();
        } catch (Exception e) {
            logger.error("官方微信--证书加载失败!{}", e);
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                logger.error("官方微信--证书加载失败!{}", e);
            }
        }
        @SuppressWarnings("deprecation")
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        try {
            HttpPost httpPost = new HttpPost(requestUrl);
            RequestConfig config = RequestConfig.custom().setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
            httpPost.setConfig(config);
            httpPost.addHeader(HTTP.CONTENT_TYPE, HTTP_CONTENT_TYPE);
            httpPost.addHeader(HTTP.USER_AGENT, HTTP_USER_AGENT + mchId);
            httpPost.setEntity(new StringEntity(data, CHARSET_UTF_8));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity(), CHARSET_UTF_8);
            logger.info("官方微信--请求返回结果：{}", result);
            return result;
        } catch (Exception e) {
            logger.error("官方微信--请求失败！{}", e);
            return null;
        }
    }

    /**
     * post请求（不带证书）
     *
     * @param data       请求参数
     * @param requestUrl 请求地址
     * @return 请求返回结果
     */
    public static String requestPost(final String data, final String requestUrl) {
        logger.info("官方微信--请求地址：{},请求参数：{}", requestUrl, data);
        HttpClient httpClient = HttpClientUtils.getHttpClient();
        HttpPost httpPost = new HttpPost(requestUrl);
        StringEntity stringEntity = new StringEntity(data, "UTF-8");
        httpPost.setEntity(stringEntity);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");
            logger.info("官方微信--请求返回结果：{}", result);
            return result;
        } catch (IOException e) {
            logger.error("官方微信--请求失败！{}", e);
            return null;
        }
    }

    /**
     * get请求
     *
     * @param requestUrl 请求地址
     * @return 请求返回结果
     */
    public static String requestGet(final String requestUrl) {
        logger.info("官方微信--GET请求地址：{}", requestUrl);
        HttpClient httpClient = HttpClientUtils.getHttpClient();
        HttpGet httpGet = new HttpGet(requestUrl);
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String result = EntityUtils.toString(httpEntity, "UTF-8");
            logger.info("官方微信--GET请求返回结果：{}", result);
            return result;
        } catch (IOException e) {
            logger.error("官方微信--GET请求失败！{}", e);
            return null;
        }
    }

    /**
     * 获取平台证书
     *
     * @param mchId
     * @param merchantSecret
     * @return
     */
    public static Map<String, Object> getCertificates(String mchId, String merchantSecret) {
        logger.info("官方微信--获取平台证书");
        SortedMap<String, Object> paramMap = new TreeMap<>();
        paramMap.put("mch_id", mchId);
        paramMap.put("nonce_str", createNonceStr());
        paramMap.put("sign_type", "HMAC-SHA256");
        paramMap.put("sign", sha256Sign(paramMap, merchantSecret));

        String data = mapToXml(paramMap);
        logger.info("官方微信--获取平台证书-请求参数:\r\n{}", data);
        String result = requestPost(data, "https://api.mch.weixin.qq.com/risk/getcertficates");
        logger.info("官方微信--获取平台证书-返回结果:\r\n{}", result);

        Map<String, Object> resultMap = xmlToMap(result);
        logger.info("官方微信--获取平台证书-解析结果:{}", resultMap);
        return resultMap;
    }

    /**
     * @param content    对敏感内容（入参Content）加密
     * @param ciphertext 平台证书接口得到的参数certificates包含了加密的平台证书内容ciphertext
     * @return
     * @throws Exception
     */
    public static String rsaEncrypt(String content, String ciphertext) throws Exception {
        final byte[] PublicKeyBytes = ciphertext.getBytes();
        X509Certificate certificate = X509Certificate.getInstance(PublicKeyBytes);
        PublicKey publicKey = certificate.getPublicKey();
        Cipher ci = Cipher.getInstance("RSA/ECB/PKCS1Padding", "SunJCE");
        ci.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.encode(ci.doFinal(content.getBytes("UTF-8")));
    }


    /**
     * @param aad        encrypt_certificate.associated_data
     * @param iv         encrypt_certificate.nonce
     * @param cipherText encrypt_certificate.ciphertext
     * @return 返回ciphertext明文
     * @throws Exception
     */
    public static String aesgcmDecrypt(String aad, String iv, String cipherText, String APIv3Secret) throws Exception {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(APIv3Secret.getBytes(), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        cipher.updateAAD(aad.getBytes());
        return new String(cipher.doFinal(Base64.decode(cipherText)));
    }


    /**
     * 文件转MD5Hash
     *
     * @param fis
     * @return
     */
    public static String md5HashCode(InputStream fis) {
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }


    /**
     * 签名
     *
     * @param paramMap
     * @param key
     * @return
     */
    public static String getSign(SortedMap<String, Object> paramMap, String key) {
        StringBuilder signBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if (!"sign".equals(entry.getKey()) && null != entry.getValue()) {
                signBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        signBuilder.append("key=").append(key);
        logger.info("微信待签名参数字符串:{}", signBuilder.toString());
        return MD5.encode(signBuilder.toString()).toUpperCase();
    }


    /**
     * 发送https请求
     *
     * @param requestUrl    请求地址
     * @param requestMethod 请求方式（GET、POST）
     * @param outputStr     提交的数据
     * @return JSONObject(通过JSONObject.get ( key)的方式获取json对象的属性值)
     */
    public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        HttpsURLConnection conn = null;
        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = {new MyX509TrustManager()};
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            URL url = new URL(requestUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(ssf);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 设置请求方式（GET/POST）
            conn.setRequestMethod(requestMethod);
            // 当outputStr不为null时向输出流写数据
            if (null != outputStr) {
                OutputStream outputStream = conn.getOutputStream();
                // 注意编码格式
                outputStream.write(outputStr.getBytes("UTF-8"));
                outputStream.close();
            }
            // 从输入流读取返回内容
            inputStream = conn.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String str;
            StringBuffer buffer = new StringBuffer();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            return buffer.toString();
        } catch (ConnectException ce) {
            log.error("连接超时：{}", ce);
        } catch (Exception e) {
            log.error("https请求异常：{}", e);
        } finally {
            // 释放资源
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            if (null != inputStreamReader) {
                try {
                    inputStreamReader.close();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            if (null != conn) {
                try {
                    conn.disconnect();
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
        return null;
    }



    /**
     * 获取微信access_token
     * @param appId
     * @param appSecret
     * @param code
     * @return
     */
    public static JSONObject accessToken(String appId, String appSecret, String code) {
        String URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
        URL = String.format(URL, appId, appSecret, code);
        try {
            String jsonBody = WXCommonUtils.requestGet(URL);
            if (!org.springframework.util.StringUtils.isEmpty(jsonBody)) {
                return JSONObject.parseObject(jsonBody);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }


    /**
     * 刷新微信access_token
     * @param appid
     * @param refreshToken
     * @return
     */
    public static JSONObject refreshAccessToken(String appid,String refreshToken){
        String URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s";
        URL = String.format(URL, appid, refreshToken);
        try {
            String jsonBody = WXCommonUtils.requestGet(URL);
            if (!org.springframework.util.StringUtils.isEmpty(jsonBody)) {
                return JSONObject.parseObject(jsonBody);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }



    /**
     * 获取用户信息
     * @param accessToken
     * @param openid
     * @return
     */
    public static JSONObject getUserInfo(String accessToken, String openid) {
        String URL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN";
        URL = String.format(URL, accessToken, openid);
        try {
            String jsonBody = WXCommonUtils.requestGet(URL);
            if (!org.springframework.util.StringUtils.isEmpty(jsonBody)) {
                return JSONObject.parseObject(jsonBody);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }



    /**
     * 解密用户手机号
     *
     * @param sessionKey sessionKey
     * @param ivStr      ivData
     * @param encDataStr 带解密数据
     * @return JSON格式数据
     */
    public static String decrypt(String sessionKey, String ivStr, String encDataStr){
        log.info("sessionKey: {}", sessionKey);
        log.info("ivStr: {}", ivStr);
        log.info("encDataStr: {}", encDataStr);
        byte[] encData = Base64Utils.decodeFromString(encDataStr);
        byte[] ivs = Base64Utils.decodeFromString(ivStr);
        byte[] key = Base64Utils.decodeFromString(sessionKey);

        try {
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivs);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new String(cipher.doFinal(encData), "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(),e);
        } catch (NoSuchPaddingException e) {
            logger.error(e.getMessage(),e);
        } catch (InvalidKeyException e) {
            logger.error(e.getMessage(),e);
        } catch (InvalidAlgorithmParameterException e) {
            logger.error(e.getMessage(),e);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
        } catch (IllegalBlockSizeException e) {
            logger.error(e.getMessage(),e);
        } catch (BadPaddingException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }






}
