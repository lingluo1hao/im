package com.im.messageservice.controller;


import com.im.common.result.R;
import com.im.messageservice.dto.MessageReadDTO;
import com.im.messageservice.entity.ImConversation;
import com.im.messageservice.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 会话控制器
@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/list")
    public R<List<ImConversation>> listConversation() {
        return R.ok(conversationService.listConversation());
    }

    @PostMapping("/markRead")
    public R<Void> markRead(@RequestBody MessageReadDTO dto) {
        conversationService.markRead(dto);
        return R.ok();
    }
}