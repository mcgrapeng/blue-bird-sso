//package com.bird.sso.service.rpc;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.bird.sso.api.IOrganizeQueryService;
//import com.bird.sso.api.domain.Organization;
//import com.bird.sso.api.domain.SSOOrganize;
//import com.bird.sso.api.enums.AppEnum;
//import com.bird.sso.api.ex.SSOException;
//import com.bird.sso.domain.SysOrganization;
//import com.bird.sso.mapper.OrganizationMapper;
//import com.bird.sso.service.OrganizeManageService;
//import com.bird.sso.service.OrganizeQueryService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.config.ConfigurableBeanFactory;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @author 张朋
// * @version 1.0
// * @desc
// * @date 2020/6/19 10:20
// */
//@org.apache.dubbo.config.annotation.Service(interfaceName = "com.bird.sso.api.IOrganizeQueryService", protocol = "dubbo", version = "1.0", retries = 3
//        , timeout = 60000, loadbalance = "random", executes = 200, actives = 0, cluster = "failover")
//@Component
//@Slf4j
//public class OrganizeQueryRPC implements IOrganizeQueryService {
//    @Autowired
//    private OrganizationMapper organizationMapper;
//
//
//    @Autowired
//    private OrganizeManageService organizationManageService;
//
//    @Autowired
//    private TreeHandler treeHandler;
//
//    @Autowired
//    private RecursionParent recursionParent;
//
//
//    @Autowired
//    private OrganizeQueryService organizationQueryService;
//
//
///*
//    @Override
//    public PageBean<Organization> page(AppEnum appEnum, PageParam pageParam) {
//
//        if (null == appEnum) {
//            throw SSOException.APP_TYPE_IS_NULL;
//        }
//
//        int count = organizationMapper.count(null);
//        if (count == 0) {
//            return PageBean.of(pageParam.getPageNum(), pageParam.getPageSize());
//        }
//
//        // 校验当前页数
//        int currentPage = PageBean.checkCurrentPage(count, pageParam.getPageSize(), pageParam.getPageNum());
//        int numPerPage = PageBean.checkNumPerPage(pageParam.getPageSize()); // 校验每页记录数
//
//        Map<String, Object> params = Maps.newHashMap();
//        // 根据页面传来的分页参数构造SQL分页参数
//        params.put("pageNum", (currentPage - 1) * numPerPage);
//        params.put("pageSize", numPerPage);
//        params.put("appType", appEnum.name());
//
//
//        List<SysOrganization> sysOrganizations = organizationMapper
//                .selectBy(params);
//
//        params.clear();
//
//        List<Organization> l = sysOrganizations.stream().map(input -> {
//            Organization n = new Organization(input.getSid(), input.getPid()
//                    , input.getName(), organizationQueryService.getParentOrganizationNameByPid(input.getAppType(), input.getPid()), input.getAppType());
//            return n;
//        }).collect(Collectors.toList());
//
//        return PageBean.of(currentPage, numPerPage, count, l);
//    }
//*/
//
//
//    @Override
//    public List<SSOOrganize> tree(AppEnum appEnum) {
//        List<SSOOrganize> l = list(appEnum);
//        TreeHandler.TreeBuilder build = treeHandler.build(l);
//        List<SSOOrganize> tree = build.builTree();
//        build.destory();
//        return tree;
//    }
//
//
//
//    @Override
//    public List<SSOOrganize> list(AppEnum appEnum) {
//        if (null == appEnum) {
//            throw SSOException.APP_TYPE_IS_NULL;
//        }
//
//        Map<String, Object> params = Maps.newHashMap();
//        params.put("appType", appEnum.name());
//
//        List<SysOrganization> list = organizationManageService.list(params);
//        if (CollectionUtils.isEmpty(list)) {
//            return Lists.newArrayList();
//        }
//
//        List<SSOOrganize> l = list.stream().map(input -> {
//            SSOOrganize n = new Organization(input.getSid(), input.getPid()
//                    , input.getName(), organizationQueryService.getParentOrganizationNameByPid(input.getAppType(), input.getPid()), input.getAppType());
//            return n;
//        }).collect(Collectors.toList());
//        return l;
//    }
//
//
//    @Override
//    public List<Organization> listRecursionParentOrganization(AppEnum appEnum, long userId) {
//        Organization basicOrganization = getBasicOrganizationByUserId(appEnum.name(), userId);
//        if(null == basicOrganization){
//            return Lists.newArrayList();
//        }
//        List<Organization> organizations = recursionParent.build()
//                .recursionParentOrganization(appEnum, basicOrganization.getParentOrgId());
//        organizations.add(basicOrganization);
//        return organizations;
//    }
//
//    @Override
//    public List<SysOrganization> listRecursionChildOrganization(String appType, long userId) {
//        return organizationQueryService.listRecursionChildOrganization(appType,userId);
//    }
//
//    @Override
//    public SSOOrganize getBasicOrganization(String appType, long orgId) {
//        return organizationQueryService.getBasicOrganization(appType,orgId);
//    }
//
//    @Override
//    public SSOOrganize getBasicOrganizationByUserId(String appType, long userId) {
//        return null;
//    }
//
//
//    @Component
//    class RecursionParent implements ApplicationContextAware {
//        private static final String NAME = "ParentNodes";
//        private ApplicationContext applicationContext;
//
//        @Override
//        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//            this.applicationContext = applicationContext;
//        }
//
//        public ParentNodes build() {
//            ParentNodes build = applicationContext.getBean(NAME, ParentNodes.class);
//            return build;
//        }
//
//        @Component("ParentNodes")
//        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//        class ParentNodes {
//
//            private List<SSOOrganize> data = Lists.newArrayList();
//
//            public List<SSOOrganize> recursionParentOrganization(AppEnum appEnum, long parentOrgId) {
//                if (parentOrgId == 0) {
//                    return data;
//                }
//                SSOOrganize basicOrganization = getBasicOrganization(appEnum.name(), parentOrgId);
//                data.add(basicOrganization);
//                return recursionParentOrganization(appEnum, basicOrganization.getParentOrgId());
//            }
//        }
//    }
//
//
//
//
//    @Component
//    class TreeHandler implements ApplicationContextAware {
//
//        private static final String NAME = "OrganizeTreeBuilder";
//        private ApplicationContext applicationContext;
//
//        @Override
//        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//            this.applicationContext = applicationContext;
//        }
//
//        public TreeBuilder build(List<SSOOrganize> data) {
//            TreeBuilder build = applicationContext.getBean(NAME, TreeBuilder.class);
//            return build.build(data);
//        }
//
//        @Component("OrganizeTreeBuilder")
//        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//        class TreeBuilder {
//
//            private List<SSOOrganize> data;
//
//            public TreeBuilder build(List<SSOOrganize> data) {
//                this.data = data;
//                return this;
//            }
//
//            public List<SSOOrganize> builTree() {
//                List<SSOOrganize> tree = Lists.newArrayList();
//                for (SSOOrganize node : getRootNode()) {
//                    node = buildChilTree(node);
//                    tree.add(node);
//                }
//                destory();
//                return tree;
//            }
//
//
//            private SSOOrganize buildChilTree(SSOOrganize pNode) {
//                List<SSOOrganize> chils = Lists.newArrayList();
//                for (SSOOrganize node : this.data) {
//                    if (node.getPid().equals(pNode.getSid())) {
//                        chils.add(buildChilTree(node));
//                    }
//                }
//                pNode.setChilds(chils);
//                return pNode;
//            }
//
//            private List<SSOOrganize> getRootNode() {
//                List<SSOOrganize> rootMenuLists = Lists.newArrayList();
//                for (SSOOrganize node : this.data) {
//                    if (node.getPid() == 0) {
//                        rootMenuLists.add(node);
//                    }
//                }
//                return rootMenuLists;
//            }
//
//            private void destory() {
//                if (CollectionUtils.isNotEmpty(this.data)) {
//                    this.data.clear();
//                    this.data = null;
//                }
//            }
//        }
//    }
//
//}
//
