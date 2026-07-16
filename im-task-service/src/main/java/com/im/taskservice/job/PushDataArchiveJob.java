package com.im.taskservice.job;

import com.im.common.result.R;
import com.im.taskservice.config.TaskProperties;
import com.im.taskservice.feign.PushClient;
import com.im.taskservice.service.TaskLogService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushDataArchiveJob {

    private final TaskLogService taskLogService;
    private final TaskLockHelper taskLockHelper;
    private final PushClient pushClient;
    private final TaskProperties taskProperties;

    @XxlJob("pushDataArchiveJob")
    public void execute() {
        String taskCode = "push_data_archive_job";
        String taskName = "推送数据归档任务";

        if (!taskLockHelper.tryLock(taskCode)) {
            return;
        }

        Long logId = taskLogService.startTask(taskCode, taskName);
        try {
            log.info("开始执行{}", taskName);
            R<Integer> result = pushClient.archiveExpiredRecord(taskProperties.getMessageKeepDays());
            log.info("{}执行完成，归档记录数：{}", taskName, result.getData());
            taskLogService.finishTask(logId, true, null);
        } catch (Exception e) {
            log.error("{}执行失败", taskName, e);
            taskLogService.finishTask(logId, false, e.getMessage());
        } finally {
            taskLockHelper.unlock(taskCode);
        }
    }
}