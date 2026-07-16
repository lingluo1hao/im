package com.im.friendservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FriendCacheService {

    private final StringRedisTemplate redisTemplate;

    // 精准对齐文档中的 Key 前缀设计
    private static final String FRIEND_LIST_PREFIX = "friend:list:";
    private static final String FRIEND_BLACKLIST_PREFIX = "friend:blacklist:";
    private static final long CACHE_TTL_DAYS = 7; // 缓存设置7天过期策略

    /* ==================== 1. 好友列表缓存操纵 ==================== */

    public Set<String> getFriendCache(Long userId) {
        return redisTemplate.opsForSet().members(FRIEND_LIST_PREFIX + userId);
    }

    public void setFriendCache(Long userId, Set<String> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) return;
        String key = FRIEND_LIST_PREFIX + userId;
        // 批量装载进 Set 集合
        redisTemplate.opsForSet().add(key, friendIds.toArray(new String[0]));
        redisTemplate.expire(key, CACHE_TTL_DAYS, TimeUnit.DAYS);
    }

    public void delFriendCache(Long userId) {
        redisTemplate.delete(FRIEND_LIST_PREFIX + userId);
    }

    /* ==================== 2. 黑名单缓存操纵 ==================== */

    public Set<String> getBlacklistCache(Long userId) {
        return redisTemplate.opsForSet().members(FRIEND_BLACKLIST_PREFIX + userId);
    }

    public void setBlacklistCache(Long userId, Set<String> blackIds) {
        if (blackIds == null || blackIds.isEmpty()) return;
        String key = FRIEND_BLACKLIST_PREFIX + userId;
        redisTemplate.opsForSet().add(key, blackIds.toArray(new String[0]));
        redisTemplate.expire(key, CACHE_TTL_DAYS, TimeUnit.DAYS);
    }

    public void delBlacklistCache(Long userId) {
        redisTemplate.delete(FRIEND_BLACKLIST_PREFIX + userId);
    }

    /* ==================== 3. 强力布控拦截断路器 ==================== */

    /**
     * 判断 userId 是否被 targetId 拉黑
     * 核心逻辑：检查 targetId 的黑名单集合中是否包含 userId
     */
    public boolean isBlocked(Long userId, Long targetId) {
        String targetBlackKey = FRIEND_BLACKLIST_PREFIX + targetId;

        // 1. 先查缓存
        Boolean isMember = redisTemplate.opsForSet().isMember(targetBlackKey, String.valueOf(userId));
        if (isMember != null) {
            return isMember;
        }

        // 2. 如果缓存为空，可视为不拦截，或由下游 DB 兜底进行物理隔离判断
        return false;
    }
}
