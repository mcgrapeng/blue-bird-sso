//package com.bird.sso.core.auth.wx;
//
//import com.bird.common.tools.VerifyCodeService;
//import com.bird.sso.core.UserService;
//import com.bird.sso.core.auth.otp.SMSAuthenticationToken;
//import com.bird.sso.web.conts.ResultEnum;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//
///**
// * @author 张朋
// * @version 1.0
// * @desc
// * @date 2020/5/11 12:19
// */
//@Slf4j
//public class WxProgramAuthenticationProvider implements AuthenticationProvider {
//
//    private VerifyCodeService verifyCodeService;
//
//    private UserService userService;
//
//
//    public WxProgramAuthenticationProvider(UserService userService , VerifyCodeService verifyCodeService) {
//        this.userService = userService;
//        this.verifyCodeService = verifyCodeService;
//    }
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        SMSAuthenticationToken authenticationToken = (SMSAuthenticationToken) authentication;
//        String mobile = (String) authenticationToken.getPrincipal();
//        String code =  authenticationToken.getCode();
//        String appType = authenticationToken.getAppType();
//        boolean check = verifyCodeService.checkVerifyCode(appType,mobile
//                , code);
//        if(!check){
//            throw new BadCredentialsException(ResultEnum.验证码已过期.name());
//        }
//        org.springframework.security.core.userdetails.UserDetails userDetails = userService.loadUserByUsername(appType,mobile);
//        if(null == userDetails){
//            throw new BadCredentialsException(ResultEnum.尚未注册.name());
//        }
//        SMSAuthenticationToken authenticationResult = new SMSAuthenticationToken(userDetails);
//        return authenticationResult;
//    }
//
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        // 判断 authentication 是不是 SMSAuthenticationToken 的子类或子接口
//        return authentication.isAssignableFrom(SMSAuthenticationToken.class);
//    }
//}
