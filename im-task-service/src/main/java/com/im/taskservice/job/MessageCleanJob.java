package com.im.taskservice.job;

import com.im.common.result.R;
import com.im.taskservice.config.TaskProperties;
import com.im.taskservice.feign.MessageClient;
import com.im.taskservice.service.TaskLogService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCleanJob {

    private final TaskLogService taskLogService;
    private final TaskLockHelper taskLockHelper;
    private final MessageClient messageClient;
    private final TaskProperties taskProperties;

    @XxlJob("messageCleanJob")
    public void execute() {
        String taskCode = "message_clean_job";
        String taskName = "历史消息清理任务";

        // 分布式锁，避免集群重复执行
        if (!taskLockHelper.tryLock(taskCode)) {
            log.info("任务已在其他节点执行，跳过：{}", taskName);
            return;
        }

        Long logId = taskLogService.startTask(taskCode, taskName);
        try {
            log.info("开始执行{}", taskName);
            R<Integer> result = messageClient.cleanExpiredMessage(taskProperties.getMessageKeepDays());
            log.info("{}执行完成，清理消息数：{}", taskName, result.getData());
            taskLogService.finishTask(logId, true, null);
        } catch (Exception e) {
            log.error("{}执行失败", taskName, e);
            taskLogService.finishTask(logId, false, e.getMessage());
        } finally {
            taskLockHelper.unlock(taskCode);
        }
    }
}