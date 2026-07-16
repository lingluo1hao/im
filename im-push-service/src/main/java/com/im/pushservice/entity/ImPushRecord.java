package com.im.pushservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 推送记录实体
@Data
@TableName("im_push_record")
public class ImPushRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long userId;
    private String deviceToken;
    private Integer pushChannel;
    private String title;
    private String content;
    private Long bizMsgId;
    private Integer status;
    private String failReason;
    private LocalDateTime createTime;
}