package com.im.nettyserver.server;

import com.im.nettyserver.codec.ImProtobufDecoder;
import com.im.nettyserver.codec.ImProtobufEncoder;
import com.im.nettyserver.config.NettyProperties;
import com.im.nettyserver.handler.HeartBeatHandler;
import com.im.nettyserver.handler.ImMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NettyTcpServer {

    private final NettyProperties nettyProperties;
    private final HeartBeatHandler heartBeatHandler;
    private final ImMessageHandler imMessageHandler;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    @PostConstruct
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 1. 空闲检测：读空闲超时触发断连
                            pipeline.addLast(new IdleStateHandler(
                                    nettyProperties.getReaderIdleTime(), 0, 0, TimeUnit.SECONDS));
                            // 2. Protobuf 协议编解码器（替换原自定义JSON编解码器）
                            pipeline.addLast(new ImProtobufDecoder());
                            pipeline.addLast(new ImProtobufEncoder());
                            // 3. 心跳与业务处理器
                            pipeline.addLast(heartBeatHandler);
                            pipeline.addLast(imMessageHandler);
                        }
                    });

            bootstrap.bind(nettyProperties.getPort()).sync();
            log.info("Netty 长连接服务启动成功，端口: {}", nettyProperties.getPort());
        } catch (InterruptedException e) {
            log.error("Netty 服务启动失败", e);
            stop();
        }
    }

    @PreDestroy
    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty 服务已关闭");
    }
}