package com.im.pushservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeviceTypeEnum {
    ANDROID(1, "安卓"),
    IOS(2, "iOS"),
    WEB(3, "Web端"),
    PC(4, "PC端");

    private final Integer code;
    private final String desc;
}