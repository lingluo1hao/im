package com.im.taskservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "im.task")
public class TaskProperties {

    private XxlConfig xxl;

    /**
     * 消息保留天数，超过则清理
     */
    private Integer messageKeepDays = 90;

    /**
     * 离线消息最大补发次数
     */
    private Integer maxResendTimes = 3;

    /**
     * 设备离线超期天数，超过则清理
     */
    private Integer deviceExpireDays = 180;

    /**
     * 分布式锁超时时间（秒）
     */
    private Integer lockExpireSeconds = 300;

    @Data
    public static class XxlConfig {
        private String adminAddresses;
        private String appname;
        private String address;
        private String ip;
        private Integer port;
        private String accessToken;
        private String logPath;
        private Integer logRetentionDays;
    }
}