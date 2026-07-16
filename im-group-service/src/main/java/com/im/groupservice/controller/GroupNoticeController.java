package com.im.groupservice.controller;


import com.im.common.result.R;
import com.im.groupservice.dto.GroupNoticeReqDTO;
import com.im.groupservice.entity.ImGroupNotice;
import com.im.groupservice.service.GroupNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group/notice")
@RequiredArgsConstructor
public class GroupNoticeController {

    private final GroupNoticeService groupNoticeService;

    /**
     * 发布公告
     */
    @PostMapping("/publish")
    public R<Long> publishNotice(@RequestBody GroupNoticeReqDTO dto) {
        return R.ok(groupNoticeService.publishNotice(dto));
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/delete")
    public R<Void> deleteNotice(Long groupId, Long noticeId) {
        groupNoticeService.deleteNotice(groupId, noticeId);
        return R.ok();
    }

    /**
     * 查询群公告列表
     */
    @GetMapping("/list")
    public R<List<ImGroupNotice>> getNoticeList(Long groupId) {
        return R.ok(groupNoticeService.getNoticeList(groupId));
    }
}