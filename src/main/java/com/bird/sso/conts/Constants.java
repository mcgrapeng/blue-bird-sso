package com.bird.sso.conts;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/20 11:02
 */
public final class Constants {


    public static final String LOGIN_SOURCE = "source";

    public static final String LOGIN_PROFILE = "profile";

    public static final String SSO_HEAD_IMG_PREFIX = "sso/user";

    public static final String bird_OSS = "bird/oss";

    /**
     * redis前缀
     */
    public static class RedisPrefix {
        public static final String ALGORITHM_NAME = "RSA";
        public static final String MD5_RSA = "MD5withRSA";
        public static final String RSA_PUBLIC_KEY = "bird:RSA:PUBLIC:KEY";
        public static final String RSA_PRIVATE_KEY = "bird:RSA:PRIVATE:KEY";
        public final static String LOGIN_ERROR_COUNT = "pm:login:error:count:%s:%s";
        public static final String ACCESS_TOKEN = "ACCESS_TOKEN:%s:%s:%s";
        public static final String ACCESS_APP_TYPE_TOKEN = "bird:ACCESS_APP_TYPE_TOKEN:%s:%s";
        public static final String SYS_DATA_ORGANIZE = "bird:ORGANIZE:TREE:%s";
        public static final String SYS_DATA_USER_ORGANIZE = "bird:ORGANIZE:USER:TREE:%s";
        public static final String SYS_ROLE_CODE = "bird:SSO:ROLE:CODE:%s";
        public static final String SYS_MENU_CODE = "bird:SSO:MENU:CODE:%s";

        public static final String LOGIN_SUCCESS = "bird:LOGIN:SUCCESS:%s:%s";
    }
}
