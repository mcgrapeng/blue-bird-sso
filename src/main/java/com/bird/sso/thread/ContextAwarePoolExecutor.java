package com.bird.sso.thread;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author 张朋
 * @version 1.0
 * @desc
 * @date 2020/5/29 19:07
 */
@Slf4j
public class ContextAwarePoolExecutor extends ThreadPoolTaskExecutor {


    @Override
    public <T> Future<T> submit(Callable<T> task) {
        showThreadPoolInfo();
        return super.submit(task);
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        showThreadPoolInfo();
        return super.submitListenable(task);
    }


    private void showThreadPoolInfo() {
        ThreadPoolExecutor threadPoolExecutor = getThreadPoolExecutor();

        if (null == threadPoolExecutor) {
            return;
        }

        log.info("{}, {},taskCount [{}], completedTaskCount [{}], activeCount [{}], queueSize [{}]",
                threadPoolExecutor.getTaskCount(),
                threadPoolExecutor.getCompletedTaskCount(),
                threadPoolExecutor.getActiveCount(),
                threadPoolExecutor.getQueue().size());
    }


    @Override
    public void execute(Runnable task) {
        showThreadPoolInfo();
        super.execute(task);
    }


    @Override
    public void execute(Runnable task, long startTimeout) {
        showThreadPoolInfo();
        super.execute(task, startTimeout);
    }


    @Override
    public Future<?> submit(Runnable task) {
        showThreadPoolInfo();
        return super.submit(task);
    }


    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        showThreadPoolInfo();
        return super.submitListenable(task);
    }
}