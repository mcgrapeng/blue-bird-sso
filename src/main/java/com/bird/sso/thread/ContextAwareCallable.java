package com.bird.sso.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.Callable;

/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/11/24 19:09
 */
@Slf4j
public class ContextAwareCallable<T> implements Callable<T> {
    private Callable<T> task;
    private RequestAttributes context;

    public ContextAwareCallable(Callable<T> task, RequestAttributes context) {
        this.task = task;
        this.context = context;
    }

    @Override
    public T call() throws Exception {
        if (context != null) {
            log.info(">>>>>>>>>>>>>>>>>>>>context={}",context);
            RequestContextHolder.setRequestAttributes(context);
        }

        try {
            return task.call();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}