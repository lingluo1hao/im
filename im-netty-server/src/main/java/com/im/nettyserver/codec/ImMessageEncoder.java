package com.im.nettyserver.codec;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.nettyserver.protocol.ImMessage;
import com.im.nettyserver.protocol.ImProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImMessageEncoder extends MessageToByteEncoder<ImMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, ImMessage msg, ByteBuf out) throws Exception {
        try {
            byte[] bodyBytes = objectMapper.writeValueAsBytes(msg);

            // 按协议顺序写入
            out.writeShort(ImProtocol.MAGIC);       // 魔数
            out.writeByte(ImProtocol.VERSION);      // 版本号
            out.writeByte(msg.getMsgType());        // 指令类型
            out.writeInt(bodyBytes.length);         // 消息体长度
            out.writeBytes(bodyBytes);              // 消息体
        } catch (Exception e) {
            log.error("消息编码失败", e);
        }
    }
}