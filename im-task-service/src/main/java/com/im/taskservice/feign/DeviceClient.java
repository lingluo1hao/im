package com.im.taskservice.feign;

import com.im.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 用户设备服务客户端
@FeignClient("im-push-service")
public interface DeviceClient {

    /**
     * 清理超期离线设备
     */
    @PostMapping("/device/internal/cleanExpired")
    R<Integer> cleanExpiredDevice(@RequestParam("expireDays") Integer expireDays);
}