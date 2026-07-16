package com.im.friendservice.feign;

import com.im.common.dto.UserInfoDTO;
import com.im.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * ⚡ 远程调用用户微服务客户端
 * value: 对应 Nacos 中注册的用户业务微服务名称 "im-user-service"
 * contextId: 显式指定 Bean 的唯一标识，防止微服务中存在多个 FeignClient 导致启动冲突
 */
@FeignClient(value = "im-user-service", contextId = "UserFeignClient")
public interface UserFeignClient {

    /**
     * 核心高性能规约：根据一组用户 ID 集合，跨服务批量拉取对应的用户基本详情
     * 作用：供 FriendController 异步装配好友列表，将传统的 N+1 次循环网络 IO 刚性压缩为 1 次批量聚合
     *
     * @param userIds 好友 ID 集合
     * @return 聚合 Map (Key: 用户ID, Value: 用户基本信息结构体)
     */
    @PostMapping("/user/internal/list/map")
    R<Map<Long, UserInfoDTO>> getUserInfoMapByIds(@RequestBody List<Long> userIds);
}
