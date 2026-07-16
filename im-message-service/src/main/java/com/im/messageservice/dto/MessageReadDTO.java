package com.im.messageservice.dto;

import lombok.Data;

// 消息已读入参
@Data
public class MessageReadDTO {
    private String conversationId;
    Long lastReadMsgId;
}