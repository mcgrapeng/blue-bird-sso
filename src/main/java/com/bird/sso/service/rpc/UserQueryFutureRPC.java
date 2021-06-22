package com.bird.sso.service.rpc;

import com.bird.sso.api.IUserQueryFutureService;
import com.bird.sso.api.domain.SSOUser;
import com.bird.sso.domain.SysUser;
import com.bird.sso.service.UserAuthorityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
@org.apache.dubbo.config.annotation.Service(interfaceName = "com.bird.sso.api.IUserQueryFutureService", protocol = "dubbo", version = "1.0", retries = 3
        , timeout = 60000, loadbalance = "random", executes = 200, actives = 0, cluster = "failover")
@Slf4j
public class UserQueryFutureRPC implements IUserQueryFutureService {


    @Autowired
    @Qualifier(value = "com.bird.sso.service.UserQueryService")
    private com.bird.sso.service.UserQueryService userQueryService;


    @Autowired
    private UserAuthorityService userAuthorityService;


    @Override
    public CompletableFuture<SSOUser> getFutureSSOUser(String appType, long userId) {
        return CompletableFuture.supplyAsync(() -> {
            SysUser user = userQueryService.findUserByUserId(appType, userId);
            if (ObjectUtils.isEmpty(user))
                return null;
            SSOUser ssoUser = new SSOUser();
            BeanUtils.copyProperties(user, ssoUser);
            return ssoUser;
        });
    }
}
