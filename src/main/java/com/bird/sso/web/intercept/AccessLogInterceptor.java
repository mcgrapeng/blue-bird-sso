//package com.bird.sso.web.intercept;
//
//import com.alibaba.fastjson.JSON;
//import jdk.nashorn.internal.ir.annotations.Reference;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.util.Assert;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Date;
//
///**
// * @author 张朋
// * @version 1.0
// * @desc 访问日志拦截器
// * @date 2020/5/7 11:43
// */
//@Slf4j
//@Component
//public class AccessLogInterceptor extends HandlerInterceptorAdapter {
//    /**
//     * 开始时间
//     */
//    private static final ThreadLocal<Date> START_TIME = new ThreadLocal<>();
//
//    //@Reference(validation = "true", version = "${dubbo.consumer.AdminAccessLogService.version:1.0.0}")
//    //private SystemLogService systemAccessLogService;
//
//    @Value("${spring.application.name}")
//    private String applicationName;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        // 记录当前时间
//        START_TIME.set(new Date());
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        //AccessLogAddDTO accessLog = new AccessLogAddDTO();
//        try {
//            // 初始化 accessLog
//            initAccessLog(accessLog, request);
//            // 执行插入 accessLog
//            addAccessLog(accessLog);
//            // TODO 提升：暂时不考虑 ELK 的方案。而是基于 MySQL 存储。如果访问日志比较多，需要定期归档。
//        } catch (Throwable th) {
//            log.error("[afterCompletion][插入访问日志({}) 发生异常({})", JSON.toJSONString(accessLog), ExceptionUtils.getRootCauseMessage(th));
//        } finally {
//            clear();
//        }
//    }
//
//    private void initAccessLog(AccessLogAddDTO accessLog, HttpServletRequest request) {
//        // 设置用户编号
//        accessLog.setUserId(MallUtil.getUserId(request));
//        if (accessLog.getUserId() == null) {
//            accessLog.setUserId(AccessLogAddDTO.USER_ID_NULL);
//        }
//        accessLog.setUserType(MallUtil.getUserType(request));
//        // 设置访问结果
//        CommonResult result = MallUtil.getCommonResult(request);
//        Assert.isTrue(result != null, "result 必须非空");
//        accessLog.setErrorCode(result.getCode())
//                .setErrorMessage(result.getMessage());
//        // 设置其它字段
//        accessLog.setTraceId(MallUtil.getTraceId())
//                .setApplicationName(applicationName)
//                .setUri(request.getRequestURI()) // TODO 提升：如果想要优化，可以使用 Swagger 的 @ApiOperation 注解。
//                .setQueryString(HttpUtil.buildQueryString(request))
//                .setMethod(request.getMethod())
//                .setUserAgent(HttpUtil.getUserAgent(request))
//                .setIp(HttpUtil.getIp(request))
//                .setStartTime(START_TIME.get())
//                .setResponseTime((int) (System.currentTimeMillis() - accessLog.getStartTime().getTime())); // 默认响应时间设为 0
//    }
//
////    @Async // 异步入库
////    public void addAccessLog(AccessLogAddDTO accessLog) {
////        try {
////            systemAccessLogService.addAccessLog(accessLog);
////        } catch (Throwable th) {
////            log.error("[addAccessLog][插入访问日志({}) 发生异常({})", JSON.toJSONString(accessLog), ExceptionUtils.getRootCauseMessage(th));
////        }
////    }
//
//    private static void clear() {
//        START_TIME.remove();
//    }
//}
