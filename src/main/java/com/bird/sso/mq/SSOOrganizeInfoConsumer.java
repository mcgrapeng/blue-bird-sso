package com.bird.sso.mq;

import com.alibaba.fastjson.JSON;
import com.bird.common.mq.SSOOrganizationConsumer;
import com.bird.sso.api.domain.SSOOrganize;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.OrganizeManageService;
import com.bird.sso.service.UserManageService;
import com.bird.sso.service.UserQueryService;
import com.bird.sso.thread.ContextAwarePoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SSOOrganizeInfoConsumer extends SSOOrganizationConsumer {

    @Autowired
    private OrganizeManageService organizeManageService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserManageService userManageService;


    @Autowired
    protected ContextAwarePoolExecutor executor;

    @Override
    public void createOrganize(SSOOrganize organize) {
        log.info(">>>>>>>>>>>>SSOOrganizeInfoConsumer_createOrganize>>>>>>>>>>>>>>>>>organize={}", JSON.toJSONString(organize));
        SysOrganization organization = new SysOrganization();
        organization.setSid(organize.getSid());
        organization.setAppType(organize.getAppType());
        organization.setName(organize.getOrgName());
        organization.setPid(organize.getPid());
        organization.setParentName(organize.getParentOrgName());
        organization.setIsAvailable(PublicEnum.Y.name());
        organizeManageService.createOrganize(organization);
    }

    @Override
    public void updateOrganize(SSOOrganize organize) {
        log.info(">>>>>>>>>>>SSOOrganizeInfoConsumer_updateOrganize>>>>>>>>>>>>>>>>organize={}", JSON.toJSONString(organize));
        SysOrganization organization = new SysOrganization();
        organization.setAppType(organize.getAppType());
        organization.setSid(organize.getSid());
        organization.setName(organize.getOrgName());
        organization.setPid(organize.getPid());
        organization.setParentName(organize.getParentOrgName());
        organization.setIsAvailable(organize.getIsAvailable());
        organizeManageService.updateOrganize(organization);


        executor.execute(() -> {
            List<SysUser> users = userQueryService
                    .listUserByOrgId(organize.getAppType(), organize.getSid());
            if (CollectionUtils.isNotEmpty(users)) {
                users.stream().forEach(t -> {
                    SysUser u = new SysUser();
                    u.setAppType(t.getAppType());
                    u.setUserId(t.getUserId());
                    u.setOrgName(organize.getOrgName());
                    u.setParentOrgName(organize.getParentOrgName());
                    userManageService.updateUser(u);
                });
            }
        });

    }
}
