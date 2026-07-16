package com.im.messageservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.context.UserContext;
import com.im.common.exception.BusinessException;
import com.im.messageservice.dto.MessagePullDTO;
import com.im.messageservice.dto.MessageRecallDTO;
import com.im.messageservice.dto.MessageSendDTO;
import com.im.messageservice.entity.ImMessage;
import com.im.messageservice.enums.ConversationTypeEnum;
import com.im.messageservice.enums.MessageStatusEnum;
import com.im.messageservice.mapper.MessageMapper;
import com.im.messageservice.service.ConversationService;
import com.im.messageservice.service.MessageService;
import com.im.messageservice.util.ConversationIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, ImMessage> implements MessageService {

    private final ConversationService conversationService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendMessage(MessageSendDTO dto) {
        Long senderId = UserContext.getUserId();
        String conversationId;

        // 1. 生成会话ID
        if (ConversationTypeEnum.PRIVATE.getCode().equals(dto.getConversationType())) {
            conversationId = ConversationIdUtil.buildPrivateId(senderId, dto.getTargetId());
        } else {
            conversationId = ConversationIdUtil.buildGroupId(dto.getTargetId());
        }

        // 2. 插入消息
        ImMessage message = new ImMessage();
        message.setConversationId(conversationId);
        message.setConversationType(dto.getConversationType());
        message.setSenderId(senderId);
        message.setTargetId(dto.getTargetId());
        message.setMsgType(dto.getMsgType());
        message.setContent(dto.getContent());
        message.setStatus(MessageStatusEnum.NORMAL.getCode());
        this.save(message);

        // 3. 更新收发双方会话（单聊）/群成员会话（群聊）
        conversationService.refreshConversation(message);

        return message.getId();
    }

    @Override
    public List<ImMessage> pullMessage(MessagePullDTO dto) {
        Long userId = UserContext.getUserId();
        // 校验是否在会话内
        conversationService.checkInConversation(dto.getConversationId(), userId);

        LambdaQueryWrapper<ImMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImMessage::getConversationId, dto.getConversationId())
                .gt(dto.getLastMsgId() != null && dto.getLastMsgId() > 0,
                        ImMessage::getId, dto.getLastMsgId())
                .orderByDesc(ImMessage::getId)
                .last("limit " + dto.getPageSize());

        // 按ID倒序拉取，返回前反转成正序
        List<ImMessage> list = this.list(wrapper);
        Collections.reverse(list);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recallMessage(MessageRecallDTO dto) {
        Long userId = UserContext.getUserId();
        ImMessage message = this.getById(dto.getMsgId());

        if (message == null) {
            throw new BusinessException(400, "消息不存在");
        }
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(403, "仅发送方可撤回消息");
        }
        if (MessageStatusEnum.RECALLED.getCode().equals(message.getStatus())) {
            throw new BusinessException(400, "消息已撤回");
        }

        // 更新消息状态
        message.setStatus(MessageStatusEnum.RECALLED.getCode());
        message.setContent("消息已撤回");
        this.updateById(message);

        // 更新会话最后消息预览
        conversationService.updateLastMsgOnRecall(message);
    }


    @Override
    public Integer cleanExpiredMessage(Integer keepDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(keepDays);
        int total = 0;
        long lastId = 0;
        int batchSize = 500;

        // 游标分批删除，避免大事务锁表
        while (true) {
            LambdaQueryWrapper<ImMessage> wrapper = Wrappers.lambdaQuery();
            wrapper.lt(ImMessage::getCreateTime, expireTime)
                    .gt(ImMessage::getId, lastId)
                    .orderByAsc(ImMessage::getId)
                    .last("limit " + batchSize);
            List<ImMessage> list = this.list(wrapper);

            if (CollUtil.isEmpty(list)) {
                break;
            }

            List<Long> ids = list.stream().map(ImMessage::getId).toList();
            this.removeByIds(ids);
            total += ids.size();
            lastId = list.get(list.size() - 1).getId();
        }
        return total;
    }

    @Override
    public Integer resendOfflineMessage(Integer maxTimes) {
        // 业务逻辑：扫描近24小时未读的单聊消息，调用推送服务补发离线通知
        // 此处为骨架实现，生产环境可结合补发次数字段、幂等控制完善
        LocalDateTime startTime = LocalDateTime.now().minusHours(24);

        LambdaQueryWrapper<ImMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.gt(ImMessage::getCreateTime, startTime)
                .eq(ImMessage::getConversationType, ConversationTypeEnum.PRIVATE.getCode())
                .eq(ImMessage::getStatus, MessageStatusEnum.NORMAL.getCode());

        // 实际业务：遍历消息，判断接收方是否离线，调用 push-service 补发
        // 此处返回统计值，可按需接入真实补发逻辑
        long count = this.count(wrapper);
        return (int) count;
    }
}