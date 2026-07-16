package com.im.messageservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 会话已读实体
@Data
@TableName("im_conversation_read")
public class ImConversationRead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationId;
    private Long userId;
    private Long lastReadMsgId;
    private LocalDateTime updateTime;
}