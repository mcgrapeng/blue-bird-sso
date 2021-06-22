package com.bird.sso.service;

import com.bird.sso.api.domain.SSOOrganize;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.mapper.OrganizationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 16:20
 */
@Slf4j
@Service
public class OrganizeManageService
        extends AbsManageService<OrganizationMapper, SysOrganization> {


    @Autowired
    private OrgAuthorityService orgAuthorityService;


    @Autowired
    private OrganizeQueryService organizeQueryService;


    @Override
    protected String getName() {
        return "组织信息";
    }



    @Transactional(rollbackFor = Exception.class)
    public void createOrganize(SysOrganization sysOrganization){
        sysOrganization.setCreateTime(Date.from(Instant.now()));
        mapper.insert(sysOrganization);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrganize(SysOrganization sysOrganization){
        sysOrganization.setUpdateTime(Date.from(Instant.now()));
        mapper.update(sysOrganization);
    }


    /**
     * 删除组织后，重新设置用户授予的组织
     */
    @Override
    protected void getAuthoritiesAndSet(String appType, List<Long> sids) {
        return;
    }


}
