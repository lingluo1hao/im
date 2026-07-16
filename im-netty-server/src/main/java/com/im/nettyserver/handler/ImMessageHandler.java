package com.im.nettyserver.handler;

import com.im.nettyserver.cluster.ClusterMsgPublisher;
import com.im.nettyserver.config.NettyProperties;
import com.im.nettyserver.manager.ImSessionManager;
import com.im.netty.protocol.protobuf.ImProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class ImMessageHandler extends ChannelInboundHandlerAdapter {

    private final ImSessionManager sessionManager;
    private final ClusterMsgPublisher clusterMsgPublisher;
    private final StringRedisTemplate redisTemplate;
    private final NettyProperties nettyProperties;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ImProtocol.ImPacket packet = (ImProtocol.ImPacket) msg;
        Channel channel = ctx.channel();

        // ========== 核心规则：未鉴权状态下，只允许处理登录请求 ==========
        if (!sessionManager.isAuthenticated(channel)) {
            if (packet.getMsgType() != ImProtocol.MsgType.LOGIN_REQUEST) {
                log.warn("未鉴权连接发送非登录消息，强制断开，channelId={}", channel.id());
                channel.close();
                return;
            }
            // 处理登录鉴权
            handleLogin(channel, packet.getLoginRequest());
            return;
        }

        // ========== 已鉴权，分发业务消息 ==========
        switch (packet.getMsgType()) {
            case HEARTBEAT_REQUEST -> handleHeartbeat(channel);
            case PRIVATE_MESSAGE -> handlePrivateMsg(packet);
            case GROUP_MESSAGE -> handleGroupMsg(packet);
            case LOGOUT_REQUEST -> handleLogout(channel);
            default -> log.info("未处理的消息类型: {}", packet.getMsgType());
        }
    }

    /**
     * 登录鉴权处理：校验Token → 绑定会话 → 写入集群路由表
     */
    private void handleLogin(Channel channel, ImProtocol.LoginRequest request) {
        String token = request.getToken();
        String redisKey = "auth:token:" + token;

        // 构建登录响应包
        ImProtocol.ImPacket.Builder respBuilder = ImProtocol.ImPacket.newBuilder()
                .setMsgType(ImProtocol.MsgType.LOGIN_RESPONSE);

        // 1. Token 校验（直接查Redis，性能最高，适配高并发长连接场景）
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.FALSE.equals(exists)) {
            respBuilder.setLoginResponse(
                    ImProtocol.LoginResponse.newBuilder()
                            .setCode(1001)
                            .setMsg("登录失败，凭证无效")
                            .build()
            );
            channel.writeAndFlush(respBuilder.build());
            channel.close();
            log.warn("用户登录鉴权失败，token={}", token);
            return;
        }

        // 2. 获取用户ID
        String userIdStr = redisTemplate.opsForValue().get(redisKey);
        Long userId = Long.valueOf(userIdStr);

        // 3. 标记已鉴权 + 绑定本地会话
        sessionManager.markAuthenticated(channel, userId);

        // 4. 写入Redis集群用户节点路由表
        String nodeKey = nettyProperties.getUserNodePrefix() + userId;
        String nodeId = getCurrentNodeId();
        redisTemplate.opsForValue().set(nodeKey, nodeId, 90, TimeUnit.SECONDS);

        // 5. 返回登录成功响应
        respBuilder.setLoginResponse(
                ImProtocol.LoginResponse.newBuilder()
                        .setCode(0)
                        .setMsg("登录成功")
                        .setUserId(userId)
                        .build()
        );
        channel.writeAndFlush(respBuilder.build());
        log.info("用户{}登录鉴权成功，节点: {}", userId, nodeId);
    }

    /**
     * 心跳处理：响应心跳 + 续期集群路由表
     */
    private void handleHeartbeat(Channel channel) {
        // 构建心跳响应包
        ImProtocol.ImPacket resp = ImProtocol.ImPacket.newBuilder()
                .setMsgType(ImProtocol.MsgType.HEARTBEAT_RESPONSE)
                .setHeartbeatResponse(
                        ImProtocol.HeartbeatResponse.newBuilder()
                                .setTimestamp(System.currentTimeMillis())
                                .build()
                )
                .build();
        channel.writeAndFlush(resp);

        // 续期用户节点映射过期时间
        Long userId = sessionManager.getUserId(channel);
        if (userId != null) {
            String nodeKey = nettyProperties.getUserNodePrefix() + userId;
            redisTemplate.expire(nodeKey, 90, TimeUnit.SECONDS);
        }
    }

    /**
     * 单聊消息处理：本地在线直接转发，离线跨节点发布
     */
    private void handlePrivateMsg(ImProtocol.ImPacket packet) {
        ImProtocol.BusinessMessage message = packet.getPrivateMessage();
        Long targetId = message.getTargetId();

        // 本地在线直接转发
        if (sessionManager.isOnline(targetId)) {
            Channel channel = sessionManager.getChannel(targetId);
            channel.writeAndFlush(packet);
            return;
        }

        // 跨节点：发布到集群消息主题
        clusterMsgPublisher.publishMsg(packet);
    }

    /**
     * 群聊消息处理
     */
    private void handleGroupMsg(ImProtocol.ImPacket packet) {
        // 生产环境：调用群服务拉取群成员ID列表，遍历本地转发 + 跨节点发布
        clusterMsgPublisher.publishMsg(packet);
    }

    /**
     * 登出处理：主动断开连接
     */
    private void handleLogout(Channel channel) {
        channel.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Long userId = sessionManager.getUserId(channel);
        if (userId != null) {
            sessionManager.unbind(channel);
            redisTemplate.delete(nettyProperties.getUserNodePrefix() + userId);
            log.info("用户{}断开连接", userId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常", cause);
        sessionManager.unbind(ctx.channel());
        ctx.close();
    }

    private String getCurrentNodeId() {
        return "127.0.0.1:" + nettyProperties.getPort();
    }
}