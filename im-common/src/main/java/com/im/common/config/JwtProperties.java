package com.im.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * JWT 签名密钥
     */
    private String secret;

    /**
     * 令牌过期时间，单位：毫秒
     */
    private Long expire;

    /**
     * 请求头中携带 Token 的字段名
     */
    private String header = "token";
}