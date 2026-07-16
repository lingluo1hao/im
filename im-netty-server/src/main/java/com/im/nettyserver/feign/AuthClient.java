package com.im.nettyserver.feign;

import com.im.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 原：@FeignClient("im-auth-service")
@FeignClient("im-user-service")
public interface AuthClient {

    /**
     * 校验Token并返回用户ID
     * @param token 登录凭证
     * @return 用户ID
     */
    // 原：@PostMapping("/auth/internal/validateToken")
    @PostMapping("/user/internal/validateToken")
    R<Long> validateToken(@RequestParam("token") String token);
}