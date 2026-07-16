package com.im.messageservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 消息实体
@Data
@TableName("im_message")
public class ImMessage {
    @TableId(type = IdType.ASSIGN_ID) // 雪花算法生成，全局时序递增
    private Long id;
    private String conversationId;
    private Integer conversationType;
    private Long senderId;
    private Long targetId;
    private Integer msgType;
    private String content;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}