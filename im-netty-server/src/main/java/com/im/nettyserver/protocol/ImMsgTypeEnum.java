package com.im.nettyserver.protocol;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImMsgTypeEnum {
    LOGIN_REQUEST(1, "登录请求"),
    LOGIN_RESPONSE(2, "登录响应"),
    HEARTBEAT_REQUEST(3, "心跳请求"),
    HEARTBEAT_RESPONSE(4, "心跳响应"),
    PRIVATE_MSG(5, "单聊消息"),
    GROUP_MSG(6, "群聊消息"),
    LOGOUT(7, "登出");

    private final Integer code;
    private final String desc;

    public static ImMsgTypeEnum getByCode(Integer code) {
        for (ImMsgTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}