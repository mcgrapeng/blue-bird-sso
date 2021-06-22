package com.bird.sso.web.controller.manage.range;

import com.bird.RES;
import com.bird.sso.enums.DataScopeEnum;
import com.bird.sso.service.CommonService;
import com.bird.sso.service.OrganizeQueryService;
import com.bird.sso.service.RangeManageService;
import com.bird.sso.service.RoleManageService;
import com.bird.sso.web.conts.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/12/8 16:09
 */
@Slf4j
@RequestMapping("/sso/manage/range")
@Controller
public class RangeManageController {


    @Autowired
    private RangeManageService rangeManageService;

    @Autowired
    private OrganizeQueryService organizeQueryService;

    @Autowired
    private RoleManageService roleManageService;

    @Autowired
    private CommonService commonService;

    /**
     * 分配数据权限（所有）
     *
     * @param userRangeForm
     * @return
     */
    @RequestMapping(value = "/assign-all-permission", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> assignAllDataPermission(@RequestBody @Validated(UserRangeForm.Range1.class) UserRangeForm userRangeForm) {
        rangeManageService.assignDataPermission(DataScopeEnum.ALL.name(), userRangeForm);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 分配数据权限(本部门)
     *
     * @return
     */
    @RequestMapping(value = "/assign-only-current-permission", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> assignOnlyCurrentDataPermission(@RequestBody @Validated(UserRangeForm.Range1.class) UserRangeForm userRangeForm) {
        rangeManageService.assignDataPermission(DataScopeEnum.DEP.name(), userRangeForm);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 分配数据权限(本部门及以下部门)
     *
     * @return
     */
    @RequestMapping(value = "/assign-current-permission", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> assignCurrentDataPermission(@RequestBody @Validated(UserRangeForm.Range2.class) UserRangeForm userRangeForm) {
        rangeManageService.assignDataPermission(DataScopeEnum.DER.name(), userRangeForm);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 分配数据权限(自定义)
     *
     * @return
     */
    @RequestMapping(value = "/assign-custom-permission", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> assignCustomDataPermission(@RequestBody @Validated(UserRangeForm.Range3.class) UserRangeForm userRangeForm) {
        rangeManageService.assignDataPermission(DataScopeEnum.CUS.name(), userRangeForm);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 当前角色拥有的数据权限(sid)
     *
     * @param sid
     * @return
     */
    @RequestMapping(value = "/has-permission/{sid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<Long>> hasDataPermission(@PathVariable Long sid) {
        return RES.of(ResultEnum.处理成功.code, rangeManageService.hasDataPermission(sid), ResultEnum.处理成功.name());
    }


    /**
     * 当前角色拥有的数据权限(code)
     *
     * @param code
     * @return
     */
    @RequestMapping(value = "/has-permission/code/{code}", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<Long>> hasDataPermission(@PathVariable String code) {
        return RES.of(ResultEnum.处理成功.code, rangeManageService.hasDataPermission(code), ResultEnum.处理成功.name());
    }


    /**
     * 初始化单位管理员
     *
     * @param pid（直机关id）
     * @return
     */
    /*@RequestMapping(value = "/init-unit/{pid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<Long>> initUnitRole(@PathVariable Long pid) {

        List<SysOrganization> organizations = organizeQueryService.listChildByPid(pid);
        List<SysRole> roles = Lists.newArrayList();
        for (SysOrganization unit : organizations) {
            List<SysOrganization> union = organizeQueryService.listUnionByPid(unit.getAppType(), unit.getSid());
            List<Long> orgIds = union.stream().map(t -> t.getSid()).collect(Collectors.toList());
            SysRole unitRole = new SysRole();
            unitRole.setSid(SnowflakeIdWorker.build(8L).nextId());
            unitRole.setAppType(unit.getAppType());
            unitRole.setPid(0L);
            unitRole.setName(StringUtils.join(unit.getName(), "单位管理员"));
            unitRole.setCode(commonService.produceCode(Constants.RedisPrefix.SYS_ROLE_CODE, "R", unit.getAppType()));
            unitRole.setOrgs(JSON.toJSONString(orgIds));
            unitRole.setType(RoleTypeEnum.NORMAL.name());
            roles.add(unitRole);
        }

        roleManageService.addInfo(roles);

        log.info(">>>>>>>>>>初始化完成>>>>>>initUnitRole>>>>>>>>roles={}", JSON.toJSONString(roles));

        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }*/


    /**
     * 初始化支部管理员
     *
     * @param pid（直机关id）
     * @return
     */
    /*@RequestMapping(value = "/init-branch/{pid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<Long>> initBranchRole(@PathVariable Long pid) {
        List<SysRole> roles = Lists.newArrayList();
        List<SysOrganization> organizations = organizeQueryService.listChildByPid(pid);
        if (CollectionUtils.isNotEmpty(organizations)) {
            for (SysOrganization unit : organizations) {
                List<SysOrganization> branchs = organizeQueryService.listChildByPid(unit.getSid());
                if (CollectionUtils.isNotEmpty(branchs)) {
                    for (SysOrganization branch : branchs) {
                        SysRole unitRole = new SysRole();
                        unitRole.setSid(SnowflakeIdWorker.build(8L).nextId());
                        unitRole.setAppType(branch.getAppType());
                        unitRole.setPid(0L);
                        unitRole.setName(StringUtils.join(branch.getName(), "支部管理员"));
                        String r = commonService.produceCode(Constants.RedisPrefix.SYS_ROLE_CODE, "R", branch.getAppType());
                        unitRole.setCode(r);
                        unitRole.setOrgs(JSON.toJSONString(Lists.newArrayList(branch.getSid())));
                        unitRole.setType(RoleTypeEnum.NORMAL.name());
                        unitRole.setRoleType(RoleEnum.GOV_ROLE.name());
                        roles.add(unitRole);
                    }
                }
            }
        }
        roleManageService.addInfo(roles);
        log.info(">>>>>>>>>>初始化完成>>>>>>initBranchRole>>>>>>>>roles={}", JSON.toJSONString(roles));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }*/


}
