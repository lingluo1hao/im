package com.im.pushservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 推送任务实体
@Data
@TableName("im_push_task")
public class ImPushTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskTitle;
    private String taskContent;
    private Integer pushType;
    private String targetIds;
    private Integer status;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}