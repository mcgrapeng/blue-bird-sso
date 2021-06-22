package com.bird.sso.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.bird.RES;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.conts.Constants;
import com.bird.sso.conts.RedisTimer;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.UserManageService;
import com.bird.sso.service.UserQueryService;
import com.bird.sso.utils.RedisUtils;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.utils.wx.WXCommonUtils;
import com.bird.sso.web.conts.ResultEnum;
import com.bird.sso.web.wx.WXLoginAuth;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/8/18 10:33
 */
@Slf4j
@Controller
@RequestMapping("/sso/wx")
public class WXLoginController {


    private static final String APP_ID = "wxddd0dc9d8168d93d";

    //    private static final String APP_SECRET = "58d5086aeb8276674412123099c71bba";
    private static final String APP_SECRET = "b4784fa6130bbffe7b59a128f8ccbe8b";


    private static final String WX_REDIS_OPENID_PREFIX = "sso:wx:auth:openid:";
    private static final String WX_REDIS_ACCESS_TOKEN_PREFIX = "sso:wx:auth:access:token:";
    private static final String WX_REDIS_TOKEN_PREFIX = "sso:wx:auth:token:";


    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserManageService userManageService;

    @Autowired
    private RedisUtils redisUtils;


    /**
     * 微信小程序登录同意授权
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public RES<Map<String, Object>> login(@RequestBody @Validated WXLoginAuth req) {

        Map<String, Object> accessTokenAndOpenId = getAccessTokenAndOpenId(req.getCode());

        String accessToken = MapUtils.getString(accessTokenAndOpenId, "accessToken");
        String openId = MapUtils.getString(accessTokenAndOpenId, "openId");
        long l = SnowflakeIdWorker.build(9).nextId();

        Map<String, Object> data = Maps.newHashMap();
        data.put("sessionKey", accessToken);
        data.put("token", WX_REDIS_TOKEN_PREFIX + l);

        redisUtils.set(WX_REDIS_TOKEN_PREFIX + l, WX_REDIS_TOKEN_PREFIX + l, RedisTimer.MONTH);
        redisUtils.set(WX_REDIS_ACCESS_TOKEN_PREFIX + l, accessToken, RedisTimer.MONTH);
        redisUtils.set(WX_REDIS_OPENID_PREFIX + l, openId, RedisTimer.MONTH);

        return RES.of(ResultEnum.处理成功.code, data, ResultEnum.处理成功.name());
    }


    /**
     * 公众号微信同意授权
     *
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public void authorize(HttpServletResponse response) throws IOException {
        //party.bjchy.bird.com 是在公众号里设置的网页授权域名
        // https://party.bjchy.bird.com:8899/sso/wx/userInfo指的是下面那个接口的路径
        String returnUrl = URLEncoder.encode("https://party.bjchy.bird.com:8899/sso/wx/userInfo", "UTF-8");
        // 按照文档要求拼接访问地址
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                + APP_ID
                + "&redirect_uri="
                + returnUrl
                + "&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";


        log.info("redirectUrl={}", url);
        response.sendRedirect(url);// 跳转到要访问的地址;
    }


    /**
     * 小程序获取openid、accessToken
     *
     * @param code
     * @return
     */
    private Map<String, Object> getAccessTokenAndOpenId(String code) {
        JSONObject code2SessionJSON = WXCommonUtils.accessToken(APP_ID, APP_SECRET, code);
        if (null == code2SessionJSON) {
            log.error("auth.accessToken--->微信返回报文数据为空！");
            throw new SSOException(ResultEnum.处理失败.code, ResultEnum.处理失败.name());
        }
        log.info("auth.code2Session--->微信返回报文数据，result = {}", code2SessionJSON.toJSONString());
        Integer errCode = code2SessionJSON.getInteger("errcode");
        if (null != errCode
                && errCode != 0) {
            log.error("auth.code2Session发生异常，getSessionKeyOrOpenId---> errCode = {}", errCode);
            throw new SSOException(ResultEnum.处理失败.code, ResultEnum.处理失败.name());
        }
        Map<String, Object> m = Maps.newHashMap();
        m.put("accessToken", code2SessionJSON.getString("session_key"));
        m.put("unionId", code2SessionJSON.getString("unionid"));
        m.put("errCode", code2SessionJSON.getString("errcode"));
        m.put("errMsg", code2SessionJSON.getString("errmsg"));
        m.put("openId", code2SessionJSON.getString("openid"));
        return m;
    }


    /**
     * 微信公众号获取用户信息
     *
     * @param accessToken
     * @param openId
     * @return
     */
    private SysUser getUserInfo(String accessToken, String openId) {
        JSONObject userInfo = WXCommonUtils.getUserInfo(accessToken, openId);
        log.info("获取微信用户信息,getUserInfo-->user={}", userInfo.toJSONString());
        SysUser user = new SysUser();
        user.setNickName(userInfo.getString("nickname"));
        user.setCreateTime(new Date());
        user.setWxOpenId(userInfo.getString("openid"));
        user.setUpdateTime(new Date());
        user.setHeadImg(userInfo.getString("headimgurl"));
        user.setIsBindMobile(PublicEnum.N.name());
        user.setWxUnionId(userInfo.getString("unionid"));
        //user.setUserName(userInfo.getString("openid"));
        return user;
    }


    @RequestMapping(value = "/get-user-info", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> getMobile(@RequestBody @Validated WXLoginAuth req) {

        String accessToken = getAccessToken(req.getToken());
        String decrypt = WXCommonUtils.decrypt(accessToken, req.getIv(), req.getEncryptedData());
        if (StringUtils.isBlank(decrypt)) {
            log.error("获取微信用户手机信息发生异常，getUserInfo--->sessionKey={},iv={},encryptedData={}", getAccessToken(req.getToken())
                    , req.getIv(), req.getEncryptedData());
            throw SSOException.AUTH_FAIL;
        }
        JSONObject mobileObj = JSON.parseObject(decrypt);
        log.info("获取微信用户手机信息,getUserWxMobile-->mobileObj={}", JSON.toJSONString(mobileObj));
        String phoneNumber = mobileObj.getString("purePhoneNumber");

        SysUser u = userQueryService.findUserByUserName(req.getAppType(), phoneNumber);
        if (ObjectUtils.isEmpty(u)) {
            String openId = getOpenId(req.getToken());
            u = getUserInfo(getAccessToken(req.getToken()), openId);
            u.setUserName(phoneNumber);
            long userId = userManageService.createUser(u);
            u.setUserId(userId);
            u.setAppType(req.getAppType());
        }

        String loginSource = WebUtils.getHeader(Constants.LOGIN_SOURCE);
        String jwt = JWTHelper.generateWxToken(u, loginSource);
        // 这里创建的token只是单纯的token
        // 按照jwt的规定，最后请求的时候应该是 `Bearer token`
        WebUtils.getResponse().setCharacterEncoding("UTF-8");
        WebUtils.getResponse().setContentType("application/json; charset=utf-8");
        WebUtils.getResponse().setHeader("Authorization", jwt);
        return RES.of(ResultEnum.处理成功.code, phoneNumber, ResultEnum.处理成功.name());
    }


    /**
     * 小程序获取微信手机号
     *
     * @param sessionKey
     * @param iv
     * @param encryptedData
     * @return
     */
    private String getUserWxMobile(String sessionKey, String iv, String encryptedData) {
        String decrypt = WXCommonUtils.decrypt(sessionKey, iv, encryptedData);
        if (StringUtils.isBlank(decrypt)) {
            log.error("获取微信用户手机信息发生异常，getUserInfo--->sessionKey={},iv={},encryptedData={}", sessionKey, iv, encryptedData);
            return null;
        }
        JSONObject mobileObj = JSON.parseObject(decrypt);
        log.info("获取微信用户手机信息,getUserWxMobile-->mobileObj={}", JSON.toJSONString(mobileObj));
        String phoneNumber = mobileObj.getString("purePhoneNumber");
        return phoneNumber;
    }


    /**
     * 小程序获取用户信息
     *
     * @param sessionKey
     * @param iv
     * @param encryptedData
     * @return
     */
    private SysUser getUserInfo(String sessionKey, String iv, String encryptedData) {
        String decrypt = WXCommonUtils.decrypt(sessionKey, iv, encryptedData);
        if (StringUtils.isBlank(decrypt)) {
            log.error("获取微信用户信息发生异常，getUserInfo--->sessionKey={},iv={},encryptedData={}", sessionKey, iv, encryptedData);
            return null;
        }

        JSONObject userInfoObj = JSON.parseObject(decrypt);
        log.info("获取微信用户信息,getUserInfo-->user={}", JSON.toJSONString(userInfoObj));

        SysUser user = new SysUser();
        user.setNickName(userInfoObj.getString("nickName"));
        user.setCreateTime(new Date());
        user.setWxOpenId(userInfoObj.getString("openId"));
        user.setUpdateTime(new Date());
        user.setHeadImg(userInfoObj.getString("avatarUrl"));
        user.setWxUnionId(userInfoObj.getString("unionId"));
        user.setUserName(userInfoObj.getString("openId"));
        return user;
    }


    private String getAccessToken(String token) {
        return (String) redisUtils.get(WX_REDIS_ACCESS_TOKEN_PREFIX + token);
    }


    private String getOpenId(String token) {
        return (String) redisUtils.get(WX_REDIS_OPENID_PREFIX + token);
    }


}
