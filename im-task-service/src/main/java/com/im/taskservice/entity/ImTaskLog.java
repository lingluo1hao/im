package com.im.taskservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// 任务日志实体
@Data
@TableName("im_task_log")
public class ImTaskLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String taskCode;
    private String taskName;
    private Integer status;
    private Long costTime;
    private String failReason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}