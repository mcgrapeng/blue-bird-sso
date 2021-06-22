package com.bird.sso.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.bird.sso.domain.SysUser;
import com.bird.sso.domain.SysUserVersion;
import com.bird.sso.mapper.SysUserVersionMapper;
import com.bird.sso.mapper.UserMapper;
import com.bird.sso.page.PageBean;
import com.bird.sso.page.PageParam;
import com.bird.sso.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/7 13:45
 */
@Service(value = "com.bird.sso.service.UserQueryService")
@Slf4j
public class UserQueryService {


    @Autowired
    private UserMapper userMapper;


    @Autowired
    private SysUserVersionMapper userVersionMapper;


    public SysUser findUserByUserName(String appType, String username) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("userName", username);
        return userMapper.getByUserName(params);
    }


    public SysUser findUserByOpenId(String appType, String openid) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("wxOpenId", openid);
        params.put("appType", appType);
        return userMapper.getByOpenId(params);
    }


    public List<SysUser> listUserByOrgId(String appType, long orgId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("appType", appType);
        params.put("orgId", orgId);
        return userMapper.selectBy(params);
    }

    public SysUser findUserByUserId(String appType, long userId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("appType",  appType);
        return userMapper.getByUserId(params);
    }


    public List<SysUser> listUserByUserIds(String appType, List<Long> userIds) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userIds", userIds);
        params.put("appType", appType);
        return userMapper.selectBy(params);
    }


    /**
     * 账号变更轨迹
     *
     * @param appType
     * @param userId
     * @return
     */
    public List<SysUserVersion> listUserVersion(String appType, long userId) {

        List<SysUserVersion> versions = userVersionMapper.selectByUserId(appType, userId);
        if (CollectionUtils.isEmpty(versions)) return Lists.newArrayList();


        return versions;
    }


    /**
     * 获取授权用户（非默认授权）
     *
     * @return
     */
    public List<SysUser> listAuthoritiesUser(String appType) {
        return userMapper.selectHasRoles(appType);
    }


    public PageBean<SysUser> page(PageParam pageParam, Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params)) {
            params = Maps.newHashMap();
        }
        // 统计总记录数
        Integer totalCount = userMapper.count(params);

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
        List<SysUser> list = userMapper.selectBy(params);
        return PageBean.of(currentPage, numPerPage
                , totalCount.intValue(), list);
    }

}
