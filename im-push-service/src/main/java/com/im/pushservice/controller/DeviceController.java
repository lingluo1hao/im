package com.im.pushservice.controller;

import com.im.common.result.R;
import com.im.pushservice.dto.DeviceBindDTO;
import com.im.pushservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 设备管理控制器
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/bind")
    public R<Void> bindDevice(@RequestBody DeviceBindDTO dto) {
        deviceService.bindDevice(dto);
        return R.ok();
    }

    @PostMapping("/unbind")
    public R<Void> unbindDevice(String deviceId) {
        deviceService.unbindDevice(deviceId);
        return R.ok();
    }
}