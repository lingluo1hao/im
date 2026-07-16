package com.im.pushservice.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PushStatusEnum {
    PUSHING(1, "推送中"),
    SUCCESS(2, "推送成功"),
    FAIL(3, "推送失败");

    private final Integer code;
    private final String desc;
}