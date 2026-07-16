package com.im.nettyserver.codec;

import com.im.netty.protocol.protobuf.ImProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImProtobufDecoder extends LengthFieldBasedFrameDecoder {

    private static final short MAGIC = 0x1234;
    private static final byte VERSION = 0x01;

    public ImProtobufDecoder() {
        // 最大帧1MB，长度域偏移3字节（魔数2+版本1），长度域占4字节
        super(1024 * 1024, 3, 4, 0, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        try {
            // 1. 校验魔数
            short magic = frame.readShort();
            if (magic != MAGIC) {
                log.error("非法数据包，魔数不匹配: {}", magic);
                ctx.close();
                return null;
            }

            // 2. 校验版本
            byte version = frame.readByte();
            if (version != VERSION) {
                log.error("不支持的协议版本: {}", version);
                ctx.close();
                return null;
            }

            // 3. 读取消息体长度并跳过（长度域已由父类处理拆包，此处仅消费字节）
            frame.readInt();

            // 4. Protobuf 反序列化
            byte[] bodyBytes = new byte[frame.readableBytes()];
            frame.readBytes(bodyBytes);
            return ImProtocol.ImPacket.parseFrom(bodyBytes);

        } catch (Exception e) {
            log.error("Protobuf消息解码失败", e);
            ctx.close();
            return null;
        } finally {
            frame.release();
        }
    }
}