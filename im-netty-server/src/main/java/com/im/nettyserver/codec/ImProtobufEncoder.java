package com.im.nettyserver.codec;

import com.im.netty.protocol.protobuf.ImProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImProtobufEncoder extends MessageToByteEncoder<ImProtocol.ImPacket> {

    private static final short MAGIC = 0x1234;
    private static final byte VERSION = 0x01;

    @Override
    protected void encode(ChannelHandlerContext ctx, ImProtocol.ImPacket msg, ByteBuf out) {
        try {
            byte[] bodyBytes = msg.toByteArray();

            // 按帧格式顺序写入
            out.writeShort(MAGIC);          // 魔数
            out.writeByte(VERSION);         // 版本号
            out.writeInt(bodyBytes.length); // 消息体长度
            out.writeBytes(bodyBytes);      // Protobuf消息体
        } catch (Exception e) {
            log.error("Protobuf消息编码失败", e);
        }
    }
}