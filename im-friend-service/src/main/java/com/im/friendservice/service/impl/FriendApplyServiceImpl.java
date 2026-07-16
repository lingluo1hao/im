package com.im.friendservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.context.UserContext;
import com.im.friendservice.entity.FriendApply;
import com.im.friendservice.entity.FriendRelation;
import com.im.friendservice.mapper.FriendApplyMapper;
import com.im.friendservice.service.FriendApplyService;
import com.im.friendservice.service.FriendCacheService;
import com.im.friendservice.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FriendApplyServiceImpl extends ServiceImpl<FriendApplyMapper, FriendApply> implements FriendApplyService {

    private final FriendService friendService;
    private final FriendCacheService cacheService;

    @Override
    public void submitApply(Long toUserId, String applyMsg) {
        Long fromUserId = UserContext.getUserId();

        // 1. 前置安全校验：是否在对方黑名单中
        if (cacheService.isBlocked(fromUserId, toUserId)) {
            throw new RuntimeException("发送申请失败，已被对方拉黑");
        }

        // 2. 业务级判断：是否已经是正常好友关系
        long isFriend = friendService.count(new LambdaQueryWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, fromUserId)
                .eq(FriendRelation::getFriendId, toUserId)
                .eq(FriendRelation::getStatus, 1));
        if (isFriend > 0) {
            throw new RuntimeException("你们已经是好友了");
        }

        // 3. 构建申请单并入库
        FriendApply apply = new FriendApply();
        apply.setFromUserId(fromUserId);
        apply.setToUserId(toUserId);
        apply.setApplyMsg(applyMsg); // 适配字段名
        apply.setStatus(0); // 0-待处理

        try {
            this.save(apply);
        } catch (DuplicateKeyException e) {
            // 🔒 联合唯一索引 uk_from_to 底层兜底拦截并发重复点击
            throw new RuntimeException("已有相同的待处理申请，请勿重复发起");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handleApply(Long applyId, Integer status) {
        Long currentUserId = UserContext.getUserId();
        FriendApply apply = this.getById(applyId);

        if (apply == null || !apply.getToUserId().equals(currentUserId) || apply.getStatus() != 0) {
            throw new RuntimeException("申请单不存在、状态已变更或无权处理");
        }

        // 1. 更新申请状态与处理时间
        apply.setStatus(status);
        apply.setHandleTime(LocalDateTime.now()); // 适配 handle_time 字段
        this.updateById(apply);

        // 2. 如果同意 (status == 1)，在事务内建立双向好友关系
        if (status == 1) {
            // 采用 saveOrUpdate 机制适配：如果之前删除过（存在 status=2 历史记录），则转为更新，避免 uk_user_friend 冲突
            saveOrUpdateRelation(apply.getFromUserId(), apply.getToUserId());
            saveOrUpdateRelation(apply.getToUserId(), apply.getFromUserId());

            // 3. 激进式缓存双删，确保数据完全干净
            cacheService.delFriendCache(apply.getFromUserId());
            cacheService.delFriendCache(apply.getToUserId());
            cacheService.delBlacklistCache(apply.getFromUserId());
            cacheService.delBlacklistCache(apply.getToUserId());
        }
    }

    private void saveOrUpdateRelation(Long userId, Long friendId) {
        FriendRelation exist = friendService.getOne(new LambdaQueryWrapper<FriendRelation>()
                .eq(FriendRelation::getUserId, userId)
                .eq(FriendRelation::getFriendId, friendId));
        if (exist != null) {
            exist.setStatus(1); // 重新标记为 1 (正常)
            friendService.updateById(exist);
        } else {
            FriendRelation rel = new FriendRelation();
            rel.setUserId(userId);
            rel.setFriendId(friendId);
            rel.setStatus(1); // 1-正常
            friendService.save(rel);
        }
    }
}
