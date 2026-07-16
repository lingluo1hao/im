package com.im.nettyserver.protocol;


import lombok.Data;

@Data
public class ImMessage {
    /**
     * 指令类型
     */
    private Integer msgType;

    /**
     * 发送方用户ID（登录时为空）
     */
    private Long fromUserId;

    /**
     * 接收方用户ID/群组ID
     */
    private Long targetId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 登录凭证token
     */
    private String token;
}