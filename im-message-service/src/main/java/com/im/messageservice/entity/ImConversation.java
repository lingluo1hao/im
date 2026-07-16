package com.im.messageservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 会话实体
@Data
@TableName("im_conversation")
public class ImConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String conversationId;
    private Integer conversationType;
    private Long targetId;
    private Long lastMsgId;
    private String lastMsgContent;
    private Integer unreadCount;
    private LocalDateTime lastMsgTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}