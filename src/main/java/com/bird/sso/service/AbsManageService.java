package com.bird.sso.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.PublicEnum;
import com.bird.sso.api.enums.RoleTypeEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.core.TreeHandler;
import com.bird.sso.domain.SelfReference;
import com.bird.sso.domain.SysRole;
import com.bird.sso.domain.SysUser;
import com.bird.sso.mapper.SelfReferenceMapper;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.thread.ContextAwarePoolExecutor;
import com.bird.sso.utils.SnowflakeIdWorker;
import com.bird.sso.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 抽象基本信息管理
 *
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/13 16:27
 */
@Slf4j
public abstract class AbsManageService<N extends SelfReferenceMapper<T>, T extends SelfReference>
        implements SSOManageService<N, T> {

    @Autowired
    protected N mapper;


    protected abstract String getName();


    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    protected UserQueryService userQueryService;

    @Autowired
    protected ContextAwarePoolExecutor executor;

    /**
     * 获取授权列表并更新
     *
     * @return
     */
    protected abstract void getAuthoritiesAndSet(String appType, List<Long> sids);


    /**
     * 去重逻辑
     *
     * @param t
     * @param message 冲突信息
     */
    //protected abstract void duplicate(T t, String message);

    /**
     * 基本信息
     * <p>
     * 角色
     * [{
     * "appType":"",
     * "name":"",
     * "code":"",
     * "level":""
     * "type":""
     * "clientType":""
     * }]
     * <p>
     * 菜单
     * [{
     * "appType":"",
     * "name":"",
     * "code":"",
     * "level":""
     * "isClick":""
     * "clientType":"",
     * "posit":"",
     * "path":"",
     * "router":"",
     * "icon":"",
     * "type":"MENU"
     * }]
     * <p>
     * 附加信息
     * {
     * <p>
     * ....
     * <p>
     * }
     *
     * @param data
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addInfo(List<T> data) {

        if (CollectionUtils.size(data) > 1) {
            List<T> unique = data.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> StringUtils.isNotBlank(p.getCode())
                                    ? p.getAppType() + "_" + p.getCode()
                                    : p.getAppType() + "_" + p.getPid() + "_" + p.getName())))
                    , ArrayList::new));

            for (T r : unique) {
                r.setIsAvailable(PublicEnum.Y.name());
                r.setSid(SnowflakeIdWorker.build(4).nextId());
                if (ObjectUtils.isEmpty(r.getPid())) {
                    r.setPid(0L);
                }
                r.setCreator(WebUtils.getSSOUser().getUserName());
                r.setCreateTime(Date.from(Instant.now()));
                //duplicate(r, StringUtils.join("当前要添加的", getName(), "已存在，冲突信息为：", r.getName()));
                duplicate(r, StringUtils.join("当前要添加的", getName(), "已存在，冲突信息为：", r.getName()), DuplicateEnum.INSERT);
                try {
                    mapper.insert(r);
                } catch (DuplicateKeyException e) {
                }
            }
        } else {
            T r = data.get(0);
            r.setIsAvailable(PublicEnum.Y.name());
            r.setSid(SnowflakeIdWorker.build(3).nextId());
            if (ObjectUtils.isEmpty(r.getPid())) {
                r.setPid(0L);
            }
            r.setCreator(WebUtils.getSSOUser().getUserName());
            r.setCreateTime(Date.from(Instant.now()));
            //duplicate(r, StringUtils.join("当前要添加的", getName(), "已存在，冲突信息为：", r.getName()));
            duplicate(r, StringUtils.join("当前要添加的", getName(), "已存在，冲突信息为：", r.getName()), DuplicateEnum.INSERT);
            try {
                mapper.insert(r);
            } catch (DuplicateKeyException e) {
            }
        }
    }

    @Override
    public void addInfo(T data) {
        addInfo(Lists.newArrayList(data));
    }


    @Override
    public void updInfo(T data) {
        updInfo(Lists.newArrayList(data));
    }

    /**
     * 基本信息
     * [{
     * "sid":"",
     * "name":"",
     * "code":"",
     * "level":""
     * "type":""
     * "clientType":""
     * "appType":""
     * }]
     * <p>
     * 附加信息
     * {
     * <p>
     * .....
     * }
     *
     * @param data
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updInfo(List<T> data) {
        if (CollectionUtils.size(data) > 1) {
            List<T> unique = data.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(p -> StringUtils.isNotBlank(p.getCode())
                                    ? p.getAppType() + "_" + p.getCode()
                                    : p.getAppType() + "_" + p.getPid() + "_" + p.getName()))), ArrayList::new));

            for (T r : unique) {
                //duplicate(r, StringUtils.join("当前要修改的", getName(), "已存在，冲突信息为：", r.getName()));
                duplicate(r, StringUtils.join("当前要添加的", getName(), "已存在，冲突信息为：", r.getName()), DuplicateEnum.UPDATE);
                r.setEditor(WebUtils.getSSOUser().getUserName());
                mapper.update(r);
            }
        } else {
            T r = data.get(0);
            //duplicate(r, StringUtils.join("当前要修改的", getName(), "已存在，冲突信息为：", r.getName()));
            duplicate(r, StringUtils.join("当前要添加的", getName(), "已存在，冲突信息为：", r.getName()), DuplicateEnum.UPDATE);
            r.setEditor(WebUtils.getSSOUser().getUserName());
            mapper.update(r);
        }
    }


    @Override
    public List<T> findInfoByPid(String appType, long pid) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("pid", pid);
        params.put("appType", StringUtils.isBlank(appType) ? WebUtils.getSSOUser().getAppType() : appType);
        return mapper.selectBy(params);
    }


    @Override
    public T findInfoBySid(String appType, long sid) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sid", sid);
        params.put("appType", StringUtils.isBlank(appType) ? WebUtils.getSSOUser().getAppType() : appType);
        return mapper.getBy(params);
    }


    /**
     * 级联删除
     *
     * @param appType
     * @param pid
     * @param sid
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delInfo(String appType, long pid, long sid) {
        //删除保障机制（不能删除自己的角色、组织，权限）
        deleteSafe();

        mapper.deleteReference(appType, sid);
        //节点及其子节点删除
        deleteChildByParent(appType, sid);
        //异步获取授权数据并更新
        getAuthoritiesAndSet(appType, Lists.newArrayList(sid));
    }


    /**
     * 删除
     *
     * @param appType
     * @param sid
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delInfo(String appType, long sid) {
        //删除保障机制（只有系统管理员可删除）
        deleteSafe();

        //实际删除
        int rowCount = mapper.deleteReference(appType, sid);

        if (rowCount > 0) {
            //异步获取授权数据并更新
            executor.execute(() -> getAuthoritiesAndSet(appType, Lists.newArrayList(sid)));
        }
    }

    /**
     * 通用树状结构
     *
     * @param params
     * @return
     */
    public List<T> tree(Map<String, Object> params) {
        List<T> data = list(params);
        TreeHandler of = TreeHandler.of(data);
        List<T> tree = of.builTree();
        of.destroy();
        return tree;
    }


    /**
     * 分页
     *
     * @param pageParam
     * @param params
     * @return
     */
    @Override
    public PageBean<T> page(PageParam pageParam, Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params)) {
            params = Maps.newHashMap();
        }
        // 统计总记录数
        Integer totalCount = mapper.count(params);

        if (0 == totalCount) {
            return PageBean.of(pageParam.getPageNum(), pageParam.getPageSize());
        }


        // 校验当前页数
        int currentPage = PageBean.checkCurrentPage(totalCount.intValue()
                , pageParam.getPageSize(), pageParam.getPageNum());

        // 校验页面输入的每页记录数numPerPage是否合法
        int numPerPage = PageBean.checkNumPerPage(pageParam.getPageSize()); // 校验每页记录数


        // 根据页面传来的分页参数构造SQL分页参数
        params.put("pageNum", (currentPage - 1) * numPerPage);
        params.put("pageSize", numPerPage);
        params.putAll(params);
        // 获取分页数据集
        List<T> list = mapper.selectBy(params);
        return PageBean.of(currentPage, numPerPage
                , totalCount.intValue(), list);
    }


    @Override
    public List<T> list(Map<String, Object> params) {
        List<T> ts = mapper.selectBy(params);
        // ts.sort(Comparator.comparingInt(SelfReference::getOrd));
        return ts;
    }


    /**
     * 删除保障机制(判断是否为系统管理员)
     */
    private void deleteSafe() {
        SSOUser loginUser = WebUtils.getSSOUser();

        SysUser user = userQueryService.findUserByUserName(loginUser.getAppType()
                , loginUser.getUserName());
        List<SysRole> has = user.getAuthority().listRole();

        if (CollectionUtils.isEmpty(has)) {
            throw SSOException.NOT_DEL_PERM;
        }

        List<SysRole> roles = has.stream().filter(t -> t.getType()
                .equals(RoleTypeEnum.SYSTEM.name()) || t.getType()
                .equals(RoleTypeEnum.SUPER.name())).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(roles)) {
            throw SSOException.NOT_DEL_PERM;
        }
    }


    /**
     * 删除子级
     *
     * @param appType
     * @param sid
     */
    private void deleteChildByParent(String appType, long sid) {
        mapper.deleteReference(appType, sid);
        List<T> childs = findInfoByPid(appType, sid);
        if (CollectionUtils.isNotEmpty(childs)) {
            for (T r : childs) {
                deleteChildByParent(r.getAppType(), r.getSid());
            }
        }
    }

    @Deprecated
    @Override
    public T findInfo(int id) {
        return findInfo(null, id);
    }


    @Override
    public T findInfo(String appType, int id) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("appType", StringUtils.isBlank(appType) ? WebUtils.getSSOUser().getAppType() : appType);
        return mapper.getBy(params);
    }


    /**
     * 去重逻辑
     *
     * @param a
     * @param message 冲突信息
     */
    protected void duplicate(T a, String message, DuplicateEnum removalEnum) {
        List<T> data = findInfoByPid(a.getAppType(), a.getPid());
        if (CollectionUtils.isEmpty(data)) {
            return;
        }

        if (removalEnum.equals(DuplicateEnum.UPDATE) && data.remove(a)) {
            List<T> exists = data.stream().filter(t -> t.getName()
                    .equals(a.getName()) || (StringUtils.isNotBlank(t.getCode())
                    && StringUtils.isNotBlank(a.getCode())
                    && t.getCode().equals(a.getCode()))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(exists)) {
                throw new SSOException(SSOException.DB_RECORD_IS_EXIST.getCode()
                        , message);
            }
        } else {
            List<T> exists = data.stream().filter(t -> t.getName()
                    .equals(a.getName()) || (StringUtils.isNotBlank(t.getCode())
                    && StringUtils.isNotBlank(a.getCode())
                    && t.getCode().equals(a.getCode()))).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(exists)) {
                throw new SSOException(SSOException.DB_RECORD_IS_EXIST.getCode()
                        , message);
            }
        }
    }

    enum DuplicateEnum {
        INSERT,
        UPDATE;
    }
}
