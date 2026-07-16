package com.im.taskservice.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class XxlJobConfig {

    private final TaskProperties taskProperties;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("XXL-JOB 执行器初始化");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(taskProperties.getXxl().getAdminAddresses());
        executor.setAppname(taskProperties.getXxl().getAppname());
        executor.setAddress(taskProperties.getXxl().getAddress());
        executor.setIp(taskProperties.getXxl().getIp());
        executor.setPort(taskProperties.getXxl().getPort());
        executor.setAccessToken(taskProperties.getXxl().getAccessToken());
        executor.setLogPath(taskProperties.getXxl().getLogPath());
        executor.setLogRetentionDays(taskProperties.getXxl().getLogRetentionDays());
        return executor;
    }
}