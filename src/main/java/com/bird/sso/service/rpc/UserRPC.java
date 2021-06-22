package com.bird.sso.service.rpc;

import com.google.common.collect.Maps;
import com.bird.sso.api.IUserService;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.enums.UserStatusEnum;
import com.bird.sso.api.enums.UserTypeEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.UserMapper;
import com.bird.sso.service.UserManageService;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/7/23 10:18
 */
@org.apache.dubbo.config.annotation.Service(interfaceName = "com.bird.sso.api.IUserService", protocol = "dubbo", version = "1.0", retries = 3
        , timeout = 60000, loadbalance = "random", executes = 200, actives = 0, cluster = "failover")
@Slf4j
public class UserRPC implements IUserService {

    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private com.bird.sso.service.UserQueryService userQueryService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private UserMapper userMapper;


    @Autowired
    private UserManageService userManageService;

    @Override
    public long createSSOUserByPermitAll(String appType, String username) {
        return createSSOUser(appType, UserTypeEnum.USER.name(), PublicEnum.Y.name(), username, "111111");
    }

    @Override
    public long createSSOUser(String appType, String username) {
        return createSSOUser(appType, UserTypeEnum.USER.name(), username);
    }

    @Override
    public long createAdminSSOUser(String appType, String username) {
        return /*createSSOUser(appType, UserTypeEnum.ADMIN_USER.name(), username);*/ 0L;
    }

    @Override
    public long createSSOUser(String appType, String userType, String username) {
        return createSSOUser(appType, userType, PublicEnum.N.name(), username, null);
    }

    @Override
    public long createSSOUser(String appType, String userType, String username, String password) {
        return createSSOUser(appType, userType, PublicEnum.N.name(), username, password);
    }

    @Override
    public void syncMobile(String appType, long userId, String mobile) {
        SysUser u = new SysUser();
        u.setAppType(appType);
        u.setUserId(userId);
        u.setUserMobi(mobile);
        userMapper.update(u);
    }


    @Override
    public void deleteSSOUser(String appType, String userName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userName", userName);
        params.put("appType", appType);
        params.put("status", UserStatusEnum.DEL.name());
        userMapper.updateMapByUserName(params);
    }

    @Override
    public void activeSSOUser(String appType, String userName) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userName", userName);
        params.put("appType", appType);
        params.put("status", UserStatusEnum.ACTIVE.name());
        userMapper.updateMapByUserName(params);
    }

    public void restoreUser(String appType, String username
            , String password) {
        SysUser user = new SysUser();
        user.setUserName(username);
        user.setAppType(appType);
        user.setUserPass(passwordEncoder.encode(password));
        user.setUpdateTime(Date.from(Instant.now()));
        user.setStatus(UserStatusEnum.ACTIVE.name());
        user.setEditor(username);

        userMapper.updateByUserName(user);
    }

    private long createSSOUser(String appType, String userType, String isPermitAll, String username, String password) {
        SysUser user = userQueryService.findUserByUserName(appType, username);


        if (ObjectUtils.isNotEmpty(user)) {
            if (user.getStatus().equals(UserStatusEnum.DEL.name())) {
                restoreUser(appType, username
                        , password);
                return user.getUserId();
            } else {
                throw new SSOException(ResultEnum.已注册.code, ResultEnum.已注册.name());
            }
        }

        user = new SysUser();
        long userId = SnowflakeIdWorker.build(6).nextId();
        user.setUserId(userId);
        user.setUserName(username);
        user.setUserMobi(username);
        user.setIsBindMobile(PublicEnum.Y.name());
        user.setAppType(appType);
        user.setUserType(StringUtils.isBlank(userType) ? UserTypeEnum.USER.name() : userType);

        if (StringUtils.isBlank(password)) {
            user.setUserPass(passwordEncoder.encode(username));
        } else {
            user.setUserPass(passwordEncoder.encode(password));
        }

        user.setCreateTime(Date.from(Instant.now()));
        user.setLoginTimes(0);
        user.setStatus(UserStatusEnum.ACTIVE.name());
        user.setIsPermitAll(isPermitAll);

        userMapper.insert(user);

        return userId;
    }
}
