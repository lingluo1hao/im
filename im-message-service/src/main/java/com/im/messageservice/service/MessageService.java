package com.im.messageservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.messageservice.dto.MessagePullDTO;
import com.im.messageservice.dto.MessageRecallDTO;
import com.im.messageservice.dto.MessageSendDTO;
import com.im.messageservice.entity.ImMessage;

import java.util.List;

public interface MessageService extends IService<ImMessage> {
    /**
     * 发送消息：落库+更新会话+未读数+1
     */
    Long sendMessage(MessageSendDTO dto);

    /**
     * 拉取会话历史消息
     */
    List<ImMessage> pullMessage(MessagePullDTO dto);

    /**
     * 撤回消息
     */
    void recallMessage(MessageRecallDTO dto);

    /**
     * 清理过期历史消息
     * @param keepDays 保留天数
     * @return 清理条数
     */
    Integer cleanExpiredMessage(Integer keepDays);

    /**
     * 补发超时离线消息
     * @param maxTimes 最大补发次数
     * @return 补发条数
     */
    Integer resendOfflineMessage(Integer maxTimes);
}