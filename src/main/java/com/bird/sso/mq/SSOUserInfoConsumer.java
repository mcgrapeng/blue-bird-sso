package com.bird.sso.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bird.common.mq.SSOUserConsumer;
import com.bird.common.mq.message.CommonMessage;
import com.bird.sso.api.domain.SSORole;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.UserStatusEnum;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.RoleManageService;
import com.bird.sso.service.UserAuthorityService;
import com.bird.sso.service.UserManageService;
import com.bird.sso.service.UserQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2021/5/31 15:46
 */
@Component
@Slf4j
public class SSOUserInfoConsumer extends SSOUserConsumer {

    @Autowired
    private UserManageService userManageService;

    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder passwordEncoder;


    @Override
    public void createUser(SSOUser ssoUser) {
        log.info(">>>>>>>>>>>>SSOUserInfoConsumer_createUser>>>>>>>>>>>>>>>>>ssoUser={}", JSON.toJSONString(ssoUser));
        SysUser sysUser = new SysUser();
        sysUser.setUserId(ssoUser.getUserId());
        sysUser.setAppType(ssoUser.getAppType());
        sysUser.setOrgId(ssoUser.getOrgId());
        sysUser.setHeadImg(ssoUser.getHeadImg());
        sysUser.setRealName(ssoUser.getRealName());
        sysUser.setUserName(ssoUser.getUserName());
        sysUser.setUserMobi(ssoUser.getUserMobi());
        sysUser.setStatus(UserStatusEnum.ACTIVE.name());
        sysUser.setOrgName(ssoUser.getOrgName());
        sysUser.setParentOrgName(ssoUser.getParentOrgName());
        sysUser.setUserPass(passwordEncoder.encode(ssoUser.getUserMobi()));

        userManageService.createUser(sysUser);

        userAuthorityService.assignDefaultAuthorities(ssoUser.getAppType()
                , ssoUser.getUserId());
    }

    @Override
    public void updateUserSafe(String appType, long userId, SSOUser.SSOUserSafe userSafe) {

    }

    @Override
    public void updateUserRole(String appType, long userId, List<SSORole> role) {

    }

    @Override
    public void updateUserStatus(String appType, long userId, String status) {
        log.info(">>>>>>>>>>SSOUserInfoConsumer_updateUserStatus>>>>>>>>>>>appType={},userId={},status={}", appType, userId, status);
        SysUser user = new SysUser();
        user.setAppType(appType);
        user.setUserId(userId);
        user.setStatus(status);
        userManageService.updateUser(user);
    }

    @Override
    public void updateUserInfo(String appType, long userId, SSOUser.SSOUserInfo userInfo) {
        log.info(">>>>>>>>>>SSOUserInfoConsumer_updateUserInfo>>>>>>>>>>>userInfo={}", JSON.toJSONString(userInfo));
        SysUser user = new SysUser();
        user.setAppType(appType);
        user.setOrgId(userInfo.getOrgId());
        user.setHeadImg(userInfo.getHeadImg());
        user.setRealName(userInfo.getRealName());
        user.setUserId(userId);
        user.setUserMobi(userInfo.getUserMobi());
        user.setOrgName(userInfo.getOrgName());
        user.setParentOrgName(userInfo.getParentOrgName());
        userManageService.updateUser(user);
    }
}
