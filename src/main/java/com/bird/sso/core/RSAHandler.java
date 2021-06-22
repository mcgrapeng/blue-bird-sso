//package com.bird.sso.core;
//
//import com.bird.sso.api.ex.SSOException;
//import com.bird.sso.conts.Constants;
//import com.bird.sso.utils.rsa.Base64Utils;
//import com.bird.sso.utils.rsa.RSAUtils;
//import com.bird.sso.utils.rsa.v2.RSA;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.security.KeyPair;
//
///**
// * @author 张朋
// * @version 1.0
// * @desc
// * @date 2020/5/20 11:09
// */
//@Slf4j
//@Component
//public class RSAHandler {
//
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//
//    /**
//     * 获取私钥
//     *
//     * @return
//     */
//   /* public String getPrivateKey(String requestId) {
//        return (String) redisTemplate.opsForValue().get(StringUtils.join(
//                Constants.RedisPrefix.RSA_PRIVATE_KEY, requestId));
//    }*/
//
//
//    /**
//     * 密码解密
//     *
//     * @param password
//     * @param requestId
//     * @return
//     */
//  /*  public String getDecryptPwd(String password, String requestId) {
//        String privateKey = getPrivateKey(requestId);
//        if (org.springframework.util.StringUtils.isEmpty(privateKey)) {
//            throw new SSOException(SSOException.FAILED, "私钥已过期，请重新申请~");
//        }
//
//        String decryptPwd;
//        try {
//            decryptPwd = RSAUtils.decrypt(password, RSAUtils.getPrivateKey(privateKey));
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            throw new SSOException(SSOException.FAILED, "密码解析错误，请使用合法的公钥~");
//        }
//        return decryptPwd;
//    }
//*/
//
//    @Deprecated
//    public String getPublicKey() {
//        KeyPair keyPair = null;
//        try {
//            keyPair = RSAUtils.getKeyPair();
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        String privateKey = new String(Base64Utils.encoder(keyPair.getPrivate().getEncoded()));
//        String publicKey = new String(Base64Utils.encoder(keyPair.getPublic().getEncoded()));
//        redisTemplate.opsForValue().set(Constants.RedisPrefix.RSA_PUBLIC_KEY, publicKey);
//        redisTemplate.opsForValue().set(Constants.RedisPrefix.RSA_PRIVATE_KEY, privateKey);
//        return publicKey;
//    }
//
//    @Deprecated
//    public String getPrivateKey() {
//        return (String) redisTemplate.opsForValue().get(Constants.RedisPrefix.RSA_PRIVATE_KEY);
//    }
//
//
//    @Deprecated
//    public String getDecryptPwd(String password) {
//        String privateKey = getPrivateKey();
//        if (org.springframework.util.StringUtils.isEmpty(privateKey)) {
//            throw new SSOException(SSOException.FAILED, "私钥已过期，请重新申请~");
//        }
//
//        String decryptPwd;
//        try {
//            decryptPwd = RSAUtils.decrypt(password, RSAUtils.getPrivateKey(privateKey));
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            throw new SSOException(SSOException.FAILED, "密码解析错误，请使用合法的公钥~");
//        }
//        return decryptPwd;
//    }
//
//
//    public String getDecryptPassword(String password) {
//        return RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY));
//    }
//
//
///*
//
//    public void setLoginErrorTimes(String username) {
//        String ip = WebUtils.getIpAddr(WebUtils.getRequest());
//        String cacheKey = String.format(Constants.RedisPrefix.LOGIN_ERROR_COUNT, username, ip);
//        Integer value = Integer.valueOf((String) redisTemplate.opsForValue().get(cacheKey));
//        if (null != value) {
//            value += 1;
//        }
//        redisTemplate.opsForValue().set(cacheKey, Integer.toString(value));
//        redisTemplate.expire(cacheKey, 10, TimeUnit.MINUTES);
//    }
//
//    public int getLoginErrorTimes(String username) {
//        String ip = WebUtils.getIpAddr(WebUtils.getRequest());
//        String cacheKey = String.format(Constants.RedisPrefix.LOGIN_ERROR_COUNT, username, ip);
//        return Integer.valueOf((String) redisTemplate.opsForValue().get(cacheKey));
//    }
//
//    public void resetLoginErrorTimes(String username) {
//        String ip = WebUtils.getIpAddr(WebUtils.getRequest());
//        String cacheKey = String.format(Constants.RedisPrefix.LOGIN_ERROR_COUNT, username, ip);
//        redisTemplate.delete(cacheKey);
//    }
//*/
//
//
//}
