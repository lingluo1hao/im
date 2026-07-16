package com.im.friendservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.context.UserContext;
import com.im.friendservice.entity.FriendBlacklist;
import com.im.friendservice.mapper.FriendBlacklistMapper;
import com.im.friendservice.service.BlacklistService;
import com.im.friendservice.service.FriendCacheService;
import com.im.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl extends ServiceImpl<FriendBlacklistMapper, FriendBlacklist> implements BlacklistService {

    private final FriendService friendService;
    private final FriendCacheService cacheService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBlacklist(Long blackUserId) {
        Long userId = UserContext.getUserId();

        // 1. 写入黑名单表
        FriendBlacklist blacklist = new FriendBlacklist();
        blacklist.setUserId(userId);
        blacklist.setBlackUserId(blackUserId);

        try {
            this.save(blacklist);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("该用户已在您的黑名单中");
        }

        // 2. 自动解除双向好友关系（将关系表中状态改为 2-已删除）
        friendService.removeFriend(blackUserId);

        // 3. 实时擦除相关缓存
        cacheService.delFriendCache(userId);
        cacheService.delBlacklistCache(userId);
    }

    @Override
    public void removeBlacklist(Long blackUserId) {
        Long userId = UserContext.getUserId();

        // 依据联合唯一索引完美精准删除
        this.remove(new LambdaQueryWrapper<FriendBlacklist>()
                .eq(FriendBlacklist::getUserId, userId)
                .eq(FriendBlacklist::getBlackUserId, blackUserId));

        // 擦除缓存，下次读取自动从 DB 刷新
        cacheService.delBlacklistCache(userId);
    }
}
