package com.im.pushservice.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class PushThreadPoolConfig {

    @Bean("pushExecutor")
    public ThreadPoolExecutor pushExecutor() {
        return new ThreadPoolExecutor(
                4,
                16,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(2000),
                new ThreadFactoryBuilder().setNamePrefix("push-thread-").build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}