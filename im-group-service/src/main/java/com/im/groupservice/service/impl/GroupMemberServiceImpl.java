package com.im.groupservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.exception.BusinessException;
import com.im.common.context.UserContext;
import com.im.groupservice.dto.MuteUserReqDTO;
import com.im.groupservice.entity.ImGroupMember;
import com.im.groupservice.enums.GroupRoleEnum;
import com.im.groupservice.mapper.GroupMemberMapper;
import com.im.groupservice.service.GroupMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, ImGroupMember> implements GroupMemberService {

    @Override
    public void checkGroupOwner(Long groupId, Long userId) {
        ImGroupMember member = getMember(groupId, userId);
        if (!GroupRoleEnum.OWNER.getCode().equals(member.getRole())) {
            throw new BusinessException("无操作权限，仅群主可执行");
        }
    }

    @Override
    public void checkGroupAdmin(Long groupId, Long userId) {
        ImGroupMember member = getMember(groupId, userId);
        Integer role = member.getRole();
        if (!GroupRoleEnum.OWNER.getCode().equals(role)
                && !GroupRoleEnum.ADMIN.getCode().equals(role)) {
            throw new BusinessException("无操作权限，需管理员以上身份");
        }
    }

    @Override
    public ImGroupMember getMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        ImGroupMember member = getOne(wrapper);
        if (member == null) {
            throw new BusinessException("您不在该群组内");
        }
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteUser(MuteUserReqDTO dto) {
        Long currentUserId = UserContext.getUserId();
        // 权限校验
        checkGroupAdmin(dto.getGroupId(), currentUserId);

        // 不能禁言群主和管理员
        ImGroupMember targetMember = getMember(dto.getGroupId(), dto.getUserId());
        if (GroupRoleEnum.OWNER.getCode().equals(targetMember.getRole())
                || GroupRoleEnum.ADMIN.getCode().equals(targetMember.getRole())) {
            throw new BusinessException("无法禁言管理员及以上身份");
        }

        LambdaUpdateWrapper<ImGroupMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, dto.getGroupId())
                .eq(ImGroupMember::getUserId, dto.getUserId())
                .set(ImGroupMember::getIsMuted, dto.getIsMuted())
                .set(ImGroupMember::getMuteExpireTime, dto.getMuteExpireTime());
        update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setAdmin(Long groupId, Long userId, Integer role) {
        checkGroupOwner(groupId, UserContext.getUserId());

        LambdaUpdateWrapper<ImGroupMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId)
                .set(ImGroupMember::getRole, role);
        update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long groupId, Long userId) {
        checkGroupAdmin(groupId, UserContext.getUserId());

        ImGroupMember member = getMember(groupId, userId);
        if (GroupRoleEnum.OWNER.getCode().equals(member.getRole())) {
            throw new BusinessException("无法移除群主");
        }

        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        remove(wrapper);
    }
}