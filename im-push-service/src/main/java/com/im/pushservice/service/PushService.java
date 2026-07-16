package com.im.pushservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.pushservice.dto.AllUserPushDTO;
import com.im.pushservice.dto.BatchPushDTO;
import com.im.pushservice.dto.SinglePushDTO;
import com.im.pushservice.entity.ImPushTask;

public interface PushService extends IService<ImPushTask> {
    /**
     * 单用户离线推送
     */
    void singlePush(SinglePushDTO dto);

    /**
     * 批量用户推送
     */
    void batchPush(BatchPushDTO dto);

    /**
     * 全员推送（异步执行）
     */
    Long allUserPush(AllUserPushDTO dto);
    /**
     * 归档过期推送记录
     * @param keepDays 保留天数
     * @return 归档条数
     */
    Integer archiveExpiredRecord(Integer keepDays);
}