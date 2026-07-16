package com.im.taskservice.feign;

import com.im.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 推送服务客户端
@FeignClient("im-push-service")
public interface PushClient {

    /**
     * 归档过期推送记录
     */
    @PostMapping("/push/internal/archiveExpired")
    R<Integer> archiveExpiredRecord(@RequestParam("keepDays") Integer keepDays);
}
