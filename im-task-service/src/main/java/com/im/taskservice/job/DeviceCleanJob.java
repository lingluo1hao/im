package com.im.taskservice.job;

import com.im.common.result.R;
import com.im.taskservice.config.TaskProperties;
import com.im.taskservice.feign.DeviceClient;
import com.im.taskservice.service.TaskLogService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCleanJob {

    private final TaskLogService taskLogService;
    private final TaskLockHelper taskLockHelper;
    private final DeviceClient deviceClient;
    private final TaskProperties taskProperties;

    @XxlJob("deviceCleanJob")
    public void execute() {
        String taskCode = "device_clean_job";
        String taskName = "无效设备清理任务";

        if (!taskLockHelper.tryLock(taskCode)) {
            return;
        }

        Long logId = taskLogService.startTask(taskCode, taskName);
        try {
            log.info("开始执行{}", taskName);
            R<Integer> result = deviceClient.cleanExpiredDevice(taskProperties.getDeviceExpireDays());
            log.info("{}执行完成，清理设备数：{}", taskName, result.getData());
            taskLogService.finishTask(logId, true, null);
        } catch (Exception e) {
            log.error("{}执行失败", taskName, e);
            taskLogService.finishTask(logId, false, e.getMessage());
        } finally {
            taskLockHelper.unlock(taskCode);
        }
    }
}