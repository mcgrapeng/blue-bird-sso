package com.bird.sso.config;


import com.bird.sso.thread.ContextAwarePoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean(name = "asyncServiceExecutor")
    public ContextAwarePoolExecutor asyncServiceExecutor() {
        //ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        ContextAwarePoolExecutor executor = new ContextAwarePoolExecutor();

        //配置核心线程数
        executor.setCorePoolSize(100);
        //配置最大线程数
        executor.setMaxPoolSize(120);
        //配置队列大小
        executor.setQueueCapacity(100);

        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("bird-async-service-");
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务

        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        //执行初始化
        executor.initialize();
        return executor;
    }


}