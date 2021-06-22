package com.bird.sso.utils.rsa.v2;

import com.bird.sso.api.ex.SSOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/2/2 12:16
 */
@Slf4j
public class RSA {

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;


    //私钥
    public final static String PRIVATE_KEY
            = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMbk5zzqZpwWFqf9qpzdpLa/WZNG5ddBwCGuS2MrjPqhuSFmBarABZsXzHabs3ffYoxhQaRuD0/6C4ifkE9VRIp2Chg4LfxAYwDxOP4rL8bES3Cm+eTywxMqqdN4za04aYir/yKYOa1ow4N4snTk1Lqd0i+wP2HBh0TIVENCyDq5AgMBAAECgYEAkagzPGZBLQ/0GaTo3KZpKThrJjNmwUSyqzfUl9TeEvJ6/N2VSt4DfPhp0XbncONUbZAQHZZIen6ZEb4hVigQ37ctofQ92+rrV0Zq1L08Ub6fHBAOKfTuyPFSgb6liPde3v/7wCkbsXDUcoS6469J9wM06/Z2CGUDQgLaIf8gWj0CQQDpgv6E69Vk+C8yhtcyUpmwXbF/Q8DXLekwnOpAU96RfFa/wlZhBXOsO2SL5izcGG9LpCs/+cjdUi3O8bYWlif3AkEA2gxyaKeACnixFNqIxiIO2zmTL98CXXPGRRG2Dp/naDpAcUK7yxj3aCD8+qtTj0jk2G3DAXXZLjjnQbC94rrmzwJAO65dOr/DxJCiD3njGlt+KFNqACri6Jw1U1VnkZVd9zUePth+l0F7dPOhGRh1MrOUDnMhErLWoapVRuUEMvWhtQJADsQFA11EaHEf3L+nv3MS+nl9pCI/8MAkTw0R/Q+LBh3bu0+/k9NO/bguKXNspylShe145SJr4Jtt3Z7wXOg3NQJAMRw4h3VmrGPuKMcde0xmvg/nhcTf7f70mzhrvLOMyuyErpL+3gXU/lDsVLAisBFF3oRQJ9z4u/OzX+htT+d7uA==";


    //公钥
    public final static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG5Oc86macFhan/aqc3aS2v1mTRuXXQcAhrktjK4z6obkhZgWqwAWbF8x2m7N332KMYUGkbg9P+guIn5BPVUSKdgoYOC38QGMA8Tj+Ky/GxEtwpvnk8sMTKqnTeM2tOGmIq/8imDmtaMODeLJ05NS6ndIvsD9hwYdEyFRDQsg6uQIDAQAB";


    /**
     * 获取密钥对
     *
     * @return 密钥对
     */
    public static KeyPair getKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    /**
     * 获取私钥
     *
     * @param privateKey 私钥字符串
     * @return
     */
    public static PrivateKey getPrivateKey(String privateKey) {
        PrivateKey pri = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] decodedKey = Base64.decodeBase64(privateKey.getBytes());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            pri = keyFactory.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return pri;
    }

    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    public static PublicKey getPublicKey(String publicKey) {
        PublicKey pub = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] decodedKey = Base64.decodeBase64(publicKey.getBytes());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            pub = keyFactory.generatePublic(keySpec);

        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            log.error(e.getMessage(), e);
        }
        return pub;
    }

    /**
     * RSA加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return
     */
    public static String encrypt(String data, PublicKey publicKey) {
        byte[] encryptedData = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int inputLen = data.getBytes().length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data.getBytes(), offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data.getBytes(), offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK;
            }
            encryptedData = out.toByteArray();
            out.close();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            log.error(e.getMessage(), e);
        } catch (BadPaddingException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        return new String(Base64.encodeBase64String(encryptedData));
    }

    /**
     * RSA解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return
     */
    public static String decrypt(String data, PrivateKey privateKey) {
        byte[] decryptedData;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] dataBytes = Base64.decodeBase64(data);
            int inputLen = dataBytes.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_DECRYPT_BLOCK;
            }
            decryptedData = out.toByteArray();
            out.close();
            return new String(decryptedData, "UTF-8");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            log.error(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            log.error(e.getMessage(), e);
        } catch (BadPaddingException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        // 解密后的内容
        throw new SSOException(SSOException.FAILED, "解析失败");
    }

    /**
     * 签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    public static String sign(String data, PrivateKey privateKey) throws Exception {
        byte[] keyBytes = privateKey.getEncoded();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey key = keyFactory.generatePrivate(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(key);
        signature.update(data.getBytes());
        return new String(Base64.encodeBase64(signature.sign()));
    }

    /**
     * 验签
     *
     * @param srcData   原始字符串
     * @param publicKey 公钥
     * @param sign      签名
     * @return 是否验签通过
     */
    public static boolean verify(String srcData, PublicKey publicKey, String sign) throws Exception {
        byte[] keyBytes = publicKey.getEncoded();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(key);
        signature.update(srcData.getBytes());
        return signature.verify(Base64.decodeBase64(sign.getBytes()));
    }


    public static void main(String[] args) {
        try {
            // 生成密钥对
            KeyPair keyPair = getKeyPair();
            String privateKey = /*new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded()))*/
                    PRIVATE_KEY;
            String publicKey = /*new String(Base64.encodeBase64(keyPair.getPublic().getEncoded()))*/
                    PUBLIC_KEY;
            System.out.println("私钥:" + privateKey);
            System.out.println("公钥:" + publicKey);
            // RSA加密
            String data = "15262619831022151X";
            String encryptData = encrypt(data, getPublicKey(publicKey));
            System.out.println("加密后内容:" + encryptData);
            // RSA解密
            String decryptData = decrypt(encryptData, getPrivateKey(privateKey));
            System.out.println("解密后内容:" + decryptData);

            // RSA签名
            String sign = sign(data, getPrivateKey(privateKey));
            // RSA验签
            boolean result = verify(data, getPublicKey(publicKey), sign);
            System.out.print("验签结果:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print("加解密异常");
        }
    }


}
