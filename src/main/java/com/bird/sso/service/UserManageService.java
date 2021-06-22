package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.common.mq.birdKafkaExecutor;
import com.bird.common.mq.config.ProfileProperties;
import com.bird.common.mq.message.CommonMessage;
import com.bird.common.tools.VerifyCodeService;
import com.bird.sso.api.KafkaTopics;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.*;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.conts.Constants;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.domain.SysUser;
import com.bird.sso.domain.SysUserVersion;
import com.bird.sso.mapper.SysUserVersionMapper;
import com.bird.sso.mapper.UserMapper;
import com.bird.sso.thread.ContextAwarePoolExecutor;
import com.bird.sso.utils.RedisUtils;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.utils.UploadUtils;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.utils.rsa.v2.RSA;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 10:53
 */
@Slf4j
@Service
public class UserManageService {

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Qualifier("passwordEncoder")
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private UserQueryService userQueryService;

    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    private RoleQueryService roleQueryService;


    @Autowired
    private ContextAwarePoolExecutor birdThreadPoolTaskExecutor;


    @Autowired
    private UploadUtils uploadUtils;


    @Autowired
    private birdKafkaExecutor kafkaExecutor;


    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ProfileProperties profileProperties;

    @Autowired
    private SysUserVersionMapper userVersionMapper;


    @Transactional(rollbackFor = Exception.class)
    public long userRegisterAndALLAuthorities(String appType, String username, String password, String userType, String vercode) {
        log.info("#########################开始注册" +
                ",userRegisterAndALLAuthorities##########appType={},username={},userType={},vercode={}", appType, username, userType, vercode);

        long userId = userRegister(appType, username, password, userType, vercode);


        birdThreadPoolTaskExecutor.execute(() -> userAuthorityService.assignAuthorities(appType, userId));

        return userId;
    }

    @Transactional(rollbackFor = Exception.class)
    public long userRegisterAndDefaultAuthorities(String appType, String username, String password, String userType, String vercode) {
        log.info("#########################开始注册" +
                ",userRegisterAndDefaultAuthorities##########appType={},username={},userType={},vercode={}", appType, username, userType, vercode);
        long userId = userRegister(appType, username, password, userType, vercode);
        birdThreadPoolTaskExecutor.execute(() -> userAuthorityService.assignDefaultAuthorities(appType, userId));
        return userId;
    }


    @Transactional(rollbackFor = Exception.class)
    public long userRegisterAndAuthorities(String appType, String username, String password
            , String userType, String vercode, List<String> roleCode) {
        log.info("#########################开始注册" +
                ",userRegisterAndAuthorities##########appType={},username={},userType={},vercode={},roleCode={}", appType, username, userType, vercode, JSON.toJSONString(roleCode));
        long userId = userRegister(appType, username, password, userType, vercode);
        if (CollectionUtils.isNotEmpty(roleCode)) {
            birdThreadPoolTaskExecutor.execute(() -> userAuthorityService.assignCoverAuthorities(appType, userId, Lists.newArrayList(roleCode)));
        } else {
            birdThreadPoolTaskExecutor.execute(() -> userAuthorityService.assignDefaultAuthorities(appType, userId));
        }
        return userId;
    }

    /**
     * 注册
     *
     * @param appType
     * @param username
     * @param password
     * @param userType
     * @param vercode
     * @return
     */
    private long userRegister(String appType, String username, String password
            , String userType, String vercode) {

        if (StringUtils.isBlank(verifyCodeService.getVerifyCode(appType, username))) {
            throw new SSOException(ResultEnum.验证码已过期.code, ResultEnum.验证码已过期.name());
        } else if (!verifyCodeService.checkVerifyCode(appType, username, vercode)) {
            log.info("验证码校验失败，请求数据 mobile={},verifyCode={}", username, vercode);
            throw new SSOException(ResultEnum.验证码输入错误.code, ResultEnum.验证码输入错误.name());
        }

        SysUser user = userQueryService.findUserByUserName(appType, username);

        if (ObjectUtils.isNotEmpty(user)) {
            if (user.getStatus().equals(UserStatusEnum.ACTIVE.name())) {
                throw new SSOException(ResultEnum.已注册.code, ResultEnum.已注册.name());
            } else if (user.getStatus().equals(UserStatusEnum.DEL.name())) {
                restoreUser(appType, username
                        , password);
                return user.getUserId();
            }
        }

        long userId = createUser(appType, userType, username, password);


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(5).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", userId)
                                .addData("userName", username)
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_REGISTER, appType);

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return userId;
    }


    /**
     * 创建用户
     *
     * @param appType
     * @param userType
     * @param username
     * @param password
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public long createUser(String appType, String userType, String username
            , String password) {
        SysUser user = buildUser(appType, userType, username
                , password);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
        }
        return user.getUserId();
    }


    /**
     * 创建用户
     *
     * @param appType
     * @param username
     * @param password
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public long createUser(String appType, String username
            , String password) {
        return createUser(appType, UserTypeEnum.USER.name(), username
                , password);
    }



    /**
     * user最简化更新
     * @param user
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        userMapper.update(user);
    }


    /**
     * 修改账号
     *
     * @param appType
     * @param userId
     * @param userName
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserName(String appType, long userId, String userName, String password) {
        if (StringUtils.isBlank(userName) && StringUtils.isBlank(password)) return;

        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("userId", userId);
        params.put("userName", userName);
        params.put("userMobi", userName);
        if (AppEnum.APP_UF.name().equals(appType)) {
            params.put("userPass", passwordEncoder.encode(password));
        } else {
            params.put("userPass", passwordEncoder
                    .encode(RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }
        params.put("editor", WebUtils.getSSOUser().getUserName());
        userMapper.updateUserName(params);

    }


    /**
     * 创建用户
     *
     * @param appType
     * @param userType
     * @param username
     * @param password
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser createAndGetUser(String appType, String userType, String username
            , String password) {
        SysUser user = buildUser(appType, userType, username, password);
        userMapper.insert(user);
        return user;
    }


    /**
     * 修改用户状态
     *
     * @param status
     */
    @Deprecated
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String status) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("appType", WebUtils.getSSOUser().getAppType());
        data.put("userName", WebUtils.getSSOUser().getUserName());
        data.put("status", status);
        data.put("editor", JWTHelper.getUsername(JWTHelper.getAuthorization()));
        userMapper.updateMapByUserName(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String appType, long userId, String status) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("appType", appType);
        data.put("userId", userId);
        data.put("status", status);
        data.put("editor", WebUtils.getSSOUser().getUserName());
        userMapper.updateMapByUserId(data);


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(5).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", userId)
                                .addData("status", status)
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_STATUS_UPDATE, appType);

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

    }


    /**
     * 构建用户
     *
     * @param appType
     * @param userType
     * @param username
     * @param password
     * @return
     */
    private SysUser buildUser(String appType, String userType, String username
            , String password) {
        SysUser user = new SysUser();
        long userId = SnowflakeIdWorker.build(8L).nextId();
        user.setUserId(userId);
        user.setUserName(username);
        user.setUserMobi(username);
        user.setIsBindMobile(PublicEnum.Y.name());
        user.setAppType(appType);
        user.setUserType(StringUtils.isBlank(userType)
                ? UserTypeEnum.USER.name()
                : userType);

        String clientType = WebUtils.getHeader(JWTHelper.CLAIM_KEY_LOGIN_SOURCE);
        user.setUserSource(clientType.equals(ClientEnum.PC.name()) ? ClientEnum.WEB.name()
                : clientType);
        if (AppEnum.APP_UF.name().equals(appType)) {
            user.setUserPass(passwordEncoder.encode(password));
        }else {
            user.setUserPass(passwordEncoder
                    .encode(RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }

        user.setCreateTime(Date.from(Instant.now()));
        user.setCreator(username);
        user.setLoginTimes(0);
        user.setStatus(UserStatusEnum.ACTIVE.name());
        return user;
    }


    /**
     * 恢复用户
     *
     * @param appType
     * @param username
     * @param password
     */
    private void restoreUser(String appType, String username
            , String password) {
        SysUser user = new SysUser();
        user.setUserName(username);
        user.setAppType(appType);
        if (AppEnum.APP_UF.name().equals(appType)) {
            user.setUserPass(passwordEncoder.encode(password));
        }else {
            user.setUserPass(passwordEncoder
                    .encode(RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }

        user.setUpdateTime(Date.from(Instant.now()));
        user.setStatus(UserStatusEnum.ACTIVE.name());
        user.setEditor(username);

        userMapper.updateByUserName(user);
    }


    public long createUser(SysUser user) {
        userMapper.insert(user);
        return user.getUserId();
    }


    /**
     * 创建用户
     *
     * @param appType
     * @param userType
     * @param username
     * @param password
     * @param orgId
     * @param nickName
     * @param userEmail
     * @param realName
     * @param headImg
     * @return
     */
    public long createUser(
            String appType,
            String userType,
            String username,
            String password,
            Long orgId,
            String orgName,
            String nickName,
            String userEmail,
            String realName,
            String headImg,
            String userSource) {

        SysUser user = new SysUser();
        long userId = SnowflakeIdWorker.build(8L).nextId();
        user.setUserId(userId);
        user.setUserName(username);
        user.setUserMobi(username);
        user.setIsBindMobile(PublicEnum.Y.name());
        user.setAppType(appType);
        user.setOrgId(orgId);
        user.setOrgName(orgName);
        user.setNickName(nickName);
        user.setUserEmail(userEmail);
        user.setRealName(realName);
        user.setHeadImg(headImg);
        user.setUserSource(userSource);
        user.setUserType(StringUtils.isBlank(userType)
                ? UserTypeEnum.USER.name()
                : userType);
        user.setStatus(UserStatusEnum.ACTIVE.name());
        if (AppEnum.APP_UF.name().equals(appType)) {
            user.setUserPass(passwordEncoder.encode(password));
        } else {
            user.setUserPass(passwordEncoder
                    .encode(RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }
        user.setUserId(userId);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
        }
        return userId;
    }


    @Transactional(rollbackFor = Exception.class)
    public void bindMobile(String mobile, String vercode) {
        bindMobile(BindMobileMethod.VERCODE.name(), mobile, vercode);
    }


    /**
     * 修改手机号
     *
     * @param method     修改方式（验证码、密码）
     * @param mobile
     * @param credential
     */
    @Transactional(rollbackFor = Exception.class)
    public void bindMobile(String method, String mobile, String credential) {
        log.info(">>>>>>>>>>>>>>>>>>>GET INTO bindMobile>>>>>>>>>>>  mobile ={} ,credential = {}"
                , mobile, credential);

        SSOUser loginUser = WebUtils.getSSOUser();
        String appType = WebUtils.getSSOUser().getAppType();
        String username = loginUser.getUserName();
        SysUser user = userQueryService.findUserByUserName(appType, username);
        if (ObjectUtils.isEmpty(user)) {
            log.error(">>>>>>>>>>>>>>>>修改的用户信息不存在~");
            throw new SSOException(ResultEnum.尚未注册.code, ResultEnum.尚未注册.name());
        }

        if (method.equals(BindMobileMethod.VERCODE.name())) {

            if (StringUtils.isBlank(verifyCodeService.getVerifyCode(appType, mobile))) {
                throw new SSOException(ResultEnum.验证码已过期.code, ResultEnum.验证码已过期.name());
            } else if (!verifyCodeService.checkVerifyCode(appType, mobile, credential)) {
                log.error(">>>>>>>>>>>>>>>>用户验证码校验失败~");
                throw new SSOException(ResultEnum.验证码输入错误.code, ResultEnum.验证码输入错误.name());
            }
        } else {
            if (AppEnum.APP_UF.name().equals(appType)) {
                if (!passwordEncoder.matches(credential, user.getUserPass())) {
                    log.error(">>>>>>>>>>>>>>>>用户密码校验失败~");
                    throw new SSOException(ResultEnum.密码输入错误.code, ResultEnum.密码输入错误.name());
                }
            }else {
                if (!passwordEncoder.matches(RSA.decrypt(credential, RSA.getPrivateKey(RSA.PRIVATE_KEY))
                        , user.getUserPass())) {
                    log.error(">>>>>>>>>>>>>>>>用户密码校验失败~");
                    throw new SSOException(ResultEnum.密码输入错误.code, ResultEnum.密码输入错误.name());
                }
            }
        }

        if (loginUser.getUserName().equals(mobile)
                || isExistBindMobile(appType, mobile)) {
            throw new SSOException(ResultEnum.该手机已被绑定.code, ResultEnum.该手机已被绑定.name());
        }

        updateUserSafeMobile(user.getUserId(), user.getAppType(), mobile);


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(5).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", user.getUserId())
                                .addData("userMobi", mobile)
                                .addData("userName", mobile)
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_SAFE_UPDATE, appType);

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);
    }


    public boolean checkOriginalPassword(String password) {
        log.info(">>>>>>>>>>>>>>>>>>>GET INTO checkOriginalPassword>>>>>>>>>>>  ,credential = {}"
                , password);
        SSOUser loginUser = WebUtils.getSSOUser();
        SysUser u = userQueryService.findUserByUserId(loginUser.getAppType()
                , loginUser.getUserId());
        if (null == u) {
            log.error("##################checkOriginalPassword######################,loginUser={}",
                    JSON.toJSONString(loginUser));
            throw SSOException.USER_NO_EXITS;
        }

        if (AppEnum.APP_UF.name().equals(loginUser.getAppType())) {
            if (!passwordEncoder.matches(password, u.getUserPass())) {
                log.error(">>>>>>>>>>>>>>>>用户密码校验失败~");
                return Boolean.FALSE;
            }
        } else {
            if (!passwordEncoder.matches(RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY))
                    , u.getUserPass())) {
                log.error(">>>>>>>>>>>>>>>>用户密码校验失败~");
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }


    /**
     * 是否绑定
     *
     * @param appType
     * @param newMobile
     * @return
     */
    private boolean isExistBindMobile(String appType, String newMobile) {
        SysUser user = userQueryService.findUserByUserName(appType, newMobile);
        return null != user;
    }

    /**
     * 用户密码修改
     *
     * @param newPassword
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void userPasswordForget(String appType, String username, String newPassword) {
        log.info("#########################忘记密码" +
                ",userPasswordForget##########appType={},username={},newPassword={}", appType, username, newPassword);
        SysUser user = userQueryService.findUserByUserName(appType, username);
        if (null == user) {
            log.error(">>>>>>>>>>>>>>>>修改的用户信息不存在~");
            throw new SSOException(ResultEnum.尚未注册.code, ResultEnum.尚未注册.name());
        }
        Map<String, Object> data = Maps.newHashMap();
        if (AppEnum.APP_UF.name().equals(appType)) {
            data.put("userPass", passwordEncoder.encode(newPassword));
        } else {
            data.put("userPass", passwordEncoder
                    .encode(RSA.decrypt(newPassword, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }

        data.put("updateTime", Date.from(Instant.now()));
        data.put("userId", user.getUserId());
        data.put("appType", user.getAppType());
        data.put("editor", username);

        userMapper.updateMapByUserId(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public void userPasswordForget(String appType, String username, String newPassword, String verifyCode) {
        log.info("#########################忘记密码" +
                ",userPasswordForget##########appType={},username={},newPassword={},verifyCode={}", appType, username, newPassword, verifyCode);
        if (!verifyCodeService.checkVerifyCode(appType, username, verifyCode)) {
            log.error(">>>>>>>>>>>>>>>>用户验证码校验失败~");
            throw new SSOException(ResultEnum.验证码已过期.code, ResultEnum.验证码已过期.name());
        }

        SysUser user = userQueryService.findUserByUserName(appType, username);
        if (null == user) {
            log.error(">>>>>>>>>>>>>>>>修改的用户信息不存在~");
            throw new SSOException(ResultEnum.尚未注册.code, ResultEnum.尚未注册.name());
        }
        Map<String, Object> data = Maps.newHashMap();
        if (AppEnum.APP_UF.name().equals(appType)) {
            data.put("userPass", passwordEncoder.encode(newPassword));
        }else {
            data.put("userPass", passwordEncoder
                    .encode(RSA.decrypt(newPassword, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }

        data.put("updateTime", Date.from(Instant.now()));
        data.put("userId", user.getUserId());
        data.put("appType", user.getAppType());
        data.put("editor", username);

        userMapper.updateMapByUserId(data);
    }


    /**
     * 用户密码修改
     *
     * @param newPassword
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void userPasswordUpdate(String password, String newPassword) {
        log.info("#########################修改密码" +
                ",userPasswordForget##########password={},newPassword={},verifyCode={}", password, newPassword);
        SSOUser user = WebUtils.getSSOUser();
        SysUser u = userQueryService.findUserByUserId(user.getAppType(), user.getUserId());

        if (AppEnum.APP_UF.name().equals(user.getAppType())) {
            if (!passwordEncoder.matches(password, u.getUserPass())) {
                throw new SSOException(ResultEnum.原密码输入错误.code, ResultEnum.原密码输入错误.name());
            }

        }else {
            if (!passwordEncoder.matches(RSA.decrypt(password, RSA.getPrivateKey(RSA.PRIVATE_KEY))
                    , u.getUserPass())) {
                throw new SSOException(ResultEnum.原密码输入错误.code, ResultEnum.原密码输入错误.name());
            }
        }

        Map<String, Object> data = Maps.newHashMap();
        if (AppEnum.APP_UF.name().equals(user.getAppType())) {
            data.put("userPass", passwordEncoder.encode(newPassword));
        }else {
            data.put("userPass", passwordEncoder
                    .encode(RSA.decrypt(newPassword, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }

        data.put("updateTime", Date.from(Instant.now()));
        data.put("userId", user.getUserId());
        data.put("appType", user.getAppType());
        data.put("editor", WebUtils.getSSOUser().getUserName());

        userMapper.updateMapByUserId(data);
    }


    @Transactional(rollbackFor = Exception.class)
    public void userVerifyCodePasswordUpdate(String verifyCode, String newPassword) {
        log.info("#########################修改密码" +
                ",userVerifyCodePasswordUpdate##########newPassword={},verifyCode={}", newPassword, verifyCode);

        SSOUser user = WebUtils.getSSOUser();
        String userName = user.getUserName();
        String appType = user.getAppType();

        if (StringUtils.isBlank(verifyCodeService.getVerifyCode(appType, userName))) {
            throw new SSOException(ResultEnum.验证码已过期.code, ResultEnum.验证码已过期.name());
        } else if (!verifyCodeService.checkVerifyCode(appType, userName, verifyCode)) {
            log.error(">>>>>>>>>>>>>>>>用户验证码校验失败~");
            throw new SSOException(ResultEnum.验证码已过期.code, ResultEnum.验证码已过期.name());
        }

        Map<String, Object> data = Maps.newHashMap();
        if (AppEnum.APP_UF.name().equals(user.getAppType())) {
            data.put("userPass", passwordEncoder.encode(newPassword));
        }else {
            data.put("userPass", passwordEncoder
                    .encode(RSA.decrypt(newPassword, RSA.getPrivateKey(RSA.PRIVATE_KEY))));
        }

        data.put("updateTime", Date.from(Instant.now()));
        data.put("userId", user.getUserId());
        data.put("appType", user.getAppType());
        data.put("editor", WebUtils.getSSOUser().getUserName());

        userMapper.updateMapByUserId(data);
    }


    /**
     * 当前账号是否存在
     *
     * @param appType
     * @param username
     * @return
     */
    public boolean isUserExists(String appType, String username) {
        SysUser u = userQueryService.findUserByUserName(appType, username);
        return null == u ? Boolean.FALSE : Boolean.TRUE;
    }


    /**
     * 上传头像
     *
     * @param file
     */
    public void updateUserHeadImg(MultipartFile file) {
        String key = uploadUtils.uploadImage(Constants.SSO_HEAD_IMG_PREFIX, file);
        log.info("#########################上传头像" +
                ",updateUserHeadImg##########key={}", key);
        Map<String, Object> data = Maps.newHashMap();
        String appType = WebUtils.getSSOUser().getAppType();
        Long userId = WebUtils.getSSOUser().getUserId();
        data.put("appType", appType);
        data.put("userId", userId);
        data.put("headImg", key);
        data.put("editor", JWTHelper.getUsername(JWTHelper.getAuthorization()));

        userMapper.updateMapByUserId(data);


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(5).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", userId)
                                .addData("headImg", key)
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_INFO_UPDATE, appType);

        log.info(">>>>>>>>>>updateUserHeadImg>>>>>>>>>>>topic={},message={}", topic, JSON.toJSONString(message));
        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);
    }


    /**
     * 修改安全手机
     *
     * @param userId
     * @param appType
     * @param mobile
     */
    private void updateUserSafeMobile(long userId, String appType, String mobile) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("userId", userId);
        data.put("appType", appType);
        data.put("userMobi", mobile);
        data.put("userName", mobile);
        data.put("editor", WebUtils.getSSOUser().getUserName());
        userMapper.updateUserName(data);
    }


    @Transactional(rollbackFor = Exception.class)
    public void userVersion(String appType,
                            long userId,
                            String oldUserName,
                            String newUserName,
                            String realName) {
        SysUserVersion userVersion = new SysUserVersion();
        userVersion.setAppType(appType);
        userVersion.setUserId(userId);
        userVersion.setNewUserName(newUserName);
        userVersion.setOldUserName(oldUserName);
        userVersion.setRealName(realName);
        try {
            userVersionMapper.insert(userVersion);
        } catch (DuplicateKeyException e) {
        }
    }

    public enum BindMobileMethod {
        VERCODE,
        PASSWORD;
    }
}
