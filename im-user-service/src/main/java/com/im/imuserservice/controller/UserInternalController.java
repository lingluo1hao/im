package com.im.imuserservice.controller;


import com.im.common.dto.UserInfoDTO;
import com.im.common.result.R;
import com.im.imuserservice.entity.User;
import com.im.imuserservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @PostMapping("/list/map")
    public R<Map<Long, UserInfoDTO>> getUserInfoMapByIds(@RequestBody List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return R.ok(Collections.emptyMap());
        }

        log.info("[RPC-安全透传流] 收到来自友邻微服务的高并发批量用户拉取，ID 数量: {}", userIds.size());

        // 1. 调用刚刚补齐的业务方法，获取清洗脱敏后的 DTO 列表
        List<UserInfoDTO> dtoList = userService.listUserInfoByIds(userIds);

        // 2. 转换为高效的内存检索 Map (O(1) 复杂度)
        Map<Long, UserInfoDTO> resultMap = dtoList.stream()
                .collect(Collectors.toMap(
                        UserInfoDTO::getId,
                        dto -> dto,
                        (existing, replacement) -> existing // 锁死幂等，防止 ID 重复引发崩溃
                ));

        return R.ok(resultMap);
    }
}
