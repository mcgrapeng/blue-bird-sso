package com.bird.sso.web.controller.manage.organize;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.common.mq.birdKafkaExecutor;
import com.bird.common.mq.config.ProfileProperties;
import com.bird.common.mq.message.CommonMessage;
import com.bird.RES;
import com.bird.sso.api.KafkaTopics;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.service.OrganizeManageService;
import com.bird.sso.service.OrganizeQueryService;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @date 2020/5/13 11:23
 */
@RequestMapping("/sso/manage/organization")
@Controller
public class OrganizationController extends BaseController {

    @Autowired
    private OrganizeManageService organizationManageService;

    @Autowired
    private OrganizeQueryService organizeQueryService;

    @Autowired
    private birdKafkaExecutor kafkaExecutor;

    @Autowired
    private ProfileProperties profileProperties;


    /**
     * 新增组织
     *
     * @return
     */
    @RequestMapping(value = "/add-org", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> addOrgs(@RequestBody @Validated(value = {OrganizationForm.Add.class})
                                       OrganizationForm form) {

        SysOrganization organization = SysOrganization.transform(form);

        organizationManageService.addInfo(Lists.newArrayList(organization));


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(8).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("sid", form.getSid())
                                .addData("code", form.getCode())
                                .addData("orgName", form.getName())
                                .addData("parentOrgName", form.getParentName())
                                .addData("pid", form.getPid())
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_ORG_BASIC_CREATE, form.getAppType());

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 更新组织
     *
     * @return
     */
    @RequestMapping(value = "/upd-org", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> updOrg(@RequestBody @Validated(value = {OrganizationForm.Upd.class})
                                      OrganizationForm form) {

        SysOrganization organization = SysOrganization.transform(form);

        organizationManageService.updInfo(Lists.newArrayList(organization));


        CommonMessage message = CommonMessage.CommonMessageBuilder.instance()
                .setMsgId(SnowflakeIdWorker.build(8).nextId())
                .setSendTime(Date.from(Instant.now()))
                .setPayload(
                        CommonMessage.Payload.instance()
                                .addData("sid", form.getSid())
                                .addData("orgName", form.getName())
                                .addData("parentOrgName", form.getParentName())
                                .addData("pid", form.getPid())
                ).build();

        String topic = CommonMessage.buildTopic(profileProperties.getActive()
                , KafkaTopics.SSO_ORG_BASIC_UPDATE, form.getAppType());

        kafkaExecutor.sendToKafkaStandardMessageAsync(topic
                , message);

        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 激活组织
     *
     * @return
     */
    @RequestMapping(value = "/available", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> updOrgAvailable(@RequestBody @Validated(value = {OrganizationForm.UpdAvailable.class})
                                               OrganizationForm form) {

        SysOrganization organization = new SysOrganization();
        organization.setAppType(form.getAppType());
        organization.setSid(form.getSid());
        organization.setIsAvailable(form.getIsAvailable());
        organizationManageService.updateOrganize(organization);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 获取组织
     *
     * @param sid
     * @return
     */
    @RequestMapping(value = "/get-org/{sid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<SysOrganization> getOrg(@RequestParam(value = "appType", required = false) String appType
            , @PathVariable Long sid) {

        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(sid)) {
            throw SSOException.NULL;
        }


        return RES.of(ResultEnum.处理成功.code, organizationManageService.findInfoBySid(appType, sid), ResultEnum.处理成功.name());
    }


    /**
     * 删除组织
     *
     * @param sid
     * @return
     */
    @RequestMapping(value = "/del-org/{sid}", method = RequestMethod.DELETE)
    @ResponseBody
    public RES<String> delRole(@RequestParam(value = "appType", required = false) String appType
            , @PathVariable Long sid) {

        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(sid)) {
            throw SSOException.NULL;
        }

        organizationManageService.delInfo(appType, sid);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 角色列表
     *
     * @return
     */
    @RequestMapping(value = "/page-org", method = RequestMethod.GET)
    @ResponseBody
    public RES<PageBean<SysOrganization>> pageOrg(
            @RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "isAvailable", required = false) String isAvailable
            , @RequestParam(value = "pageNum", required = false) Integer pageNum
            , @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        if (StringUtils.isNotBlank(name)) {
            params.put("name", StringUtils.join("%", name, "%"));
        }
        params.put("isAvailable", isAvailable);
        return RES.of(ResultEnum.处理成功.code, organizationManageService.page(new PageParam(pageNum, pageSize)
                , params), ResultEnum.处理成功.name());
    }


    /**
     * 组织列表
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/list-org", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysOrganization>> listOrg(@RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "level", required = false) Integer level) {

        if (ObjectUtils.isEmpty(level)) {
            throw SSOException.NULL;
        }

        return RES.of(ResultEnum.处理成功.code, organizeQueryService.listByLevel(appType, level), ResultEnum.处理成功.name());
    }


    /**
     * 获取上级组织
     *
     * @param appType
     * @param pid
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/get-org", method = RequestMethod.GET)
    @ResponseBody
    public RES<Integer> getOrgLevel(@RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "pid", required = false) Long pid) {
        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(pid)) {
            throw SSOException.NULL;
        }

        SysOrganization organization = organizeQueryService.getBasicOrganization(appType, pid);
        return RES.of(ResultEnum.处理成功.code, (organization.getLevel() + 1), ResultEnum.处理成功.name());
    }

}
