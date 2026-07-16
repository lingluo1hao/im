package com.im.imuserservice.controller;

import com.im.common.result.R;
import com.im.imuserservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/internal")
@RequiredArgsConstructor
public class InternalAuthController {

    private final UserService userService;

    /**
     * 内部接口：校验Token并返回用户ID（仅供服务间调用）
     */
    @PostMapping("/validateToken")
    public R<Long> validateToken(@RequestParam("token") String token) {
        Long userId = userService.validateTokenAndGetUserId(token);
        return R.ok(userId);
    }
}