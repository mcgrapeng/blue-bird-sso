package com.bird.sso.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author zhangpeng10
 * @version 1.0
 * @desc
 * @date 2020/4/26 14:50
 */
@Aspect
@Component
public class CheckLoginAspect {

    @Around("@annotation(com.bird.sso.annotation.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point) throws Throwable {
        //从header中获取token


        //校验token是否合法，不合法抛出异常、合法放行


        return point.proceed();
    }
}
