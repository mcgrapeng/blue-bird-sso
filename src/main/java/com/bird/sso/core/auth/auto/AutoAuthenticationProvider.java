package com.bird.sso.core.auth.auto;

import com.bird.common.tools.VerifyCodeService;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.UserTypeEnum;
import com.bird.sso.thread.ContextAwarePoolExecutor;
import com.bird.sso.core.UserDetails;
import com.bird.sso.core.UserService;
import com.bird.sso.core.auth.SSOAuthenticationException;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.UserAuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 12:19
 */
@Slf4j
public class AutoAuthenticationProvider implements AuthenticationProvider {

    private VerifyCodeService verifyCodeService;

    private UserService userService;

    private UserAuthorityService userAuthorityService;

    private ContextAwarePoolExecutor executor;


    public AutoAuthenticationProvider(UserService userService, VerifyCodeService verifyCodeService
            , UserAuthorityService userAuthorityService, ContextAwarePoolExecutor executor) {
        this.userService = userService;
        this.verifyCodeService = verifyCodeService;
        this.userAuthorityService = userAuthorityService;
        this.executor = executor;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AutoAuthenticationToken authenticationToken = (AutoAuthenticationToken) authentication;
        String mobile = (String) authenticationToken.getPrincipal();
        String code = authenticationToken.getCode();
        String appType = authenticationToken.getAppType();

        String verifyCode = verifyCodeService.getVerifyCode(appType, mobile);
        if (!code.equals(verifyCode)) {
            throw SSOAuthenticationException.USER_VER_ERR;
        }

        boolean check = verifyCodeService.checkVerifyCode(appType, mobile
                , code);
        if (!check) {
            throw SSOAuthenticationException.LOGIN_EXPIRE;
        }
        UserDetails userDetails = userService.getUserByUsername(appType, mobile);

        if (ObjectUtils.isEmpty(userDetails)) {

            SysUser user = userService.createUser(
                    appType, UserTypeEnum.USER.name(), mobile, code
            );
            SSOUser loginUser = new SSOUser();
            BeanUtils.copyProperties(user, loginUser);
            userDetails = new UserDetails(loginUser);

            executor.execute(() -> {
                userAuthorityService.assignAuthorities(appType,user.getUserId());
            });
        }

        AutoAuthenticationToken authenticationResult = new AutoAuthenticationToken(userDetails);
        return authenticationResult;
    }


    @Override
    public boolean supports(Class<?> authentication) {
        // 判断 authentication 是不是 AutoAuthenticationToken 的子类或子接口
        return authentication.isAssignableFrom(AutoAuthenticationToken.class);
    }
}
