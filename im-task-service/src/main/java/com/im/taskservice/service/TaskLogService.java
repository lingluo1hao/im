package com.im.taskservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.taskservice.entity.ImTaskLog;

public interface TaskLogService extends IService<ImTaskLog> {
    /**
     * 开始任务，返回日志ID
     */
    Long startTask(String taskCode, String taskName);

    /**
     * 结束任务，更新状态
     */
    void finishTask(Long logId, boolean success, String failReason);
}