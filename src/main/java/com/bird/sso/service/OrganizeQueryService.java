package com.bird.sso.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysOrganization;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.OrganizationMapper;
import com.bird.sso.utils.CommonUtils;
import com.bird.sso.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/18 16:30
 */
@Service
@Slf4j
public class OrganizeQueryService {

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private OrganizeTreeHandler organizeTreeHandler;


    @Autowired
    private SpecOrganizeTreeHandler specOrganizeTreeHandler;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private UserQueryService userQueryService;


    public List<SysOrganization> listAuthoritiesRole(String appType) {
        return organizationMapper.selectHasRoles(appType);
    }


    public List<SysOrganization> listByAppType(String appType) {
        return organizationMapper.selectByAppType(appType);
    }


    public List<SysOrganization> listBasicAll() {
        return organizationMapper.selectBasicByAppType(null);
    }


    public List<SysOrganization> listAll() {
        return organizationMapper.selectBy(null);
    }


    public List<SysOrganization> listByLevel(String appType, int level) {
        return organizationMapper.selectByAppType_Level(appType, level);
    }


    public List<SysOrganization> listUnionByPid(String appType, long pid) {
        return organizationMapper.listUnionByPid(appType, pid);
    }


    public List<SysOrganization> listByRootId(String appType, long rootId) {
        return organizationMapper.listByRootId(appType, rootId);
    }


    /**
     * 获取pid下的子组织
     *
     * @param appType
     * @param pid           根节点条件： 父ID
     * @param isContainRoot 是否包含根节点
     * @return
     */
    public List<SysOrganization> tree(String appType, long pid, boolean isContainRoot) {

//        String json = RedisUtils.get(stringRedisTemplate
//                , String.format(Constants.RedisPrefix.SYS_DATA_ORGANIZE, appType)
//                , String.class);
//
//        if (StringUtils.isNotBlank(json)) {
//            return JSONArray.parseArray(json, UserOrganize.class);
//        }

        List<SysOrganization> data = listByAppType(appType);

        if (CollectionUtils.isEmpty(data)) return Lists.newArrayList();

        List<SysOrganization> tree;
        if (pid > 0) {
            SpecOrganizeTreeHandler.SpecOrganizeTreeBuilder build = specOrganizeTreeHandler.build(data);
            tree = build.builTree(pid);
        } else {
            OrganizeTreeHandler.OrganizeTreeBuilder build = organizeTreeHandler.build(data);
            tree = build.builTree();
        }

        List<SysOrganization> filter;
        if (!isContainRoot) {
            filter = tree.get(0).getChildrens();
        } else {
            filter = tree;
        }

//        RedisUtils.set(stringRedisTemplate
//                , String.format(Constants.RedisPrefix.SYS_DATA_ORGANIZE, appType), JSON.toJSONString(orgs));

        return filter;
    }


    /**
     * 我的子组织
     *
     * @return
     */
    public List<SysOrganization> userTree() {

        String appType = WebUtils.getSSOUser().getAppType();

//        String json = RedisUtils.get(stringRedisTemplate
//                , String.format(Constants.RedisPrefix.SYS_DATA_USER_ORGANIZE, appType)
//                , String.class);
//
//        if (StringUtils.isNotBlank(json)) {
//            return JSONArray.parseArray(json, UserOrganize.class);
//        }

        Long orgId = WebUtils.getSSOUser().getOrgId();
        if (ObjectUtils.isEmpty(orgId)) {
            SysUser user = userQueryService.findUserByUserId(appType
                    , WebUtils.getSSOUser().getUserId());
            orgId = user.getOrgId();
        }

        List<SysOrganization> data = listUnionByPid(appType, orgId);

        if (CollectionUtils.isEmpty(data)) return Lists.newArrayList();


//        RedisUtils.set(stringRedisTemplate
//                , String.format(Constants.RedisPrefix.SYS_DATA_USER_ORGANIZE, appType), JSON.toJSONString(orgs));

        return organizeTreeHandler.build(data).builTree();
    }


    /**
     * @param isContainRoot 是否包含当前组织
     * @return
     */
    public List<SysOrganization> tree(boolean isContainRoot) {
        return tree(WebUtils.getSSOUser().getAppType(), 0, isContainRoot);
    }


    /**
     * 根据用户获取组织
     *
     * @return
     */
    public SysOrganization getBasicOrganizationByUserId() {
        return getBasicOrganization(WebUtils.getSSOUser().getAppType(), WebUtils.getSSOUser().getOrgId());
    }


    /**
     * 获取子组织
     *
     * @param sid
     * @return
     */
    private List<SysOrganization> getBasicOrganizationByPid(String appType, long sid) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pid", sid);
        params.put("appType", appType);
        List<SysOrganization> sysOrganizations = organizationMapper.selectBy(params);
        if (CollectionUtils.isEmpty(sysOrganizations)) return Lists.newArrayList();

        return sysOrganizations;
    }


    /**
     * 获取组织基本信息
     *
     * @param appType
     * @param orgId
     * @return
     */
    public SysOrganization getBasicOrganization(String appType, long orgId) {
        SysOrganization org = getOrganization(appType, orgId);
        if (org == null) {
            throw SSOException.ORG_IS_MISSING;
        }
        return null;
    }


    /**
     * 获取父级组织名称
     *
     * @param pid
     * @return
     */
    public String getParentOrganizationNameByPid(String appType, long pid) {
        String parentOrgName = getOrganization(appType, pid).getName();
        if (pid == 0) {
            parentOrgName = StringUtils.isBlank(parentOrgName) ? "顶级组织" : parentOrgName;
        }
        return parentOrgName;
    }


    /**
     * 获取子组织(当前sid是二级部门)
     *
     * @param sid
     * @return
     */
    public List<SysOrganization> listChildBySid(Long sid) {
        String appType = WebUtils.getSSOUser().getAppType();
        SysOrganization organization = getOrganization(appType, sid);
        if (ObjectUtils.isEmpty(organization)) return Lists.newArrayList();
        Map<String, Object> params = Maps.newHashMap();
        params.put("pid", organization.getPid());
        params.put("appType", appType);
        return organizationMapper.selectBy(params);
    }


    /**
     * 获取子组织(当前sid为一级单位)
     *
     * @param sid
     * @return
     */
    public List<SysOrganization> listChildByPid(Long sid) {
        String appType = WebUtils.getSSOUser().getAppType();
        Map<String, Object> params = Maps.newHashMap();
        params.put("pid", sid);
        params.put("appType", appType);
        return organizationMapper.selectBy(params);
    }


    public List<Long> listSidBySid(Long sid) {
        List<SysOrganization> organizations = listChildBySid(sid);
        return organizations.stream().map(t -> t.getSid()).collect(Collectors.toList());
    }

    public List<Long> listSidByPid(Long sid) {
        List<SysOrganization> organizations = listChildByPid(sid);
        return organizations.stream().map(t -> t.getSid()).collect(Collectors.toList());
    }


    /**
     * 获取组织
     *
     * @param sid
     * @return
     */
    private SysOrganization getOrganization(String appType, long sid) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sid", sid);
        params.put("appType", StringUtils.isBlank(appType) ? WebUtils.getSSOUser().getAppType() : appType);
        return organizationMapper.getBy(params);
    }


    @Component
    class OrganizeTreeHandler implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        public OrganizeTreeBuilder build(List<SysOrganization> data) {
            OrganizeTreeBuilder build = applicationContext.getBean(OrganizeTreeBuilder.class);
            return build.build(data);
        }


        @Component
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        class OrganizeTreeBuilder {

            private List<SysOrganization> data;

            public OrganizeTreeBuilder build(List<SysOrganization> data) {
                this.data = data;
                return this;
            }

            public List<SysOrganization> builTree() {
                List<SysOrganization> tree = Lists.newArrayList();
                for (SysOrganization node : getRootNode()) {
                    node = buildChilTree(node);
                    tree.add(node);
                }
                destory();
                return tree;
            }


            private SysOrganization buildChilTree(SysOrganization pNode) {
                List<SysOrganization> chils = Lists.newArrayList();
                for (SysOrganization node : this.data) {
                    if (node.getPid().equals(pNode.getSid())) {
                        chils.add(buildChilTree(node));
                    }
                }
                pNode.setChildrens(chils);
                return pNode;
            }


            private List<SysOrganization> getRootNode() {
                List<SysOrganization> rootMenuLists = Lists.newArrayList();
                for (SysOrganization node : this.data) {
                    if (node.getPid() == 0) {
                        rootMenuLists.add(node);
                    }
                }
                return rootMenuLists;
            }


            private void destory() {
                if (CollectionUtils.isNotEmpty(this.data)) {
                    this.data.clear();
                    this.data = null;
                }
            }
        }
    }


    @Component
    class SpecOrganizeTreeHandler implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        public SpecOrganizeTreeBuilder build(List<SysOrganization> data) {
            SpecOrganizeTreeBuilder build = applicationContext.getBean(SpecOrganizeTreeBuilder.class);
            return build.build(data);
        }


        public List<SysOrganization> tree2List(List<SysOrganization> tree) {
            List<SysOrganization> nodes = Lists.newArrayList();
            for (SysOrganization node : tree) {
                SysOrganization newNode = CommonUtils.deepClone(node);
                newNode.setChildrens(null);
                nodes.add(newNode);
                if (CollectionUtils.isNotEmpty(node.getChildrens())) {
                    nodes.addAll(tree2List(node.getChildrens()));
                }
            }
            return nodes;
        }


        @Component
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        class SpecOrganizeTreeBuilder {

            private List<SysOrganization> data;

            public SpecOrganizeTreeBuilder build(List<SysOrganization> data) {
                this.data = data;
                return this;
            }

            public List<SysOrganization> builTree(long rootId) {
                List<SysOrganization> tree = Lists.newArrayList();
                for (SysOrganization node : getRootNode(rootId)) {
                    node = buildChilTree(node);
                    tree.add(node);
                }
                destory();
                return tree;
            }


            private SysOrganization buildChilTree(SysOrganization pNode) {
                List<SysOrganization> chils = Lists.newArrayList();
                for (SysOrganization node : this.data) {
                    if (node.getPid().equals(pNode.getSid())) {
                        chils.add(buildChilTree(node));
                    }
                }
                pNode.setChildrens(chils);
                return pNode;
            }


            private List<SysOrganization> getRootNode(long rootId) {
                List<SysOrganization> rootMenuLists = Lists.newArrayList();
                for (SysOrganization node : this.data) {
                    if (String.valueOf(node.getPid()).equals(String.valueOf(rootId))) {
                        rootMenuLists.add(node);
                    }
                }
                return rootMenuLists;
            }


            private void destory() {
                if (CollectionUtils.isNotEmpty(this.data)) {
                    this.data.clear();
                    this.data = null;
                }
            }
        }
    }

}
