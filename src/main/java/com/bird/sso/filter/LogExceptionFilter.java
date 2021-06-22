package com.bird.sso.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/6/18 10:53
 */
@Slf4j
public class LogExceptionFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        log.info(">>>>>>>>>>iLoggingEvent={}", iLoggingEvent.getMessage());
        if (iLoggingEvent.getLevel().isGreaterOrEqual(Level.INFO)) {
            switch (iLoggingEvent.getLoggerName()) {
                case "org.springframework.cloud.sleuth.instrument.web.ExceptionLoggingFilter":
                    return FilterReply.DENY;
                case "com.alibaba.nacos.client.config.impl.ClientWorker":
                    return FilterReply.DENY;
                case "com.alibaba.nacos.client.config.http.ServerHttpAgent":
                    return FilterReply.DENY;
            }
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
