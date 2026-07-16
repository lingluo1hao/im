package com.im.messageservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 消息状态枚举
@Getter
@AllArgsConstructor
public enum MessageStatusEnum {
    NORMAL(1, "正常"),
    RECALLED(2, "已撤回");

    private final Integer code;
    private final String desc;
}