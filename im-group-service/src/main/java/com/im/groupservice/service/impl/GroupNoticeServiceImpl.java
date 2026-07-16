package com.im.groupservice.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.exception.BusinessException;
import com.im.common.context.UserContext;
import com.im.groupservice.dto.GroupNoticeReqDTO;
import com.im.groupservice.entity.ImGroupNotice;
import com.im.groupservice.mapper.GroupNoticeMapper;
import com.im.groupservice.service.GroupMemberService;
import com.im.groupservice.service.GroupNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupNoticeServiceImpl extends ServiceImpl<GroupNoticeMapper, ImGroupNotice> implements GroupNoticeService {

    private final GroupMemberService groupMemberService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishNotice(GroupNoticeReqDTO dto) {
        Long userId = UserContext.getUserId();
        // 权限校验：管理员及以上可发布公告
        groupMemberService.checkGroupAdmin(dto.getGroupId(), userId);

        ImGroupNotice notice = new ImGroupNotice();
        notice.setGroupId(dto.getGroupId());
        notice.setPublisherId(userId);
        notice.setContent(dto.getContent());
        this.save(notice);
        return notice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long groupId, Long noticeId) {
        Long userId = UserContext.getUserId();
        groupMemberService.checkGroupAdmin(groupId, userId);

        ImGroupNotice notice = this.getById(noticeId);
        if (notice == null || !notice.getGroupId().equals(groupId)) {
            throw new BusinessException("公告不存在");
        }
        this.removeById(noticeId);
    }

    @Override
    public List<ImGroupNotice> getNoticeList(Long groupId) {
        Long userId = UserContext.getUserId();
        // 校验是否在群内，防止越权访问
        groupMemberService.getMember(groupId, userId);

        LambdaQueryWrapper<ImGroupNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupNotice::getGroupId, groupId)
                .orderByDesc(ImGroupNotice::getCreateTime);
        return this.list(wrapper);
    }
}