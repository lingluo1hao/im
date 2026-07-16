package com.im.nettyserver.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "im.netty")
public class NettyProperties {
    /**
     * Netty 监听端口
     */
    private Integer port = 9000;

    /**
     * 读空闲超时时间（秒）
     */
    private Integer readerIdleTime = 60;

    /**
     * 写空闲超时时间（秒），0不检测
     */
    private Integer writerIdleTime = 0;

    /**
     * 读写总空闲超时时间（秒），0不检测
     */
    private Integer allIdleTime = 0;

    /**
     * Boss线程组线程数
     */
    private Integer bossThreads = 1;

    /**
     * Worker线程组线程数，0默认取CPU核心数*2
     */
    private Integer workerThreads = 0;

    /**
     * 最大丢失心跳次数
     */
    private Integer heartbeatMaxLose = 3;

    /**
     * 集群消息推送主题
     */
    private String clusterTopic = "im:cluster:msg";

    /**
     * 用户节点映射前缀
     */
    private String userNodePrefix = "im:user:node:";

    /**
     * 用户节点映射Redis过期时间（秒）
     */
    private Integer nodeExpireSeconds = 90;
}