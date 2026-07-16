package com.im.pushservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "im.push")
public class PushProperties {
    private Integer idempotentExpire = 3600;
    private Integer allPushBatchSize = 500;

    private JPushConfig jpush;
    private HuaweiConfig huawei;
    private XiaomiConfig xiaomi;
    private ApnsConfig apns;

    @Data
    public static class JPushConfig {
        private String appKey;
        private String masterSecret;
        private Boolean production = true;
    }

    @Data
    public static class HuaweiConfig {
        private String appId;
        private String appSecret;
    }

    @Data
    public static class XiaomiConfig {
        private String packageName;
        private String appSecret;
    }

    @Data
    public static class ApnsConfig {
        private String bundleId;
        private String teamId;
        private String keyId;
        private String p8FilePath;
        private Boolean production = true;
    }
}