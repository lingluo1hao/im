package com.im.pushservice.controller;

import com.im.common.result.R;
import com.im.pushservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/device/internal")
@RequiredArgsConstructor
public class InternalDeviceController {

    private final DeviceService deviceService;

    /**
     * 清理超期离线设备（仅供定时任务内部调用）
     */
    @PostMapping("/cleanExpired")
    public R<Integer> cleanExpiredDevice(@RequestParam("expireDays") Integer expireDays) {
        return R.ok(deviceService.cleanExpiredDevice(expireDays));
    }
}