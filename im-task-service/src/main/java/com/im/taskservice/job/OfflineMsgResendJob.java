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
public class OfflineMsgResendJob {

    private final TaskLogService taskLogService;
    private final TaskLockHelper taskLockHelper;
    private final MessageClient messageClient;
    private final TaskProperties taskProperties;

    @XxlJob("offlineMsgResendJob")
    public void execute() {
        String taskCode = "offline_msg_resend_job";
        String taskName = "离线消息补发任务";

        if (!taskLockHelper.tryLock(taskCode)) {
            log.info("任务已在其他节点执行，跳过：{}", taskName);
            return;
        }

        Long logId = taskLogService.startTask(taskCode, taskName);
        try {
            log.info("开始执行{}", taskName);
            R<Integer> result = messageClient.resendOfflineMessage(taskProperties.getMaxResendTimes());
            log.info("{}执行完成，补发消息数：{}", taskName, result.getData());
            taskLogService.finishTask(logId, true, null);
        } catch (Exception e) {
            log.error("{}执行失败", taskName, e);
            taskLogService.finishTask(logId, false, e.getMessage());
        } finally {
            taskLockHelper.unlock(taskCode);
        }
    }
}