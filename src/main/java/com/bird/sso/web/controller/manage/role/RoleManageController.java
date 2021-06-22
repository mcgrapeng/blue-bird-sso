package com.bird.sso.web.controller.manage.role;

import com.google.common.collect.Maps;
import com.bird.RES;
import com.bird.sso.conts.Constants;
import com.bird.sso.domain.SysRole;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.service.CommonService;
import com.bird.sso.service.RoleManageService;
import com.bird.sso.service.RoleQueryService;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.ValidList;
import com.bird.sso.web.controller.manage.ResourceStatusForm;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 11:21
 */
@RequestMapping("/sso/manage/role")
@Controller
public class RoleManageController extends BaseController {

    @Autowired
    private RoleManageService roleManageService;

    @Autowired
    private RoleQueryService roleQueryService;

    @Autowired
    private CommonService commonService;

    /**
     * 新增角色
     *
     * @param roles
     * @return
     */
    @RequestMapping(value = "/add-role", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> addRole(@RequestBody @Validated(UserRoleForm.RoleAdd.class) ValidList<UserRoleForm> roles) {
        roleManageService.addInfo(SysRole.transform(roles.getList()));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 更新角色
     *
     * @param roles
     * @return
     */
    @RequestMapping(value = "/upd-role", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> updRole(@RequestBody @Validated(UserRoleForm.RoleUpd.class) ValidList<UserRoleForm> roles) {
        roleManageService.updateRole(SysRole.transform(roles.getList()));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 获取角色
     *
     * @param sid
     * @return
     */
    @RequestMapping(value = "/get-role/{appType}/{sid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<SysRole> getRole(@PathVariable String appType, @PathVariable Long sid) {
        return RES.of(ResultEnum.处理成功.code, roleQueryService.findRole(sid, appType), ResultEnum.处理成功.name());
    }

    /**
     * 删除角色
     *
     * @param sid
     * @return
     */
    @RequestMapping(value = "/del-role/{appType}/{sid}", method = RequestMethod.DELETE)
    @ResponseBody
    public RES<String> delRole(@PathVariable String appType, @PathVariable Long sid) {
        roleManageService.delInfo(appType, sid);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 角色列表
     *
     * @return
     */
    @RequestMapping(value = "/page-role", method = RequestMethod.GET)
    @ResponseBody
    public RES<PageBean<SysRole>> pageRole(
            @RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "clientType", required = false) String clientType
            , @RequestParam(value = "type", required = false) String type
            , @RequestParam(value = "roleType", required = false) String roleType
            , @RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "isAvailable", required = false) String isAvailable
            , @RequestParam(value = "isDefault", required = false) String isDefault
            , @RequestParam(value = "pageNum", required = false) Integer pageNum
            , @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("type", type);
        params.put("roleType", roleType);
        if (StringUtils.isNotBlank(name)) {
            params.put("name", StringUtils.join("%", name, "%"));
        }
        params.put("isAvailable", isAvailable);
        params.put("isDefault", isDefault);
        return RES.of(ResultEnum.处理成功.code, roleManageService.page(new PageParam(pageNum, pageSize)
                , params), ResultEnum.处理成功.name());
    }


    /**
     * 生成代码
     *
     * @return
     */
    @RequestMapping(value = "/produce-code/{appType}", method = RequestMethod.GET)
    @ResponseBody
    public RES<String> produceCode(@PathVariable String appType) {
        return RES.of(ResultEnum.处理成功.code, commonService.produceCode(Constants.RedisPrefix.SYS_ROLE_CODE,"R", appType), ResultEnum.处理成功.name());
    }


    /**
     * 设置默认
     *
     * @param appType
     * @param sid
     * @return
     */
    @RequestMapping(value = "/set-default/{appType}/{sid}", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> setDefaultRole(@PathVariable String appType, @PathVariable Long sid) {
        roleManageService.setDefault(sid, appType);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 启用/停用角色
     *
     * @param resourceStatus
     * @return
     */
    @RequestMapping(value = "/change-status/{appType}/{sid}", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> changeStatus(@RequestBody @Validated ResourceStatusForm resourceStatus) {
        roleManageService.changeStatus(resourceStatus.getSid(), resourceStatus.getAppType(), resourceStatus.getIsAvailable());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }
}
