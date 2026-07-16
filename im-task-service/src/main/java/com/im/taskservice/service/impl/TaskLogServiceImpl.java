package com.im.taskservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.taskservice.entity.ImTaskLog;
import com.im.taskservice.mapper.TaskLogMapper;
import com.im.taskservice.service.TaskLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskLogServiceImpl extends ServiceImpl<TaskLogMapper, ImTaskLog> implements TaskLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startTask(String taskCode, String taskName) {
        ImTaskLog log = new ImTaskLog();
        log.setTaskCode(taskCode);
        log.setTaskName(taskName);
        log.setStatus(1);
        log.setStartTime(LocalDateTime.now());
        this.save(log);
        return log.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishTask(Long logId, boolean success, String failReason) {
        ImTaskLog log = new ImTaskLog();
        log.setId(logId);
        log.setStatus(success ? 2 : 3);
        log.setEndTime(LocalDateTime.now());
        log.setFailReason(failReason);
        // 计算耗时
        ImTaskLog origin = this.getById(logId);
        if (origin != null) {
            long cost = Duration.between(origin.getStartTime(), LocalDateTime.now()).toMillis();
            log.setCostTime(cost);
        }
        this.updateById(log);
    }
}