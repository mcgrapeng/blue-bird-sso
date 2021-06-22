package com.bird.sso.web.controller;

import com.google.common.collect.Lists;
import com.bird.common.tools.SmsHelper;
import com.bird.common.tools.SmsTemplateEnum;
import com.bird.common.tools.VerifyCodeService;
import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.service.UserManageService;
import com.bird.sso.utils.rsa.v2.RSA;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.UserAuth;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

/**
 * 认证
 *
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 18:00
 */
@Slf4j
@Controller
@RequestMapping("/sso")
public class AuthenticationController extends BaseController {

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    private UserManageService userManageService;

    /**
     * 发送验证码
     *
     * @param mobile
     * @param appType
     * @return
     * @throws ParseException
     */
    @RequestMapping("/sms/code/{appType}/{mobile}")
    @ResponseBody
    public RES sendSms(@PathVariable String mobile, @PathVariable String appType) throws ParseException {
        if (StringUtils.isBlank(mobile)) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }
//        if (!MobileUtils.isPhoneLegal(mobile)) {
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }

        String verifyCode = verifyCodeService.getVerifyCode(appType, mobile);
        if (StringUtils.isNotBlank(verifyCode)) {
            return RES.of(ResultEnum.原验证码在有效期内.code, ResultEnum.原验证码在有效期内.name());
        }

        String smsCode = verifyCodeService.generateVerifyCode(appType, mobile, 5 * 60);

        log.info(">>>>>>>>>>>>>>>>>>sendSms>>>>>>>>>mobile={},smsCode={}", mobile, smsCode);

        SmsTemplateEnum templateEnum = SmsTemplateEnum.getSmsTemplate(appType);
        boolean result = SmsHelper.send(Lists.newArrayList(mobile), templateEnum, smsCode);
        if (!result) {
            return RES.of(ResultEnum.处理失败.code, ResultEnum.处理失败.name());
        }
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 检查验证码是否有效
     *
     * @param appType
     * @param mobile
     * @param code
     * @return
     * @throws ParseException
     */
    @RequestMapping("/sms/check-vercode/{appType}/{mobile}/{code}")
    @ResponseBody
    public RES<Boolean> checkVercode(@PathVariable String appType, @PathVariable String mobile, @PathVariable String code) throws ParseException {
        if (StringUtils.isBlank(mobile)) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }
//        if(!MobileUtils.isPhoneLegal(mobile)){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        if (StringUtils.isBlank(code)) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }
        return RES.of(ResultEnum.处理成功.code, verifyCodeService.checkVerifyCode(appType, mobile, code), ResultEnum.处理成功.name());
    }


    /**
     * 注册(拥有所有权限)
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public RES<Long> register(@RequestBody @Validated(value = {UserAuth.UserReg.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        return RES.of(ResultEnum.处理成功.code, userManageService.userRegisterAndALLAuthorities(auth.getAppType(), auth.getUsername(), auth.getPassword(), auth.getUserType(), auth.getVercode()), ResultEnum.处理成功.name());
    }


    /**
     * 注册（拥有默认权限)
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/register/non-permit", method = RequestMethod.POST)
    @ResponseBody
    public RES<Long> registerNonPermit(@RequestBody @Validated(value = {UserAuth.UserReg.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        return RES.of(ResultEnum.处理成功.code, userManageService.userRegisterAndDefaultAuthorities(auth.getAppType(), auth.getUsername(), auth.getPassword(), auth.getUserType(), auth.getVercode()), ResultEnum.处理成功.name());
    }


    /**
     * 注册（拥有指定权限)
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/register/spe-permit", method = RequestMethod.POST)
    @ResponseBody
    public RES<Long> registerSpePermit(@RequestBody @Validated(value = {UserAuth.UserSpecReg.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        List<String> authRoleCode = auth.getRoleCode();
        if(CollectionUtils.isEmpty(authRoleCode)){
            throw SSOException.PARAM_ERR;
        }
        return RES.of(ResultEnum.处理成功.code, userManageService.userRegisterAndAuthorities(auth.getAppType()
                , auth.getUsername(), auth.getPassword(), auth.getUserType(), auth.getVercode(), authRoleCode), ResultEnum.处理成功.name());
    }


    /**
     * 绑定手机(默认，验证码方式)
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/bind-mobile", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> bindMobile(@RequestBody @Validated(value = {UserAuth.UserMobile.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        userManageService.bindMobile( auth.getUsername(), auth.getVercode());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 绑定手机
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/bind-mobile/{method}", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> bindMobile(@PathVariable String method, @RequestBody @Validated(value = {UserAuth.UserMobile.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        if (!Lists.newArrayList(UserManageService.BindMobileMethod.VERCODE.name()
                , UserManageService.BindMobileMethod.PASSWORD.name()).contains(method)) {
            return RES.of(ResultEnum.请求参数不匹配.code, ResultEnum.请求参数不匹配.name());
        }

        userManageService.bindMobile(method, auth.getUsername(), auth.getVercode());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 忘记密码（存在bug）
     *
     * @param auth
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/password-forget", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> userPasswordForget(@RequestBody @Validated(value = {UserAuth.UserForgetPass.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        userManageService.userPasswordForget(auth.getAppType(), auth.getUsername(), auth.getPassword());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }

    /**
     * 忘记密码（验证码方式）
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/vercode/password-forget", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> userVercodePasswordForget(@RequestBody @Validated(value = {UserAuth.UserVerForgetPass.class}) UserAuth auth) {
//        if(!MobileUtils.isPhoneLegal(auth.getUsername())){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        userManageService.userPasswordForget(auth.getAppType(), auth.getUsername(), auth.getPassword(), auth.getVercode());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }

    /**
     * 修改密码（密码方式）
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/password-update", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> userPasswordUpdate(@RequestBody @Validated(value = {UserAuth.UserUpdatePass.class}) UserAuth auth) {
        userManageService.userPasswordUpdate(auth.getPassword(), auth.getNewPassword());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 修改密码（验证码方式）
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/ver/password-update", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> userVerifyCodePasswordUpdate(@RequestBody @Validated(value = {UserAuth.UserVerUpdatePass.class}) UserAuth auth) {
        userManageService.userVerifyCodePasswordUpdate(auth.getVercode(), auth.getNewPassword());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 原始密码校验
     *
     * @param auth
     * @return
     */
    @RequestMapping(value = "/password-check", method = RequestMethod.POST)
    @ResponseBody
    public RES<Boolean> userPasswordCheck(@RequestBody @Validated(value = {UserAuth.UserCheckPass.class}) UserAuth auth) {
        return RES.of(ResultEnum.处理成功.code, userManageService.checkOriginalPassword(auth.getPassword()), ResultEnum.处理成功.name());
    }


    /**
     * 用户是否存在
     *
     * @param appType
     * @param userName
     * @return
     */
    @RequestMapping(value = "/user-exist/{appType}/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public RES<Boolean> userPasswordCheck(@PathVariable String appType, @PathVariable String userName) {
//        if(!MobileUtils.isPhoneLegal(userName)){
//            return RES.of(ResultEnum.手机号不合法.code, ResultEnum.手机号不合法.name());
//        }
        return RES.of(ResultEnum.处理成功.code, userManageService.isUserExists(appType, userName)
                , ResultEnum.处理成功.name());
    }

    /**
     * 获取密码的加密公钥
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/auth/public-key")
    @ResponseBody
    public RES<String> getPublicKey() {
        return RES.of(ResultEnum.处理成功.code, RSA.PUBLIC_KEY, ResultEnum.处理成功.name());
    }

}
