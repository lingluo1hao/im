package com.im.messageservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.context.UserContext;
import com.im.common.exception.BusinessException;
import com.im.messageservice.dto.MessageReadDTO;
import com.im.messageservice.entity.ImConversation;
import com.im.messageservice.entity.ImConversationRead;
import com.im.messageservice.entity.ImMessage;
import com.im.messageservice.enums.ConversationTypeEnum;
import com.im.messageservice.mapper.ConversationMapper;
import com.im.messageservice.mapper.ConversationReadMapper;
import com.im.messageservice.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, ImConversation> implements ConversationService {

    private final ConversationReadMapper conversationReadMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshConversation(ImMessage message) {
        if (ConversationTypeEnum.PRIVATE.getCode().equals(message.getConversationType())) {
            // 单聊：更新发送方和接收方两个会话
            updateOrCreateConversation(message.getSenderId(), message, 0);
            updateOrCreateConversation(message.getTargetId(), message, 1);
        } else {
            // 群聊：调用群服务拉取所有成员，批量更新会话（生产环境建议异步批量处理）
            // 此处简化，实际通过Feign调用im-group-service获取群成员列表
        }

        // 初始化已读位置：发送方默认已读本条消息
        upsertReadPosition(message.getConversationId(), message.getSenderId(), message.getId());
    }

    private void updateOrCreateConversation(Long userId, ImMessage message, Integer unreadAdd) {
        LambdaQueryWrapper<ImConversation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImConversation::getUserId, userId)
                .eq(ImConversation::getConversationId, message.getConversationId());
        ImConversation conversation = this.getOne(wrapper);

        if (conversation == null) {
            conversation = new ImConversation();
            conversation.setUserId(userId);
            conversation.setConversationId(message.getConversationId());
            conversation.setConversationType(message.getConversationType());
            conversation.setTargetId(message.getTargetId());
            conversation.setUnreadCount(unreadAdd);
        } else {
            conversation.setUnreadCount(conversation.getUnreadCount() + unreadAdd);
        }

        conversation.setLastMsgId(message.getId());
        conversation.setLastMsgContent(message.getContent());
        conversation.setLastMsgTime(message.getCreateTime());
        this.saveOrUpdate(conversation);
    }

    @Override
    public void updateLastMsgOnRecall(ImMessage message) {
        LambdaUpdateWrapper<ImConversation> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImConversation::getConversationId, message.getConversationId())
                .eq(ImConversation::getLastMsgId, message.getId())
                .set(ImConversation::getLastMsgContent, "消息已撤回");
        this.update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(MessageReadDTO dto) {
        Long userId = UserContext.getUserId();
        // 更新已读位置
        upsertReadPosition(dto.getConversationId(), userId, dto.getLastReadMsgId());

        // 清零未读数
        LambdaUpdateWrapper<ImConversation> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImConversation::getUserId, userId)
                .eq(ImConversation::getConversationId, dto.getConversationId())
                .set(ImConversation::getUnreadCount, 0);
        this.update(wrapper);
    }

    private void upsertReadPosition(String conversationId, Long userId, Long msgId) {
        LambdaQueryWrapper<ImConversationRead> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImConversationRead::getConversationId, conversationId)
                .eq(ImConversationRead::getUserId, userId);
        ImConversationRead read = conversationReadMapper.selectOne(wrapper);

        if (read == null) {
            read = new ImConversationRead();
            read.setConversationId(conversationId);
            read.setUserId(userId);
            read.setLastReadMsgId(msgId);
            conversationReadMapper.insert(read);
        } else if (msgId > read.getLastReadMsgId()) {
            read.setLastReadMsgId(msgId);
            conversationReadMapper.updateById(read);
        }
    }

    @Override
    public List<ImConversation> listConversation() {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ImConversation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImConversation::getUserId, userId)
                .orderByDesc(ImConversation::getLastMsgTime);
        return this.list(wrapper);
    }

    @Override
    public void checkInConversation(String conversationId, Long userId) {
        LambdaQueryWrapper<ImConversation> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImConversation::getConversationId, conversationId)
                .eq(ImConversation::getUserId, userId);
        if (this.count(wrapper) == 0) {
            throw new BusinessException(403, "无权限访问该会话");
        }
    }
}