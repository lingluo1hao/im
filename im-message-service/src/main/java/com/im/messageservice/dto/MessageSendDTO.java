package com.im.messageservice.dto;

import lombok.Data;

// 消息发送入参
@Data
public class MessageSendDTO {
    private Integer conversationType;
    private Long targetId;
    private Integer msgType;
    private String content;
}