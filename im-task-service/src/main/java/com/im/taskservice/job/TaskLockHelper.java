package com.im.taskservice.job;

import com.im.taskservice.config.TaskProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TaskLockHelper {

    private final StringRedisTemplate redisTemplate;
    private final TaskProperties taskProperties;

    /**
     * 尝试获取分布式锁
     */
    public boolean tryLock(String taskCode) {
        String key = "task:lock:" + taskCode;
        Boolean flag = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", taskProperties.getLockExpireSeconds(), TimeUnit.SECONDS);
        return Boolean.TRUE.equals(flag);
    }

    /**
     * 释放锁
     */
    public void unlock(String taskCode) {
        redisTemplate.delete("task:lock:" + taskCode);
    }
}