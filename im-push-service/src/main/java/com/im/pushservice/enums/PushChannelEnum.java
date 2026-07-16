package com.im.pushservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PushChannelEnum {
    JPUSH(1, "极光推送"),
    HUAWEI(2, "华为推送"),
    XIAOMI(3, "小米推送"),
    APNs(4, "苹果APNs"),
    OPPO(5, "OPPO推送"),
    VIVO(6, "VIVO推送");

    private final Integer code;
    private final String desc;
}