package com.im.nettyserver.config;


import com.im.nettyserver.cluster.ClusterMsgSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisListenerConfig {

    private final NettyProperties nettyProperties;
    private final ClusterMsgSubscriber clusterMsgSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // 订阅集群消息主题
        container.addMessageListener(clusterMsgSubscriber, new PatternTopic(nettyProperties.getClusterTopic()));
        return container;
    }
}