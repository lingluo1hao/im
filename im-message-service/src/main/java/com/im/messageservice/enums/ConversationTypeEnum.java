package com.im.messageservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 会话类型枚举
@Getter
@AllArgsConstructor
public enum ConversationTypeEnum {
    PRIVATE(1, "单聊"),
    GROUP(2, "群聊");

    private final Integer code;
    private final String desc;
}