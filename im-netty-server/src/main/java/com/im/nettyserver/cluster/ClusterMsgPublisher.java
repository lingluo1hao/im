package com.im.nettyserver.cluster;

import com.im.netty.protocol.protobuf.ImProtocol;
import com.im.nettyserver.config.NettyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterMsgPublisher {

    private final StringRedisTemplate redisTemplate;
    private final NettyProperties nettyProperties;

    /**
     * 发布集群消息（Protobuf 二进制格式）
     */
    public void publishMsg(ImProtocol.ImPacket packet) {
        try {
            byte[] bytes = packet.toByteArray();
            // 转 Base64 字符串兼容 StringRedisTemplate；追求极致性能可换用 RedisTemplate 直接发字节数组
            String msgBase64 = Base64.getEncoder().encodeToString(bytes);
            redisTemplate.convertAndSend(nettyProperties.getClusterTopic(), msgBase64);
        } catch (Exception e) {
            log.error("集群消息发布失败", e);
        }
    }
}