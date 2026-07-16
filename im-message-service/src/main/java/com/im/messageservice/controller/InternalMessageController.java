package com.im.messageservice.controller;


import com.im.common.result.R;
import com.im.messageservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message/internal")
@RequiredArgsConstructor
public class InternalMessageController {

    private final MessageService messageService;

    /**
     * 清理过期历史消息（仅供定时任务内部调用）
     */
    @PostMapping("/cleanExpired")
    public R<Integer> cleanExpiredMessage(@RequestParam("keepDays") Integer keepDays) {
        return R.ok(messageService.cleanExpiredMessage(keepDays));
    }

    /**
     * 补发离线消息（仅供定时任务内部调用）
     */
    @PostMapping("/resendOffline")
    public R<Integer> resendOfflineMessage(@RequestParam("maxTimes") Integer maxTimes) {
        return R.ok(messageService.resendOfflineMessage(maxTimes));
    }
}