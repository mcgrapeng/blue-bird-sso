//package com.bird.sso.utils.rsa;
//
//import com.bird.sso.conts.Constants;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.binary.Base64;
//
//import javax.crypto.BadPaddingException;
//import javax.crypto.Cipher;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.security.*;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
//
///**
// * RSA密码加密工具类
// */
//@Slf4j
//public final class RSAUtils {
//
//    /**
//     * RSA最大加密明文大小
//     */
//    private static final int MAX_ENCRYPT_BLOCK = 117;
//
//    /**
//     * RSA最大解密密文大小
//     */
//    private static final int MAX_DECRYPT_BLOCK = 128;
//
//    /**
//     * 获取密钥对
//     *
//     * @return java.security.KeyPair
//     */
//    public static KeyPair getKeyPair() throws Exception {
//        KeyPairGenerator generator = KeyPairGenerator.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//        generator.initialize(1024);
//        return generator.generateKeyPair();
//    }
//
//    /**
//     * 获取私钥
//     *
//     * @param privateKey 私钥字符串
//     * @return java.security.PrivateKey
//     */
//    public static PrivateKey getPrivateKey(String privateKey) throws Exception {
//        KeyFactory keyFactory = KeyFactory.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//        byte[] decodedKey = Base64Utils.decoder(privateKey.getBytes());
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
//        return keyFactory.generatePrivate(keySpec);
//    }
//
//    /**
//     * 获取公钥
//     *
//     * @param publicKey 公钥字符串
//     * @return java.security.PublicKey
//     */
//    public static PublicKey getPublicKey(String publicKey) throws Exception {
//        KeyFactory keyFactory = KeyFactory.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//        byte[] decodedKey = Base64Utils.decoder(publicKey.getBytes());
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
//        return keyFactory.generatePublic(keySpec);
//    }
//
//    /**
//     * RSA加密
//     *
//     * @param data      待加密数据
//     * @param publicKey 公钥
//     */
//    public static String encrypt(String data, PublicKey publicKey) throws Exception {
//        Cipher cipher = Cipher.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        int inputLen = data.getBytes().length;
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        int offset = 0;
//        byte[] cache;
//        int i = 0;
//        // 对数据分段加密
//        while (inputLen - offset > 0) {
//            if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
//                cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
//            } else {
//                cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
//            }
//            out.write(cache, 0, cache.length);
//            i++;
//            offset = i * MAX_ENCRYPT_BLOCK;
//        }
//        byte[] encryptedData = out.toByteArray();
//        out.close();
//        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
//        // 加密后的字符串
//        return new String(Base64Utils.encoder(encryptedData));
//    }
//
//    /**
//     * RSA解密
//     *
//     * @param data       待解密数据
//     * @param privateKey 私钥
//     */
//    public static String decrypt(String data, PrivateKey privateKey) throws Exception {
//        byte[] decryptedData = new byte[0];
//        try {
//            Cipher cipher = Cipher.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//            cipher.init(Cipher.DECRYPT_MODE, privateKey);
//            byte[] dataBytes = Base64.decodeBase64(data);
//            int inputLen = dataBytes.length;
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            int offset = 0;
//            byte[] cache;
//            int i = 0;
//            //对数据分段解密
//            while (inputLen - offset > 0) {
//                if (inputLen - offset > MAX_DECRYPT_BLOCK) {
//                    cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
//                } else {
//                    cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
//                }
//                out.write(cache, 0, cache.length);
//                i++;
//                offset = i * MAX_DECRYPT_BLOCK;
//            }
//            decryptedData = out.toByteArray();
//            out.close();
//        } catch (NoSuchAlgorithmException e) {
//            throwExcepAndLog(e);
//        } catch (NoSuchPaddingException e) {
//            throwExcepAndLog(e);
//        } catch (InvalidKeyException e) {
//            throwExcepAndLog(e);
//        } catch (IllegalBlockSizeException e) {
//            throwExcepAndLog(e);
//        } catch (BadPaddingException e) {
//            throwExcepAndLog(e);
//        } catch (IOException e) {
//            throwExcepAndLog(e);
//        }
//        // 解密后的内容
//        return new String(decryptedData, "UTF-8");
//    }
//
//    private static void throwExcepAndLog(Exception e) throws Exception {
//        log.error(e.getMessage(), e);
//        throw new Exception("密码校验异常");
//    }
//
//
//    /**
//     * 签名
//     *
//     * @param data       待签名数据
//     * @param privateKey 私钥
//     */
//    public static String sign(String data, PrivateKey privateKey) throws Exception {
//        byte[] keyBytes = privateKey.getEncoded();
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//        PrivateKey key = keyFactory.generatePrivate(keySpec);
//        Signature signature = Signature.getInstance(Constants.RedisPrefix.MD5_RSA);
//        signature.initSign(key);
//        signature.update(data.getBytes());
//        return new String(Base64Utils.encoder(signature.sign()));
//    }
//
//    /**
//     * 验签
//     *
//     * @param srcData   原始字符串
//     * @param publicKey 公钥
//     * @param sign      签名
//     * @return boolean 是否验签通过
//     * @author zhouxinlei
//     * @date 2019-09-12 15:23:38
//     */
//    public static boolean verify(String srcData, PublicKey publicKey, String sign) throws Exception {
//        byte[] keyBytes = publicKey.getEncoded();
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance(Constants.RedisPrefix.ALGORITHM_NAME);
//        PublicKey key = keyFactory.generatePublic(keySpec);
//        Signature signature = Signature.getInstance(Constants.RedisPrefix.MD5_RSA);
//        signature.initVerify(key);
//        signature.update(srcData.getBytes());
//        return signature.verify(Base64Utils.decoder(sign.getBytes()));
//    }
//
//
////    public static void main(String[] args) {
////        try {
////            // 生成密钥对
////            KeyPair keyPair = getKeyPair();
////            String privateKey = new String(Base64Utils.encoder(keyPair.getPrivate().getEncoded()));
////            String publicKey = new String(Base64Utils.encoder(keyPair.getPublic().getEncoded()));
////            System.out.println("私钥:" + privateKey);
////            System.out.println("公钥:" + publicKey);
////            // RSA加密
////            String data = "123456";
////            String encryptData = encrypt(data, getPublicKey(publicKey));
////            System.out.println("加密后内容:" + encryptData);
////            // RSA解密
////            String decryptData = decrypt(encryptData, getPrivateKey(privateKey));
////            System.out.println("解密后内容:" + decryptData);
////            // RSA签名
////            String sign = sign(data, getPrivateKey(privateKey));
////            // RSA验签
////            boolean result = verify(data, getPublicKey(publicKey), sign);
////            System.out.print("验签结果:" + result);
////        } catch (Exception e) {
////            e.printStackTrace();
////            System.out.print("加解密异常");
////        }
////    }
//
//
//    public static void main(String[] args) throws Exception {
//        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyvAlA62yI74vWqa64PNLdqGtUZWdww7VYYAiEtKTiePUEsXgKESOtjC4UsTRvZVkJkYg1FMq1yZ+urz8jhyy6t9OQf0EvmWSCyfMI6saA1enYxKwHonxmtOu9vAH8kSFMA0proUKQg/hfqLfnmSbqpejHS3Ewjk8vuSmRoeu75QIDAQAB";
//        String data = "111111";
//        String encryptData = encrypt(data, getPublicKey(publicKey));
//        //System.out.println("加密后内容:" + encryptData);
//
//
//        String passwd = "iQUSD05IW1rS4hWID0UHGeHnBTx03N8wOcjpEV1hue0X0brSd9hxot8SNOx53934FrO4MgkTwjHiwVYGjfzoY3tzcVMJ6+GkrO7pfBl2WCm+pq6OqPgNt0AxGpS9UAkA8hMt5eBtXJ7hNcSuNJkT8OqD2ehgM+P33c8lwaqrVmI=";
//        //String userNa = "17701342555";
//
//        //String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyvAlA62yI74vWqa64PNLdqGtUZWdww7VYYAiEtKTiePUEsXgKESOtjC4UsTRvZVkJkYg1FMq1yZ+urz8jhyy6t9OQf0EvmWSCyfMI6saA1enYxKwHonxmtOu9vAH8kSFMA0proUKQg/hfqLfnmSbqpejHS3Ewjk8vuSmRoeu75QIDAQAB";
//        String pa = encrypt(data, getPublicKey(publicKey));
//
//        String jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxNzcwMTM0MjU1NSIsImNyZWF0ZWQiOjE1ODk0MjUzMTkwMDAsImlkIjoxLCJleHAiOjE1OTA3MjEzMTl9.BYmcIf37NGbw-k115FyaH8bFAhlRGJ7EQHKpj8ZfRkYYCJu8lfN_psysfrWZAq92_gKngVfmKhQuW1YjSPxdNA";
//
//
//        String m = " 111111";
//        //String p = encrypt("111111", getPublicKey(m));
//
//        String pub = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCyvAlA62yI74vWqa64PNLdqGtUZWdww7VYYAiEtKTiePUEsXgKESOtjC4UsTRvZVkJkYg1FMq1yZ+urz8jhyy6t9OQf0EvmWSCyfMI6saA1enYxKwHonxmtOu9vAH8kSFMA0proUKQg/hfqLfnmSbqpejHS3Ewjk8vuSmRoeu75QIDAQAB";
//
//        String decrypt = encrypt(m, getPublicKey(pub));
//
//        System.out.println(decrypt);
//
//    }
//
//
//
//
//
//
//}
