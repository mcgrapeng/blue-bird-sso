package com.bird.sso.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/11 11:16
 */
@Aspect
@Component
@Slf4j
public class LogAspect {


    /**
     * 切点签名 webLog()
     * 切点表达式 xx包下所有类中所有public方法
     */
    @Pointcut("execution(public * com.bird.sso.web.*.*(..))")
    public void webLog() {
    }

    /**
     * 前置
     *
     * @param joinPoint
     */
    @Before("webLog()")
    public void atBefore(JoinPoint joinPoint) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>before [{}]", joinPoint.getSignature().getName());
    }

    /**
     * 后置返回
     *
     * @param joinPoint
     * @param response
     */
    @AfterReturning(pointcut = "webLog()", returning = "response")
    public void atAfterReturning(JoinPoint joinPoint, Object response) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>after return [{}] [{}]", joinPoint.getSignature().getName(), response);
    }

    /**
     * 后置异常
     */
    @AfterThrowing(pointcut = "webLog()", throwing = "throwable")
    public void atAfterThrowing(JoinPoint joinPoint, Throwable throwable) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>after throw [{}]", joinPoint.getSignature().getName(), throwable);
    }

    /**
     * 后置
     * <p>
     * 最终，无论成功失败
     *
     * @param joinPoint
     */
    @After("webLog()")
    public void atAfter(JoinPoint joinPoint) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>after [{}]", joinPoint.getSignature().getName());
    }

    /**
     * 环绕通知
     *
     * @param proceedingJoinPoint
     * @return
     */
    @Around("webLog()")
    public Object onAround(ProceedingJoinPoint proceedingJoinPoint) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>around [{}]", proceedingJoinPoint.getSignature().getName());

        try {
            return proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            log.warn(throwable.getMessage(), throwable);
            return null;
        }
    }
}
