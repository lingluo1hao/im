package com.im.nettyserver.server;

import com.im.nettyserver.codec.ImProtobufDecoder;
import com.im.nettyserver.codec.ImProtobufEncoder;
import com.im.nettyserver.config.NettyProperties;
import com.im.nettyserver.handler.HeartBeatHandler;
import com.im.nettyserver.handler.ImMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
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
        int workerThreads = nettyProperties.getWorkerThreads() > 0
                ? nettyProperties.getWorkerThreads() : Runtime.getRuntime().availableProcessors() * 4;
        bossGroup = new NioEventLoopGroup(nettyProperties.getBossThreads());
        workerGroup = new NioEventLoopGroup(workerThreads);

        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 4096)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new io.netty.channel.WriteBufferWaterMark(32 * 1024, 512 * 1024))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(
                                    nettyProperties.getReaderIdleTime(), 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new ImProtobufDecoder());
                            pipeline.addLast(new ImProtobufEncoder());
                            pipeline.addLast(heartBeatHandler);
                            pipeline.addLast(imMessageHandler);
                        }
                    });

            bootstrap.bind(nettyProperties.getPort()).sync();
            log.info("Netty 长连接服务启动成功，端口: {}，worker线程数: {}", nettyProperties.getPort(), workerThreads);
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
