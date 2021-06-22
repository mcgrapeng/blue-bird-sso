package com.bird.sso.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.Random;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/16 10:58
 */
@Slf4j
public final class CommonUtils {


    public static String getRandom(int len) {
        String val = "";
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            val += String.valueOf(random.nextInt(10));
        }
        return val;
    }


    /*
     * 中文转unicode编码
     */
    public static String gbEncoding(final String gbString) {
        char[] utfBytes = gbString.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }

    /*
     * unicode编码转中文
     */
    public static String decodeUnicode(final String dataStr) {
        int start = 0;
        int end = 0;
        final StringBuffer buffer = new StringBuffer();
        while (start > -1) {
            end = dataStr.indexOf("\\u", start + 2);
            String charStr = "";
            if (end == -1) {
                charStr = dataStr.substring(start + 2, dataStr.length());
            } else {
                charStr = dataStr.substring(start + 2, end);
            }
            char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
            buffer.append(new Character(letter).toString());
            start = end;
        }
        return buffer.toString();
    }


    /**
     * 深拷贝
     *
     * @param src
     * @param <T>
     * @return
     */
    public static <T> T deepClone(T src) {
        T dest = null;
        try {
            // 将该对象序列化成流,因为写在流里的是对象的一个拷贝
            // ，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
            oos.writeObject(src);
            //将流序列化成对象
            ByteArrayInputStream byteArrayInputStream
                    = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
            dest = (T) ois.readObject();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return dest;
    }


    /**
     * 生成规则设备编号:应用类型+四位编号（从1开始，不够前补0）
     *
     * @param appType  应用类型
     * @param bizType  业务类型
     * @param serialNo 编号
     * @return
     */
    public static String getCode(String appType, String bizType, String serialNo) {
        String numberNo = "0001";
        if (org.apache.commons.lang3.StringUtils.isNotBlank(serialNo)) {
            int next = Integer.parseInt(serialNo.substring(serialNo.indexOf(bizType) + 1)) + 1;
            numberNo = String.format("%04d", next);
        }
        return org.apache.commons.lang3.StringUtils.join(appType, "_", bizType, numberNo);
    }

    public static void main(String[] args) {
        String s = getCode("APP_UF", "M", "1");
        System.out.println(s);
    }
}
