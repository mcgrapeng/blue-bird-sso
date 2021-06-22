package com.bird.sso.web.controller.manage.menu;

import com.google.common.collect.Maps;
import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.conts.Constants;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.enums.MenuTypeEnum;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.service.CommonService;
import com.bird.sso.service.MenuManageService;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.ValidList;
import com.bird.sso.web.controller.manage.ResourceStatusForm;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 11:22
 */
@RequestMapping("/sso/manage/menu")
@Controller
public class MenuManageController extends BaseController {

    @Autowired
    private MenuManageService menuManageService;


    @Autowired
    private CommonService commonService;

    /**
     * 新增菜单
     *
     * @param menus
     * @return
     */
    @RequestMapping(value = "/add-menu", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> addMenu(@RequestBody
                               @Validated(UserResourceForm.MenuAdd.class) ValidList<UserResourceForm> menus) {
        menuManageService.addInfo(SysMenu.transform(menus.getList()));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 新增按钮
     *
     * @param opers
     * @return
     */
    @RequestMapping(value = "/add-oper", method = RequestMethod.POST)
    @ResponseBody
    public RES<String> addOper(@RequestBody @Validated(UserResourceForm.OperAdd.class) ValidList<UserResourceForm> opers) {
        menuManageService.addInfo(SysMenu.transform(opers.getList()));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 更新菜单
     *
     * @param menus
     * @return
     */
    @RequestMapping(value = "/upd-menu", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> updMenu(@RequestBody @Validated(UserResourceForm.MenuUpd.class) ValidList<UserResourceForm> menus) {
        menuManageService.updateMenu(SysMenu.transform(menus.getList()));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 更新按钮
     *
     * @param opers
     * @return
     */
    @RequestMapping(value = "/upd-oper", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> updOper(@RequestBody @Validated(UserResourceForm.OperUpd.class) ValidList<UserResourceForm> opers) {
        menuManageService.updateMenu(SysMenu.transform(opers.getList()));
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 获取资源
     *
     * @param sid
     * @return
     */
    @RequestMapping(value = "/get-menu/{appType}/{sid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<SysMenu> getMenu(@PathVariable String appType, @PathVariable Long sid) {
        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(sid)) {
            throw SSOException.PARAM_ERR;
        }
        return RES.of(ResultEnum.处理成功.code, menuManageService.findInfoBySid(appType, sid), ResultEnum.处理成功.name());
    }


    /**
     * 删除资源
     *
     * @param sid  菜单sid
     * @return
     */
    @RequestMapping(value = "/del-menu/{appType}/{sid}", method = RequestMethod.DELETE)
    @ResponseBody
    public RES<String> delMenu(@PathVariable String appType, @PathVariable Long sid) {
        if (StringUtils.isBlank(appType) || ObjectUtils.isEmpty(sid)) {
            throw SSOException.PARAM_ERR;
        }
        menuManageService.delInfo(appType, sid);
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 菜单列表
     * @param name  菜单名称
     * @param status  菜单状态
     * @param clientType  客户端类型
     * @param appType  应用类型
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/page-menu", method = RequestMethod.GET)
    @ResponseBody
    public RES<PageBean<SysMenu>> pageMenu(@RequestParam(value = "name", required = false) String name
            , @RequestParam(value = "isAvailable", required = false) String status
            , @RequestParam(value = "clientType", required = false) String clientType
            , @RequestParam(value = "appType", required = false) String appType
            , @RequestParam(value = "pageNum", required = false) Integer pageNum
            , @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("clientType", clientType);
        params.put("appType", appType);
        params.put("isAvailable", status);
        if (StringUtils.isNotBlank(name)) {
            params.put("name", StringUtils.join("%", name, "%"));
        }
        return RES.of(ResultEnum.处理成功.code, menuManageService.page(new PageParam(pageNum, pageSize), params), ResultEnum.处理成功.name());
    }


    /**
     * 生成代码
     *
     * @return
     */
    @RequestMapping(value = "/produce-code/{appType}/{sid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<String> produceCode(@PathVariable String appType, @PathVariable Long sid) {
        return RES.of(ResultEnum.处理成功.code, commonService.produceCode(Constants.RedisPrefix.SYS_MENU_CODE,"M", appType), ResultEnum.处理成功.name());
    }


    /**
     * 获取某个业务下某个级别的菜单列表
     *
     * @return
     */
    @RequestMapping(value = "/list-menu-level", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysMenu>> listMenu(
            @RequestParam(value = "appType", required = false) String appType,
            @RequestParam(value = "level", required = false) String level) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("level", level);
        params.put("appType", appType);
        params.put("type", MenuTypeEnum.MENU.name());
        return RES.of(ResultEnum.处理成功.code, menuManageService.list(params), ResultEnum.处理成功.name());
    }


    /**
     * 获取某个业务下的菜单列表
     *
     * @return
     */
    @RequestMapping(value = "/list-menu", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysMenu>> listMenu(
            @RequestParam(value = "appType", required = false) String appType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("type", MenuTypeEnum.MENU.name());
        return RES.of(ResultEnum.处理成功.code, menuManageService.list(params), ResultEnum.处理成功.name());
    }


    /**
     * 交换顺序
     *
     * @return
     */
    @RequestMapping(value = "/swap-sort", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> swapSort(@RequestBody @Validated UserResourceForm.ResourceSort resourceSort) {

        if (!resourceSort.getAppTypeUp().equals(resourceSort.getAppTypeDw())) {
            throw SSOException.PARAM_ERR;
        }
        menuManageService.swapSort(resourceSort.getAppTypeUp(), resourceSort.getSidUp(), resourceSort.getSortUp()
                , resourceSort.getSidDw(), resourceSort.getSortDw());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }


    /**
     * 关联关系
     *
     * @return
     */
   /* @RequestMapping(value = "/relation-menu", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> relationMenu(@RequestBody @Validated UserResourceForm.ResourceRelation resourceRelation) {
        if (!resourceRelation.getAppTypePid().equals(resourceRelation.getAppTypeSid())) {
            throw SSOException.PARAM_ERR;
        }
        menuManageService.relationMenu(resourceRelation.getAppTypeSid(), resourceRelation.getSid()
                , resourceRelation.getPid(), resourceRelation.getPLevel());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }
*/

    /**
     * 启用/停用
     * @param resourceStatus
     * @return
     */
    @RequestMapping(value = "/change-status", method = RequestMethod.PUT)
    @ResponseBody
    public RES<String> statusMenu(@RequestBody @Validated ResourceStatusForm resourceStatus) {
        menuManageService.updateResourceStatus(resourceStatus.getAppType(), resourceStatus.getSid()
                , resourceStatus.getIsAvailable());
        return RES.of(ResultEnum.处理成功.code, ResultEnum.处理成功.name());
    }

}
