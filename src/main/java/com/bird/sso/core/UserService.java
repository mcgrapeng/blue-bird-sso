package com.bird.sso.core;

import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.enums.UserStatusEnum;
import com.bird.sso.api.enums.UserTypeEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.core.auth.SSOAuthenticationException;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.UserMapper;
import com.bird.sso.service.UserQueryService;
import com.bird.sso.utils.SnowflakeIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/4/21 19:53
 */
@Slf4j
@Component
public class UserService {


    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private UserQueryService userQueryService;


    @Autowired
    private UserMapper userMapper;


    private PasswordEncoder passwordEncoder;

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserDetails loadUserByUserId(String appType, long userId) {
        SysUser user = userQueryService.findUserByUserId(appType, userId);
        if (user == null) {
            throw SSOException.DB_SELECTONE_IS_NULL;
        }
        SSOUser loginUser = new SSOUser();
        BeanUtils.copyProperties(user, loginUser);
        return new UserDetails(loginUser);
    }


    public UserDetails loadUserByUsername(String appType, String username) {
        SysUser user;
        try {
            user = userQueryService.findUserByUserName(appType, username);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw SSOAuthenticationException.USER_DUPLICATE;
        }

        if (ObjectUtils.isEmpty(user)
                || user.getStatus().equals(UserStatusEnum.DEL.name())) {
            log.error("############loadUserByUsername###############appType={},username={}"
                    , appType, username);
            throw SSOAuthenticationException.USER_NO_EXITS;
        }

        SSOUser loginUser = new SSOUser();
        BeanUtils.copyProperties(user, loginUser);
        return new UserDetails(loginUser);
    }


    public UserDetails getUserByUsername(String appType, String username) {
        SysUser user;
        try {
            user = userQueryService.findUserByUserName(appType, username);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw SSOAuthenticationException.USER_DUPLICATE;
        }

        if (ObjectUtils.isNotEmpty(user) && !user.getStatus().equals(UserStatusEnum.DEL.name())) {
            log.info("############loadUserByUsername###############appType={},username={}"
                    , appType, username);
            SSOUser loginUser = new SSOUser();
            BeanUtils.copyProperties(user, loginUser);
            return new UserDetails(loginUser);
        }
        return null;
    }


    public SysUser createUser(String appType, String userType, String username
            , String password) {
        SysUser user = new SysUser();
        user.setUserId(SnowflakeIdWorker.build(7L).nextId());
        user.setUserName(username);
        user.setUserMobi(username);
        user.setIsBindMobile(PublicEnum.Y.name());
        user.setAppType(appType);
        user.setUserType(StringUtils.isBlank(userType)
                ? UserTypeEnum.USER.name()
                : userType);
        user.setUserPass(passwordEncoder.encode(password));
        user.setCreateTime(Date.from(Instant.now()));
        user.setLoginTimes(0);
        user.setStatus(UserStatusEnum.ACTIVE.name());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
        }

        return user;
    }
}
