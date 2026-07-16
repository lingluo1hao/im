package com.im.friendservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.im.common.context.UserContext;
import com.im.common.result.R;
import com.im.friendservice.dto.AddFriendReqDTO;
import com.im.friendservice.dto.HandleApplyReqDTO;
import com.im.friendservice.entity.FriendApply;
import com.im.friendservice.service.FriendApplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friend/apply")
@RequiredArgsConstructor
public class FriendApplyController {

    private final FriendApplyService friendApplyService;

    /**
     * 发起好友申请（优雅承接 AddFriendReqDTO）
     */
    @PostMapping("/submit")
    public R<?> submitApply(@Validated @RequestBody AddFriendReqDTO reqDTO) {
        friendApplyService.submitApply(reqDTO.getToUserId(), reqDTO.getApplyMsg());
        return R.ok("申请已发送，请等待对方处理");
    }

    /**
     * 处理好友申请（同意/拒绝，优雅承接 HandleApplyReqDTO）
     */
    @PutMapping("/handle")
    public R<?> handleApply(@Validated @RequestBody HandleApplyReqDTO reqDTO) {
        friendApplyService.handleApply(reqDTO.getApplyId(), reqDTO.getStatus());
        return R.ok(reqDTO.getStatus() == 1 ? "已添加对方为好友" : "已拒绝该申请");
    }

    /**
     * 分页查询当前用户收到的待处理申请列表
     */
    @GetMapping("/list/pending")
    public R<Page<FriendApply>> getPendingApplyList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = UserContext.getUserId();

        Page<FriendApply> page = new Page<>(current, size);
        LambdaQueryWrapper<FriendApply> queryWrapper = new LambdaQueryWrapper<FriendApply>()
                .eq(FriendApply::getToUserId, currentUserId)
                .eq(FriendApply::getStatus, 0)
                .orderByDesc(FriendApply::getCreateTime);

        return R.ok(friendApplyService.page(page, queryWrapper));
    }
}
