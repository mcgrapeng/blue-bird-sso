package com.bird.sso.web.controller.manage.user;

import com.google.common.collect.Maps;
import com.bird.common.mq.birdKafkaExecutor;
import com.bird.common.mq.config.ProfileProperties;
import com.bird.common.mq.message.CommonMessage;
import com.bird.RES;
import com.bird.sso.api.KafkaTopics;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.UserSourceEnum;
import com.bird.sso.api.enums.UserStatusEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysUser;
import com.bird.sso.domain.SysUserVersion;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.service.AuthorityContext;
import com.bird.sso.service.UserManageService;
import com.bird.sso.service.UserQueryService;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.UserAssign;
import com.bird.sso.web.ValidList;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 11:20
 */
@Controller
@RequestMapping(value = "/sso/manage/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserManageController extends BaseController {

    @Autowired
    private UserManageService userManageService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private birdKafkaExecutor kafkaExecutor;

    @Autowired
    private AuthorityContext authorityContext;

    @Autowired
    private ProfileProperties profileProperties;


    /**
     * 添加账号
     *
     * @param userBasic
     * @return
     */
    @RequestMapping(value = "/add-basic-user", method = RequestMethod.POST)
    @ResponseBody
    public RES<Long> createUserInfo(@RequestBody @Validated(value = {UserBasicForm.UserBasicAdd.class}) UserBasicForm userBasic) {

        SysUser user = userQueryService.findUserByUserName(userBasic.getAppType(), userBasic.getUsername());
        if (ObjectUtils.isNotEmpty(user)) {
            throw SSOException.USER_EXIST;
        }

        long userId = userManageService.createUser(
                userBasic.getAppType(),
                userBasic.getUserType(),
                userBasic.getUsername(),
                userBasic.getPassword(),
                userBasic.getOrgId(),
                userBasic.getOrgName(),
                null,
                null,
                null,
                null,
                UserSourceEnum.WEB.name());

        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(2).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", userId)
                                .addData("userName", userBasic.getUsername())
                                .addData("orgId", userBasic.getOrgId())
                                .addData("orgName", userBasic.getOrgName())
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_REGISTER, userBasic.getAppType());

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return RES.of(ResultEnum.处理成功.code, userId, ResultEnum.处理成功.name());
    }


    /**
     * 修改账号
     *
     * @param userBasic
     * @return
     */
    @RequestMapping(value = "/upd-basic-user", method = RequestMethod.PUT)
    @ResponseBody
    public RES<SSOUser> updateBasicUserInfo(@RequestBody @Validated(value = {UserBasicForm.UserBasicUpd.class}) UserBasicForm userBasic) {

        SysUser user = userQueryService
                .findUserByUserId(userBasic.getAppType(), userBasic.getUserId());
        if (ObjectUtils.isEmpty(user)) {
            throw SSOException.USER_NO_EXITS;
        }
        userManageService.updateUserName(user.getAppType()
                , user.getUserId(), userBasic.getUsername(), userBasic.getPassword());

        if (!user.getUserName().equals(userBasic.getUsername())) {
            userManageService.userVersion(userBasic.getAppType(), userBasic.getUserId()
                    , userBasic.getOldUsername(), userBasic.getUsername(), userBasic.getRealName());
        }

        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 添加用户信息
     *
     * @param userInfo
     * @return
     */
    @RequestMapping(value = "/add-user", method = RequestMethod.POST)
    @ResponseBody
    public RES<Long> addUserInfo(@RequestBody @Validated(value = {UserInfoForm.UserInfoAdd.class}) UserInfoForm userInfo) {

        SysUser user = userQueryService.findUserByUserName(userInfo.getAppType(), userInfo.getUsername());
        if (ObjectUtils.isNotEmpty(user)) {
            throw SSOException.USER_EXIST;
        }

        long userId = userManageService.createUser(
                userInfo.getAppType(),
                userInfo.getUserType(),
                userInfo.getUsername(),
                userInfo.getPassword(),
                userInfo.getOrgId(),
                userInfo.getOrgName(),
                userInfo.getNickName(),
                userInfo.getUserEmail(),
                userInfo.getRealName(),
                userInfo.getHeadImg(),
                UserSourceEnum.WEB.name());

        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(2).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", userId)
                                .addData("userName", userInfo.getUsername())
                                .addData("realName", userInfo.getRealName())
                                .addData("nickName", userInfo.getNickName())
                                .addData("orgId", userInfo.getOrgId())
                                .addData("orgName", userInfo.getOrgName())
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_REGISTER, userInfo.getAppType());

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return RES.of(ResultEnum.处理成功.code, userId, ResultEnum.处理成功.name());
    }

    /**
     * 修改用户信息
     *
     * @param userInfo
     * @return
     */
    @RequestMapping(value = "/upd-user", method = RequestMethod.PUT)
    @ResponseBody
    public RES<SSOUser> updateUserInfo(@RequestBody @Validated(value = {UserInfoForm.UserInfoUpd.class}) UserInfoForm userInfo) {

        SysUser user = userQueryService
                .findUserByUserId(userInfo.getAppType(), userInfo.getUserId());
        if (ObjectUtils.isEmpty(user)) {
            throw SSOException.USER_NO_EXITS;
        }

        SysUser u = new SysUser();
        u.setAppType(userInfo.getAppType());
        u.setUserId(userInfo.getUserId());
        u.setNickName(userInfo.getNickName());
        u.setRealName(userInfo.getRealName());
        u.setHeadImg(userInfo.getHeadImg());
        u.setOrgId(userInfo.getOrgId());
        u.setOrgName(userInfo.getOrgName());
        u.setUserEmail(userInfo.getUserEmail());
        u.setEditor(WebUtils.getSSOUser().getUserName());
        userManageService.updateUser(u);


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(2).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("userId", userInfo.getUserId())
                                .addData("realName", userInfo.getRealName())
                                .addData("nickName", userInfo.getNickName())
                                .addData("orgId", userInfo.getOrgId())
                                .addData("orgName", userInfo.getOrgName())
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_INFO_UPDATE, userInfo.getAppType());

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 激活用户
     *
     * @return
     */
    @RequestMapping(value = "/act-user/{appType}/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> activeUser(@PathVariable Long userId, @PathVariable String appType) {
        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(userId)) {
            throw SSOException.PARAM_ERR;
        }
        userManageService.updateUserStatus(appType, userId, UserStatusEnum.ACTIVE.name());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 冻结用户
     *
     * @return
     */
    @RequestMapping(value = "/fre-user/{appType}/{userId}", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> freezeUser(@PathVariable Long userId, @PathVariable String appType) {

        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(userId)) {
            throw SSOException.PARAM_ERR;
        }

        userManageService.updateUserStatus(appType, userId, UserStatusEnum.DEL.name());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 用户列表
     *
     * @return
     */
    @RequestMapping(value = "/page-user", method = RequestMethod.GET)
    @ResponseBody
    public RES<PageBean<SysUser>> pageUser(
            @RequestParam(value = "userName", required = false) String userName
            , @RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "userType", required = false) String userType
            , @RequestParam(value = "userSource", required = false) String userSource
            , @RequestParam(value = "userStatus", required = false) String userStatus
            , @RequestParam(value = "pageNum", required = false) Integer pageNum
            , @RequestParam(value = "pageSize", required = false) Integer pageSize) {


        Map<String, Object> params = Maps.newHashMap();
        params.put("userName", userName);
        params.put("userType", userType);
        params.put("userSource", userSource);
        params.put("status", userStatus);
        params.put("appType", appType);
        PageBean<SysUser> page = userQueryService.page(new PageParam(pageNum, pageSize)
                , params);
        return RES.of(ResultEnum.处理成功.code, page, ResultEnum.处理成功.name());
    }


    /**
     * 账号变更轨迹
     *
     * @return
     */
    @RequestMapping(value = "/list-user-version", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysUserVersion>> listUserVersion(
            @RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "userId", required = false) Long userId) {


        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(userId)) {
            throw SSOException.PARAM_ERR;
        }

        return RES.of(ResultEnum.处理成功.code
                , userQueryService.listUserVersion(appType, userId), ResultEnum.处理成功.name());
    }


    /**
     * 分配角色（追加）
     *
     * @param roles
     * @return
     */
    @RequestMapping(value = "/assign-role/{appType}/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> assignUserRole(@PathVariable String appType, @PathVariable Long userId,
                                      @RequestBody @Validated ValidList<UserAssign> roles) {
        authorityContext.assignAuthorities(AuthorityContext.Strategy.USER, roles.getList(), appType, userId, Boolean.FALSE);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 分配角色(覆盖)
     *
     * @param roles
     * @return
     */
    @RequestMapping(value = "/assign-cover-role/{appType}/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> assignCoverUserRole(@PathVariable String appType, @PathVariable Long userId,
                                           @RequestBody @Validated ValidList<UserAssign> roles) {
        authorityContext.assignCoverAuthorities(AuthorityContext.Strategy.USER, roles.getList(), appType, userId);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 批量用户授予批量角色(覆盖)
     *
     * @param appType
     * @param assigns
     * @return
     */
    @RequestMapping(value = "/assign-cover-role", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> assignUserRole(@RequestParam(value = "appType", required = false) String appType,
                                      @RequestBody @Validated UserAssignBatchForm assigns) {
        authorityContext.assignBatchCoverAuthorities(AuthorityContext.Strategy.USER, assigns, appType);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }

}
