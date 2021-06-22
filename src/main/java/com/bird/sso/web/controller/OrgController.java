package com.bird.sso.web.controller;

import com.bird.RES;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.service.OrganizeQueryService;
import com.bird.sso.web.BaseController;
import com.bird.sso.web.conts.ResultEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 11:23
 */
@RequestMapping("/sso/organization")
@Controller
public class OrgController extends BaseController {

    @Autowired
    private OrganizeQueryService organizeQueryService;


    /**
     * 组织树
     *
     * @return
     */
    @RequestMapping(value = "/tree", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysOrganization>> tree(@RequestParam(value = "isRoot", required = false) Boolean isRoot) {
        if (ObjectUtils.isEmpty(isRoot)) throw SSOException.PARAM_ERR;
        return RES.of(ResultEnum.处理成功.code, organizeQueryService.tree(isRoot), ResultEnum.处理成功.name());
    }


    /**
     * 当前用户所属组织下的二级组织列表（包含当前组织）（组织树）
     *
     * @return
     */
    @RequestMapping(value = "/user/tree", method = RequestMethod.GET)
    @ResponseBody
    public RES<List<SysOrganization>> userTree() {
        return RES.of(ResultEnum.处理成功.code, organizeQueryService.userTree(), ResultEnum.处理成功.name());
    }

}
