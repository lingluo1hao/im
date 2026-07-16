package com.im.pushservice.dto;

import lombok.Data;

// 设备绑定入参
@Data
public class DeviceBindDTO {
    private String deviceId;
    private Integer deviceType;
    private Integer pushChannel;
    private String deviceToken;
}