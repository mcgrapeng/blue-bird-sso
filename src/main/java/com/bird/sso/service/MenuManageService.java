package com.bird.sso.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.core.TreeHandler;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysRole;
import com.bird.sso.enums.MenuTypeEnum;
import com.bird.sso.mapper.MenuMapper;
import com.bird.sso.mapper.RoleMapper;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 18:13
 */
@Slf4j
@Service
public class MenuManageService extends AbsManageService<MenuMapper, SysMenu> {


    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private UserQueryService userQueryService;

    @Autowired
    private RoleQueryService roleQueryService;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    protected String getName() {
        return "资源信息";
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(List<SysMenu> data) {
        if (CollectionUtils.isEmpty(data)) return;

        super.updInfo(data);

        executor.execute(() -> {

            String appType = data.get(0).getAppType();
            //获取当前系统所有角色
            List<SysRole> roles
                    = roleQueryService.listAuthoritiesRoleContainMenus(appType);
            if (CollectionUtils.isEmpty(roles)) return;

            for (SysRole r : roles) {
                List<SysMenu> perms = r.listPermission();
                List<SysMenu> filter = perms.stream().filter(t -> data.contains(t))
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(filter)
                        && perms.removeAll(filter)) {
                    perms.addAll(data);
                }
                r.coverMenu(perms);
                roleMapper.update(r);
            }
        });
    }


    /**
     * 删除权限后，重新设置角色授予的权限
     *
     * @param sids 删除的权限ID
     */
    @Override
    protected void getAuthoritiesAndSet(String appType, List<Long> sids) {
        log.info(">>>>>>>>>>>>>>>>>>>>>" + getName() +
                        ">>>getAuthoritiesAndSet start >>>>>>>>>>>>>>>>>>>>>> sids = {} "
                , JSON.toJSONString(sids));
        if (CollectionUtils.isEmpty(sids)) {
            return;
        }

        //获取当前系统所有角色
        List<SysRole> roles
                = roleQueryService.listAuthoritiesRoleContainMenus(appType);

        for (SysRole r : roles) {
            List<SysMenu> perms = r.listPermission();
            if (CollectionUtils.isNotEmpty(perms)) {
                for (Long sid : sids) {

                    TreeHandler of = TreeHandler.of();
                    List<SysMenu> nodes = of.getChildNodeContainSelf(perms, sid);

                    if (CollectionUtils.isNotEmpty(nodes)) {
                        perms.removeAll(nodes);
                    }

                    of.destroy();
                }
                r.coverMenu(perms);
                roleMapper.update(r);
            }
        }
    }


    @Override
    public PageBean<SysMenu> page(PageParam pageParam, Map<String, Object> params) {
        PageBean<SysMenu> page = super.page(pageParam, params);
        return page;
    }

    /**
     * 交换顺序
     *
     * @param appType
     * @param sidUp
     * @param sidDw
     * @param sortUp
     * @param sortDw
     */
    @Transactional(rollbackFor = Exception.class)
    public void swapSort(String appType, long sidUp, int sortUp, long sidDw, int sortDw) {
        Map<String, Object> d = Maps.newHashMap();

        d.put("appType", appType);
        d.put("sid", sidUp);
        d.put("ord", sortDw);
        mapper.updateBy(d);

        d.clear();

        d.put("appType", appType);
        d.put("sid", sidDw);
        d.put("ord", sortUp);
        mapper.updateBy(d);
    }


    /**
     * 关联关系
     *
     * @param appType
     * @param sid
     * @param pid
     */
    @Transactional(rollbackFor = Exception.class)
    public void relationMenu(String appType, long sid, long pid, int pLevel) {
        Map<String, Object> d = Maps.newHashMap();
        d.put("appType", appType);
        d.put("sid", sid);
        d.put("pid", pid);
        if (pid == 0) {
            d.put("isClick", PublicEnum.N.name());
            d.put("level", 1);
        } else {
            d.put("isClick", PublicEnum.Y.name());
            d.put("level", pLevel + 1);
        }
        mapper.updateRele(d);
    }


    /**
     * 修改资源状态
     *
     * @param appType
     * @param sid
     * @param isAvailable
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateResourceStatus(String appType, long sid, String isAvailable) {
        SysMenu menu = this.findInfoBySid(appType, sid);
        if (ObjectUtils.isEmpty(menu)) return;
        Map<String, Object> d = Maps.newHashMap();
        d.put("appType", appType);
        d.put("sid", sid);
        d.put("isAvailable", isAvailable);
        mapper.updateRele(d);


        if (isAvailable.equals(PublicEnum.N.name())) {
            executor.execute(() -> {
                //清理角色中的菜单
                getAuthoritiesAndSet(appType, Lists.newArrayList(sid));
            });

        }
    }
}
