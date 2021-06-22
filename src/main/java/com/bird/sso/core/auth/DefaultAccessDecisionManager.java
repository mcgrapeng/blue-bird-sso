package com.bird.sso.core.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/23 20:03
 */
@Slf4j
public class DefaultAccessDecisionManager implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        // 无权限访问
        if(CollectionUtils.isEmpty(configAttributes)){
            log.info(">>>>>>>>>>>>>>>>>>>>>>无访问权限.");
            throw new AccessDeniedException("无访问权限.");
        }
        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()){
            ConfigAttribute configAttribute = iterator.next();
            String needRole = configAttribute.getAttribute();
            for(GrantedAuthority grantedAuthority : authentication.getAuthorities()){
                //grantedAuthority 为用户所被赋予的权限。 needRole 为访问相应的资源应该具有的权限。
                //判断两个请求的url的权限和用户具有的权限是否相同，如相同，允许访问 权限就是那些以ROLE_为前缀的角色
                if (needRole.trim().equals(grantedAuthority.getAuthority().trim())){
                    //匹配到对应的角色，则允许通过
                    return;
                }
            }
        }
        //该url具有访问权限，但是当前登录用户没有匹配到URL对应的权限，则抛出无权限错误
        log.info(">>>>>>>>>>>>>>>>>>>>>无访问权限.");
        throw  new AccessDeniedException("无访问权限.");
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return Boolean.TRUE;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Boolean.TRUE;
    }
}
