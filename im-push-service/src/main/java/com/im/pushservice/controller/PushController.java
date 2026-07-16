package com.im.pushservice.controller;


import com.im.common.result.R;
import com.im.pushservice.dto.AllUserPushDTO;
import com.im.pushservice.dto.BatchPushDTO;
import com.im.pushservice.dto.SinglePushDTO;
import com.im.pushservice.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 推送控制器
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;

    /**
     * 单用户推送（供消息服务离线触发调用）
     */
    @PostMapping("/single")
    public R<Void> singlePush(@RequestBody SinglePushDTO dto) {
        pushService.singlePush(dto);
        return R.ok();
    }

    /**
     * 批量推送
     */
    @PostMapping("/batch")
    public R<Void> batchPush(@RequestBody BatchPushDTO dto) {
        pushService.batchPush(dto);
        return R.ok();
    }

    /**
     * 全员推送
     */
    @PostMapping("/all")
    public R<Long> allUserPush(@RequestBody AllUserPushDTO dto) {
        return R.ok(pushService.allUserPush(dto));
    }
}