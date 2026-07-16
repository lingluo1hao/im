package com.im.messageservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.messageservice.dto.MessageReadDTO;
import com.im.messageservice.entity.ImConversation;
import com.im.messageservice.entity.ImMessage;

import java.util.List;

public interface ConversationService extends IService<ImConversation> {
    /**
     * 消息发送后刷新会话
     */
    void refreshConversation(ImMessage message);

    /**
     * 消息撤回后更新会话最后消息
     */
    void updateLastMsgOnRecall(ImMessage message);

    /**
     * 标记会话已读
     */
    void markRead(MessageReadDTO dto);

    /**
     * 拉取用户会话列表
     */
    List<ImConversation> listConversation();

    /**
     * 校验用户是否在会话内
     */
    void checkInConversation(String conversationId, Long userId);
}