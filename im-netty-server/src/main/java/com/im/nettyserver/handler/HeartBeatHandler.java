package com.im.nettyserver.handler;


import com.im.nettyserver.manager.ImSessionManager;
import com.im.nettyserver.protocol.ImMessage;
import com.im.nettyserver.protocol.ImMsgTypeEnum;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final ImSessionManager sessionManager;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                // 读空闲超时，断开连接
                log.info("用户心跳超时，断开连接: {}", ctx.channel().id());
                sessionManager.unbind(ctx.channel());
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ImMessage message = (ImMessage) msg;
        if (ImMsgTypeEnum.HEARTBEAT_REQUEST.getCode().equals(message.getMsgType())) {
            // 收到心跳请求，直接响应
            ImMessage resp = new ImMessage();
            resp.setMsgType(ImMsgTypeEnum.HEARTBEAT_RESPONSE.getCode());
            ctx.writeAndFlush(resp);
            return;
        }
        super.channelRead(ctx, msg);
    }
}