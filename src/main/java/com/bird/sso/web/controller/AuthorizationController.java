package com.bird.sso.web.controller;

import com.bird.RES;
import com.bird.sso.api.domain.SSORole;
import com.bird.sso.core.auth.jwt.JWTHelper;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.service.AuthorityContext;
import com.bird.sso.service.RangeManageService;
import com.bird.sso.service.UserAuthorityService;
import com.bird.sso.utils.WebUtils;
import com.bird.sso.web.conts.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 授权
 *
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 18:14
 */
@RequestMapping("/sso/authorize")
@Controller
public class AuthorizationController {


    @Autowired
    private AuthorityContext authorityContext;

    @Autowired
    private UserAuthorityService userAuthorityService;

    @Autowired
    private RangeManageService dataScopeManageService;

    /**
     * 权限树 (拥有全部角色的权限树)
     *
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/has-authorities", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysMenu>> hasAuthorities() {
        return RES.of(ResultEnum.处理成功.code, authorityContext.hasAuthorities(AuthorityContext.Strategy.USER), ResultEnum.处理成功.name());
    }


    /**
     * 根据不同角色返回权限树
     * sid  角色ID
     *
     * @return
     */
    @RequestMapping(value = "/has-authorities/{sid}", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysMenu>> hasAuthorities(@PathVariable Long sid) {
        return RES.of(ResultEnum.处理成功.code, authorityContext.hasAuthorities(AuthorityContext.Strategy.USER, sid), ResultEnum.处理成功.name());
    }


    /**
     * 返回客户端拥有的菜单
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysMenu>> authorities() {
        String loginSource = JWTHelper.getLoginSource();
        String appType = WebUtils.getSSOUser().getAppType();
        return RES.of(ResultEnum.处理成功.code, authorityContext.hasAuthorities(AuthorityContext.Strategy.USER, appType, loginSource), ResultEnum.处理成功.name());
    }


    /**
     * 获取角色（不区分客户端）
     *
     * @return
     */
    @RequestMapping(value = "/has-role", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysRole>> hasRole() {
        return RES.of(ResultEnum.处理成功.code, userAuthorityService.hasAuthorities(WebUtils.getSSOUser().getAppType()
                , WebUtils.getSSOUser().getUserId()), ResultEnum.处理成功.name());
    }


    /**
     * 获取角色（自动适配区分客户端）
     *
     * @return
     */
    @RequestMapping(value = "/has-fit-role", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysRole>> hasFitRole() {
        return RES.of(ResultEnum.处理成功.code, userAuthorityService.hasAuthorities(WebUtils.getSSOUser().getAppType()
                , WebUtils.getSSOUser().getUserId(), JWTHelper.getLoginSource()), ResultEnum.处理成功.name());
    }


    /**
     * 获取角色
     *
     * @return
     */
    @RequestMapping(value = "/has-snapshot-role", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SSORole>> hasSnapshotRole() {
        return RES.of(ResultEnum.处理成功.code, JWTHelper.hasRole(JWTHelper.getAuthorization()),
                ResultEnum.处理成功.name());
    }


    /**
     * 获取角色（区分客户端）
     *
     * @param clientType
     * @return
     */
    @RequestMapping(value = "/has-role/{clientType}", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysRole>> hasRole(@PathVariable String clientType) {
        return RES.of(ResultEnum.处理成功.code, userAuthorityService.hasAuthorities(WebUtils.getSSOUser().getAppType()
                , WebUtils.getSSOUser().getUserId(), clientType), ResultEnum.处理成功.name());
    }


    @Deprecated
    @RequestMapping(value = "/role/has-role", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysRole>> hasAuthRole() {
        return RES.of(ResultEnum.处理成功.code, userAuthorityService.hasAuthorities(WebUtils.getSSOUser().getAppType()
                , WebUtils.getSSOUser().getUserId()), ResultEnum.处理成功.name());
    }

}
