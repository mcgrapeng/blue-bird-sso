package com.bird.sso.service.rpc;

import com.bird.sso.api.IUserQueryService;
import com.bird.sso.api.domain.SSORole;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.api.enums.AppEnum;
import com.bird.sso.api.ex.SSOException;
import com.bird.sso.domain.SysMenu;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.UserAuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * executes: 一个服务提供者并行执行请求上限，即当 Provider 对一个服务的并发调用达到上限后
 * ，新调用会阻塞，此时 Consumer 可能会超时
 * <p>
 * actives: 消费者端的最大并发调用限制，即当 Consumer 对一个服务的并发调用到上限后，新调用会阻塞直到超时 0表示不限制
 * loadbalance: 负载均衡策略 , 随机，按权重设置随机概率
 * cluster: 集群容错模式  失败自动切换，failover : 当出现失败，重试其它服务器
 *
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/7 13:45
 */
@org.apache.dubbo.config.annotation.Service(interfaceName = "com.bird.sso.api.IUserQueryService", protocol = "dubbo", version = "1.0", retries = 3
        , timeout = 60000, loadbalance = "random", executes = 200, actives = 0, cluster = "failover")
@Slf4j
public class UserQueryRPC implements IUserQueryService {


    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private com.bird.sso.service.UserQueryService userQueryService;


    @Autowired
    private UserAuthorityService userAuthorityService;


    @Override
    public SSOUser getSSOUser(AppEnum appEnum, long userId) {
        SSOUser ssoUser = new SSOUser();
        SysUser user = userQueryService.findUserByUserId(appEnum.name(), userId);
        if (ObjectUtils.isEmpty(user))
            return null;

        BeanUtils.copyProperties(user, ssoUser);
        return ssoUser;
    }

    @Override
    public SSOUser getSSOUser(AppEnum appEnum, String username) {
        SSOUser ssoUser = new SSOUser();
        SysUser user = userQueryService.findUserByUserName(appEnum.name(), username);
        if (ObjectUtils.isEmpty(user))
            return null;
        BeanUtils.copyProperties(user, ssoUser);
        return ssoUser;
    }


    @Override
    public SSOUser getSSOUser(String appType, long userId) {
        SSOUser ssoUser = new SSOUser();
        SysUser user = userQueryService.findUserByUserId(appType, userId);

        if (ObjectUtils.isEmpty(user))
            return null;

        BeanUtils.copyProperties(user, ssoUser);
        return ssoUser;
    }

    @Override
    public SSOUser getSSOUser(String appType, String username) {
        SSOUser ssoUser = new SSOUser();
        SysUser user = userQueryService.findUserByUserName(appType, username);

        if (ObjectUtils.isEmpty(user))
            return null;

        BeanUtils.copyProperties(user, ssoUser);
        return ssoUser;
    }

    @Override
    public List<String> hasAuthorities(String appType, String clientType, long userId) {
        List<SysMenu> sysMenus = userAuthorityService.hasMenus(appType, clientType, userId);
        List<String> l = sysMenus.stream().map(input -> input.getPath()).collect(Collectors.toList());
        return l;
    }

    @Override
    public List<SSORole> listRole(String appType, long userId) {
        SysUser user = userQueryService.findUserByUserId(appType, userId);
        if (ObjectUtils.isEmpty(user)) throw SSOException.USER_NO_EXITS;

        List<SSORole> roles = user.getAuthority().listRole().stream().map(r -> {
            SSORole role = SSORole.RoleBuilder.of().setAppType(r.getAppType())
                    .setCode(r.getCode()).setParentRoleId(r.getPid())
                    .setName(r.getName())
                    .setRoleId(r.getSid()).build();
            return role;
        }).collect(Collectors.toList());
        return roles;
    }


}
