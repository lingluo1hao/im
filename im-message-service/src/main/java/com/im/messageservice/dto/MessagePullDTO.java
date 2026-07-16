package com.im.messageservice.dto;

import lombok.Data;

// 消息拉取入参
@Data
public class MessagePullDTO {
    private String conversationId;
    private Long lastMsgId; // 上次拉取的最后消息ID，0表示从头拉
    private Integer pageSize = 20;
}