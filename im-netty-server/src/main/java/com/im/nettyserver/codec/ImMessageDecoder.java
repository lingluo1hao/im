package com.im.nettyserver.codec;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.nettyserver.protocol.ImMessage;
import com.im.nettyserver.protocol.ImProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImMessageDecoder extends LengthFieldBasedFrameDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImMessageDecoder() {
        // 最大帧1MB，长度域偏移4字节，长度域占4字节
        super(1024 * 1024, 4, 4, 0, 0);
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
            if (magic != ImProtocol.MAGIC) {
                log.error("非法数据包，魔数不匹配: {}", magic);
                ctx.close();
                return null;
            }

            // 2. 读取版本、指令类型
            frame.readByte(); // 版本号，暂不校验
            int msgType = frame.readByte();

            // 3. 读取消息体长度与内容
            int bodyLength = frame.readInt();
            byte[] bodyBytes = new byte[bodyLength];
            frame.readBytes(bodyBytes);

            // 4. 反序列化为消息对象
            ImMessage message = objectMapper.readValue(bodyBytes, ImMessage.class);
            message.setMsgType(msgType);
            return message;
        } catch (Exception e) {
            log.error("消息解码失败", e);
            ctx.close();
            return null;
        } finally {
            frame.release();
        }
    }
}