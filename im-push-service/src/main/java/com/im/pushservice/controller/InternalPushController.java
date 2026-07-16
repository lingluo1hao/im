package com.im.pushservice.controller;


import com.im.common.result.R;
import com.im.pushservice.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/push/internal")
@RequiredArgsConstructor
public class InternalPushController {

    private final PushService pushService;

    /**
     * 归档过期推送记录（仅供定时任务内部调用）
     */
    @PostMapping("/archiveExpired")
    public R<Integer> archiveExpiredRecord(@RequestParam("keepDays") Integer keepDays) {
        return R.ok(pushService.archiveExpiredRecord(keepDays));
    }
}