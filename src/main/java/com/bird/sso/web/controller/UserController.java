package com.bird.sso.web.controller;

import com.bird.common.mq.birdKafkaExecutor;
import com.bird.common.mq.config.ProfileProperties;
import com.bird.common.mq.message.CommonMessage;
import com.bird.RES;
import com.bird.sso.api.KafkaTopics;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.UserStatusEnum;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.OrganizeQueryService;
import com.bird.sso.service.UserManageService;
import com.bird.sso.service.UserQueryService;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.UserExtend;
import com.bird.sso.web.conts.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 11:20
 */
@Controller
@RequestMapping("/sso/user")
public class UserController extends BaseController {

    @Autowired
    private UserManageService userManageService;

    @Autowired
    private OrganizeQueryService organizeQueryService;

    @Autowired
    private birdKafkaExecutor kafkaExecutor;

    @Autowired
    private ProfileProperties profileProperties;

    @Autowired
    private UserQueryService userQueryService;


    /**
     * 获取个人基本信息
     *
     * @return
     */
    @RequestMapping(value = "/get-user-info", method = RequestMethod.GET)
    @ResponseBody
    public RES<SSOUser> getUserInfo() {
        SysUser sysUser = userQueryService.findUserByUserId(WebUtils.getSSOUser().getAppType()
                , WebUtils.getSSOUser().getUserId());
        SSOUser user = new SSOUser();
        user.setUserName(sysUser.getUserName());
        user.setUserId(sysUser.getUserId());
        user.setRealName(sysUser.getRealName());
        user.setOrgId(sysUser.getOrgId());
        user.setOrgName(sysUser.getOrgName());
        user.setParentOrgName(sysUser.getParentOrgName());
        user.setHeadImg(sysUser.getHeadImg());
        user.setStatus(sysUser.getStatus());
        user.setRoleCodes(sysUser.getAuthority().listRoleCode());
        return RES.of(ResultEnum.处理成功.code, user, ResultEnum.处理成功.name());
    }


    /**
     * 修改个人基本信息
     *
     * @return
     */
    @RequestMapping(value = "/upd-user-info", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> updUserInfo(@RequestBody UserExtend userExtend) {
        SysUser u = new SysUser();
        u.setAppType(WebUtils.getSSOUser().getAppType());
        u.setUserId(WebUtils.getSSOUser().getUserId());
        u.setNickName(userExtend.getNickName());
        u.setRealName(userExtend.getRealName());
        u.setUserMobi(userExtend.getUserMobi());
        u.setHeadImg(userExtend.getHeadImg());
        u.setOrgId(userExtend.getOrgId());
        u.setOrgName(userExtend.getOrgName());
        u.setUserEmail(userExtend.getUserEmail());
        u.setEditor(JWTHelper.getUsername(JWTHelper.getAuthorization()));
        userManageService.updateUser(u);


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(8).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance().addData("orgId", userExtend.getOrgId())
                                .addData("parentOrgId", userExtend.getParentOrgId())
                                .addData("realName", userExtend.getRealName())
                                .addData("orgName", userExtend.getOrgName())
                                .addData("parentOrgName", userExtend.getParentOrgName())
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_USER_INFO_UPDATE, u.getAppType());

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 上传头像
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload/head-img", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> uploadFile(MultipartFile file) {
        userManageService.updateUserHeadImg(file);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 组织列表(子组织ID)（所属组织是支部）
     *
     * @return
     */
    @RequestMapping(value = "/list-child-org", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<Long>> listChildOrg() {
        String authorization = JWTHelper.getAuthorization();
        String appType = JWTHelper.getAppType(authorization);
        Long userId = JWTHelper.getUserId(authorization);
        SysUser user = userQueryService.findUserByUserId(appType, userId);
        return RES.of(ResultEnum.处理成功.code, organizeQueryService
                        .listSidBySid(user.getOrgId())
                , ResultEnum.处理成功.name());
    }


    /**
     * 组织列表(子组织ID)(所属组织是单位)
     *
     * @return
     */
    @RequestMapping(value = "/list-parent-org", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<Long>> listParentOrg() {
        String authorization = JWTHelper.getAuthorization();
        String appType = JWTHelper.getAppType(authorization);
        Long userId = JWTHelper.getUserId(authorization);
        SysUser user = userQueryService.findUserByUserId(appType, userId);
        return RES.of(ResultEnum.处理成功.code, organizeQueryService
                        .listSidByPid(user.getOrgId())
                , ResultEnum.处理成功.name());
    }


    /**
     * 激活用户
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/active", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> activeUser() {
        userManageService.updateUserStatus(UserStatusEnum.ACTIVE.name());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 冻结用户
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/freeze", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> freezeUser() {
        userManageService.updateUserStatus(UserStatusEnum.DEL.name());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }
}
