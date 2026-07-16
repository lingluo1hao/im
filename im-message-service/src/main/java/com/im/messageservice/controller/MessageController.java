package com.im.messageservice.controller;

import com.im.common.result.R;
import com.im.messageservice.dto.MessagePullDTO;
import com.im.messageservice.dto.MessageRecallDTO;
import com.im.messageservice.dto.MessageSendDTO;
import com.im.messageservice.entity.ImMessage;
import com.im.messageservice.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 消息控制器
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    public R<Long> sendMessage(@RequestBody MessageSendDTO dto) {
        return R.ok(messageService.sendMessage(dto));
    }

    @PostMapping("/pull")
    public R<List<ImMessage>> pullMessage(@RequestBody MessagePullDTO dto) {
        return R.ok(messageService.pullMessage(dto));
    }

    @PostMapping("/recall")
    public R<Void> recallMessage(@RequestBody MessageRecallDTO dto) {
        messageService.recallMessage(dto);
        return R.ok();
    }
}
