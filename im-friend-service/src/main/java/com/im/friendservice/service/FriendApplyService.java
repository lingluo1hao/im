package com.im.friendservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.im.friendservice.entity.FriendApply;

public interface FriendApplyService extends IService<FriendApply> {

    /**
     * 提交好友申请（含黑名单拦截与幂等控制）
     */
    void submitApply(Long toUserId, String applyMsg);

    /**
     * 处理好友申请（同意/拒绝）
     */
    void handleApply(Long applyId, Integer status);
}
