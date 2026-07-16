package com.im.pushservice.dto;

import lombok.Data;

// 单用户推送入参
@Data
public class SinglePushDTO {
    private Long userId;
    private String title;
    private String content;
    private Long bizMsgId; // 业务消息ID，幂等用
}