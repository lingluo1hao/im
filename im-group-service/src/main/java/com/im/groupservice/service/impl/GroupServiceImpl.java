package com.im.groupservice.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.exception.BusinessException;
import com.im.common.context.UserContext;
import com.im.groupservice.dto.CreateGroupReqDTO;
import com.im.groupservice.entity.ImGroup;
import com.im.groupservice.entity.ImGroupMember;
import com.im.groupservice.enums.GroupRoleEnum;
import com.im.groupservice.mapper.GroupMapper;
import com.im.groupservice.service.GroupMemberService;
import com.im.groupservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, ImGroup> implements GroupService {

    private final GroupMemberService groupMemberService;
    private final StringRedisTemplate redisTemplate;

    private static final String GROUP_CACHE_KEY = "group:info:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(CreateGroupReqDTO dto) {
        Long userId = UserContext.getUserId();

        // 1. 插入群组主记录
        ImGroup group = new ImGroup();
        group.setGroupName(dto.getGroupName());
        group.setGroupDesc(dto.getGroupDesc());
        group.setOwnerId(userId);
        group.setIsAllMute(0);
        group.setStatus(1);
        this.save(group);

        // 2. 群主加入成员表
        ImGroupMember owner = new ImGroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setRole(GroupRoleEnum.OWNER.getCode());
        groupMemberService.save(owner);

        // 3. 删除缓存
        deleteGroupCache(group.getId());
        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setAllMute(Long groupId, Integer isAllMute) {
        // 权限校验：仅群主可操作
        groupMemberService.checkGroupOwner(groupId, UserContext.getUserId());

        ImGroup group = new ImGroup();
        group.setId(groupId);
        group.setIsAllMute(isAllMute);
        this.updateById(group);

        deleteGroupCache(groupId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroup(Long groupId) {
        Long userId = UserContext.getUserId();
        ImGroup group = getGroupById(groupId);

        if (!group.getOwnerId().equals(userId)) {
            throw new BusinessException("仅群主可解散群组");
        }

        // 更新状态为已解散
        group.setStatus(2);
        this.updateById(group);

        // 移除所有群成员
        LambdaUpdateWrapper<ImGroupMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId);
        groupMemberService.remove(wrapper);

        deleteGroupCache(groupId);
    }

    @Override
    public ImGroup getGroupById(Long groupId) {
        // 先查缓存
        String cacheKey = GROUP_CACHE_KEY + groupId;
        String cache = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cache)) {
            return JSONUtil.toBean(cache, ImGroup.class);
        }

        ImGroup group = this.getById(groupId);
        if (group == null || group.getStatus() != 1) {
            throw new BusinessException("群组不存在或已解散");
        }

        // 写入缓存，过期时间1小时
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(group), 1, TimeUnit.HOURS);
        return group;
    }

    /**
     * 删除群组缓存
     */
    private void deleteGroupCache(Long groupId) {
        redisTemplate.delete(GROUP_CACHE_KEY + groupId);
    }
}