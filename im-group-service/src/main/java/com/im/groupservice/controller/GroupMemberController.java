package com.im.groupservice.controller;

import com.im.common.result.R;
import com.im.groupservice.dto.MuteUserReqDTO;
import com.im.groupservice.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/group/member")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @PostMapping("/mute")
    public R<Void> muteUser(@RequestBody MuteUserReqDTO dto) {
        groupMemberService.muteUser(dto);
        return R.ok();
    }

    @PostMapping("/setAdmin")
    public R<Void> setAdmin(Long groupId, Long userId, Integer role) {
        groupMemberService.setAdmin(groupId, userId, role);
        return R.ok();
    }

    @PostMapping("/remove")
    public R<Void> removeMember(Long groupId, Long userId) {
        groupMemberService.removeMember(groupId, userId);
        return R.ok();
    }
}