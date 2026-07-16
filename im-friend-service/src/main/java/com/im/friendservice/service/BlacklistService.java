package com.im.friendservice.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.im.friendservice.entity.FriendBlacklist;

public interface BlacklistService extends IService<FriendBlacklist> {

    /**
     * 拉黑用户（自动解除好友关系）
     */
    void addBlacklist(Long blackUserId);

    /**
     * 移出黑名单
     */
    void removeBlacklist(Long blackUserId);
}
