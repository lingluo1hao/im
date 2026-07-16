package com.im.friendservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.context.UserContext;
import com.im.friendservice.entity.FriendRelation;
import com.im.friendservice.mapper.FriendRelationMapper;
import com.im.friendservice.service.FriendCacheService;
import com.im.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendRelationMapper, FriendRelation> implements FriendService {

    private final FriendCacheService cacheService;

    @Override
    public Set<Long> getFriendIds(Long userId) {
        // 1. 先查 Redis 旁路缓存
        Set<String> cachedIds = cacheService.getFriendCache(userId);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            return cachedIds.stream().map(Long::parseLong).collect(Collectors.toSet());
        }

        // 2. 缓存未命中查数据库，精确匹配 status = 1 (正常)
        Set<Long> dbIds = this.list(new LambdaQueryWrapper<FriendRelation>()
                        .eq(FriendRelation::getUserId, userId)
                        .eq(FriendRelation::getStatus, 1))
                .stream().map(FriendRelation::getFriendId).collect(Collectors.toSet());

        // 3. 回写 Redis 缓存，并设置过期时间
        if (!dbIds.isEmpty()) {
            cacheService.setFriendCache(userId, dbIds.stream().map(String::valueOf).collect(Collectors.toSet()));
        }
        return dbIds;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeFriend(Long friendId) {
        Long userId = UserContext.getUserId();

        // ⚡ 完美匹配 DDL：更新状态为 2 (已删除)
        this.update(new LambdaUpdateWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, userId).eq(FriendRelation::getFriendId, friendId)
                .set(FriendRelation::getStatus, 2));

        this.update(new LambdaUpdateWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, friendId).eq(FriendRelation::getFriendId, userId)
                .set(FriendRelation::getStatus, 2));

        // 激进擦除缓存，确保最终一致性
        cacheService.delFriendCache(userId);
        cacheService.delFriendCache(friendId);
    }

    @Override
    public void updateRemark(Long friendId, String remark) {
        Long userId = UserContext.getUserId();

        this.update(new LambdaUpdateWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId)
                .set(FriendRelation::getRemark, remark));

        // 备注仅当前用户可见，只删当前用户的缓存
        cacheService.delFriendCache(userId);
    }
}
