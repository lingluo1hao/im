package com.im.taskservice.feign;

import com.im.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 消息服务客户端
@FeignClient("im-message-service")
public interface MessageClient {

    /**
     * 清理过期历史消息
     */
    @PostMapping("/message/internal/cleanExpired")
    R<Integer> cleanExpiredMessage(@RequestParam("keepDays") Integer keepDays);

    /**
     * 补发超时离线消息
     */
    @PostMapping("/message/internal/resendOffline")
    R<Integer> resendOfflineMessage(@RequestParam("maxTimes") Integer maxTimes);
}