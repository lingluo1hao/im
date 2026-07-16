package com.im.groupservice.controller;

import com.im.common.result.R;
import com.im.groupservice.dto.CreateGroupReqDTO;
import com.im.groupservice.entity.ImGroup;
import com.im.groupservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public R<Long> createGroup(@RequestBody CreateGroupReqDTO dto) {
        return R.ok(groupService.createGroup(dto));
    }

    @PostMapping("/allMute")
    public R<Void> setAllMute(Long groupId, Integer isAllMute) {
        groupService.setAllMute(groupId, isAllMute);
        return R.ok();
    }

    @PostMapping("/dissolve")
    public R<Void> dissolveGroup(Long groupId) {
        groupService.dissolveGroup(groupId);
        return R.ok();
    }

    @GetMapping("/info")
    public R<ImGroup> getGroupInfo(Long groupId) {
        return R.ok(groupService.getGroupById(groupId));
    }
}