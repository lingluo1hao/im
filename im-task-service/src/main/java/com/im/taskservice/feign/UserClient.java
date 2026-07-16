package com.im.taskservice.feign;

import com.im.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("im-user-service")
public interface UserClient {

    /**
     * 批量获取用户ID列表（用于全员推送、全量数据统计等场景）
     */
    @GetMapping("/user/internal/listUserId")
    R<List<Long>> listUserId(@RequestParam("lastId") Long lastId, @RequestParam("pageSize") Integer pageSize);

    /**
     * 获取用户总数
     */
    @GetMapping("/user/internal/count")
    R<Long> getUserCount();
}