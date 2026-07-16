package com.im.nettyserver.cluster;

import com.im.netty.protocol.protobuf.ImProtocol;
import com.im.nettyserver.manager.ImSessionManager;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterMsgSubscriber implements MessageListener {

    private final ImSessionManager sessionManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msgBody = new String(message.getBody());
            byte[] bytes = Base64.getDecoder().decode(msgBody);
            // Protobuf 反序列化
            ImProtocol.ImPacket packet = ImProtocol.ImPacket.parseFrom(bytes);

            // 提取目标用户ID，根据消息类型分发
            Long targetUserId = null;
            if (packet.getMsgType() == ImProtocol.MsgType.PRIVATE_MESSAGE) {
                targetUserId = packet.getPrivateMessage().getTargetId();
            } else if (packet.getMsgType() == ImProtocol.MsgType.GROUP_MESSAGE) {
                // 群聊：此处遍历群成员，匹配本地在线用户转发
                // 实际业务需拉取群成员列表，逐个判断本地在线
                return;
            }

            // 目标用户在本节点在线，直接转发
            if (targetUserId != null && sessionManager.isOnline(targetUserId)) {
                Channel channel = sessionManager.getChannel(targetUserId);
                channel.writeAndFlush(packet);
            }

        } catch (Exception e) {
            log.error("集群消息接收解析失败", e);
        }
    }
}