package com.im.pushservice.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.pushservice.adapter.PushChannel;
import com.im.pushservice.adapter.PushChannelFactory;
import com.im.pushservice.config.PushProperties;
import com.im.pushservice.dto.AllUserPushDTO;
import com.im.pushservice.dto.BatchPushDTO;
import com.im.pushservice.dto.SinglePushDTO;
import com.im.pushservice.entity.ImPushRecord;
import com.im.pushservice.entity.ImPushTask;
import com.im.pushservice.entity.ImUserDevice;
import com.im.pushservice.enums.DeviceTypeEnum;
import com.im.pushservice.enums.PushStatusEnum;
import com.im.pushservice.mapper.PushRecordMapper;
import com.im.pushservice.mapper.PushTaskMapper;
import com.im.pushservice.service.DeviceService;
import com.im.pushservice.service.PushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushServiceImpl extends ServiceImpl<PushTaskMapper, ImPushTask> implements PushService {

    private final DeviceService deviceService;
    private final PushRecordMapper pushRecordMapper;
    private final StringRedisTemplate redisTemplate;
    private final PushProperties pushProperties;
    private final ThreadPoolExecutor pushExecutor;


    private final PushChannelFactory pushChannelFactory;

    @Override
    public void singlePush(SinglePushDTO dto) {
        String idempotentKey = "push:idempotent:" + dto.getBizMsgId() + ":" + dto.getUserId();
        Boolean setIfAbsent = redisTemplate.opsForValue()
                .setIfAbsent(idempotentKey, "1", pushProperties.getIdempotentExpire(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(setIfAbsent)) {
            return;
        }

        List<ImUserDevice> devices = deviceService.getUserDevices(dto.getUserId());
        if (CollUtil.isEmpty(devices)) {
            return;
        }

        // 过滤：只给移动端推送，Web/PC 端不推离线通知
        List<ImUserDevice> mobileDevices = devices.stream()
                .filter(d -> DeviceTypeEnum.ANDROID.getCode().equals(d.getDeviceType())
                        || DeviceTypeEnum.IOS.getCode().equals(d.getDeviceType()))
                .toList();

        for (ImUserDevice device : mobileDevices) {
            Integer channelCode = device.getPushChannel();
            // 兜底：通道为空时重新匹配最优通道
            if (channelCode == null) {
                channelCode = pushChannelFactory.matchBestChannel(device.getDeviceType());
                if (channelCode == null) {
                    continue; // 该设备类型无需推送，直接跳过
                }
            }
            PushChannel channel = pushChannelFactory.getChannel(channelCode);

            boolean success = channel.push(device.getDeviceToken(), dto.getTitle(), dto.getContent());

            ImPushRecord record = new ImPushRecord();
            record.setUserId(dto.getUserId());
            record.setDeviceToken(device.getDeviceToken());
            record.setPushChannel(device.getPushChannel());
            record.setTitle(dto.getTitle());
            record.setContent(dto.getContent());
            record.setBizMsgId(dto.getBizMsgId());
            record.setStatus(success ? PushStatusEnum.SUCCESS.getCode() : PushStatusEnum.FAIL.getCode());
            record.setFailReason(success ? null : "推送通道调用失败");
            pushRecordMapper.insert(record);
        }
    }

    @Override
    public void batchPush(BatchPushDTO dto) {
        // 异步批量推送，避免阻塞接口
        pushExecutor.execute(() -> {
            for (Long userId : dto.getUserIds()) {
                SinglePushDTO singleDTO = new SinglePushDTO();
                singleDTO.setUserId(userId);
                singleDTO.setTitle(dto.getTitle());
                singleDTO.setContent(dto.getContent());
                singlePush(singleDTO);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long allUserPush(AllUserPushDTO dto) {
        // 创建推送任务
        ImPushTask task = new ImPushTask();
        task.setTaskTitle(dto.getTitle());
        task.setTaskContent(dto.getContent());
        task.setPushType(3);
        task.setStatus(0);
        task.setTotalCount(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        this.save(task);

        // 异步分批执行全员推送
        pushExecutor.execute(() -> executeAllPushTask(task.getId(), dto.getTitle(), dto.getContent()));
        return task.getId();
    }

    /**
     * 执行全员推送任务：分页拉取设备，分批推送
     */
    private void executeAllPushTask(Long taskId, String title, String content) {
        try {
            // 更新任务状态为执行中
            ImPushTask task = new ImPushTask();
            task.setId(taskId);
            task.setStatus(1);
            this.updateById(task);

            int pageSize = pushProperties.getAllPushBatchSize();
            long lastId = 0;
            int total = 0, success = 0, fail = 0;

            while (true) {
                // 游标分页拉取设备
                LambdaQueryWrapper<ImUserDevice> wrapper = Wrappers.lambdaQuery();
                wrapper.gt(ImUserDevice::getId, lastId)
                        .eq(ImUserDevice::getIsPushEnabled, 1)
                        .orderByAsc(ImUserDevice::getId)
                        .last("limit " + pageSize);
                List<ImUserDevice> devices = deviceService.list(wrapper);

                if (CollUtil.isEmpty(devices)) {
                    break;
                }

                // 按通道分组批量推送
                Map<Integer, List<String>> tokenGroup = devices.stream()
                        .collect(Collectors.groupingBy(
                                ImUserDevice::getPushChannel,
                                Collectors.mapping(ImUserDevice::getDeviceToken, Collectors.toList())
                        ));

                for (Map.Entry<Integer, List<String>> entry : tokenGroup.entrySet()) {
                    PushChannel channel = pushChannelFactory.getChannel(entry.getKey());
                    boolean batchSuccess = channel.batchPush(entry.getValue(), title, content);
                    if (batchSuccess) {
                        success += entry.getValue().size();
                    } else {
                        fail += entry.getValue().size();
                    }
                    total += entry.getValue().size();
                }

                lastId = devices.getLast().getId();
            }

            // 更新任务最终状态
            task = new ImPushTask();
            task.setId(taskId);
            task.setStatus(2);
            task.setTotalCount(total);
            task.setSuccessCount(success);
            task.setFailCount(fail);
            task.setFinishTime(LocalDateTime.now());
            this.updateById(task);

        } catch (Exception e) {
            log.error("全员推送任务执行失败，taskId={}", taskId, e);
            ImPushTask task = new ImPushTask();
            task.setId(taskId);
            task.setStatus(3);
            task.setFinishTime(LocalDateTime.now());
            this.updateById(task);
        }
    }

    @Override
    public Integer archiveExpiredRecord(Integer keepDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(keepDays);
        int total = 0;
        long lastId = 0;
        int batchSize = 500;

        while (true) {
            LambdaQueryWrapper<ImPushRecord> wrapper = Wrappers.lambdaQuery();
            wrapper.lt(ImPushRecord::getCreateTime, expireTime)
                    .gt(ImPushRecord::getId, lastId)
                    .orderByAsc(ImPushRecord::getId)
                    .last("limit " + batchSize);
            List<ImPushRecord> list = pushRecordMapper.selectList(wrapper);

            if (CollUtil.isEmpty(list)) {
                break;
            }

            List<Long> ids = list.stream().map(ImPushRecord::getId).toList();
            pushRecordMapper.deleteBatchIds(ids);
            total += ids.size();
            lastId = list.get(list.size() - 1).getId();
        }
        return total;
    }
}