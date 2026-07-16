package com.im.pushservice.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.common.context.UserContext;
import com.im.common.exception.BusinessException;
import com.im.pushservice.adapter.PushChannelFactory;
import com.im.pushservice.dto.DeviceBindDTO;
import com.im.pushservice.entity.ImUserDevice;
import com.im.pushservice.enums.DeviceTypeEnum;
import com.im.pushservice.mapper.UserDeviceMapper;
import com.im.pushservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<UserDeviceMapper, ImUserDevice> implements DeviceService {
    private final PushChannelFactory pushChannelFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindDevice(DeviceBindDTO dto) {
        // 校验设备类型合法性
        boolean validType = Arrays.stream(DeviceTypeEnum.values())
                .anyMatch(e -> e.getCode().equals(dto.getDeviceType()));
        if (!validType) {
            throw new BusinessException(400, "非法的设备类型");
        }

        // 根据设备类型自动匹配最优推送通道
        Integer pushChannel = pushChannelFactory.matchBestChannel(dto.getDeviceType());

        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ImUserDevice> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImUserDevice::getUserId, userId)
                .eq(ImUserDevice::getDeviceId, dto.getDeviceId());
        ImUserDevice device = this.getOne(wrapper);

        if (device == null) {
            device = new ImUserDevice();
            device.setUserId(userId);
            device.setDeviceId(dto.getDeviceId());
        }
        device.setDeviceType(dto.getDeviceType());
        device.setPushChannel(pushChannel); // 自动赋值，无需前端传入
        device.setDeviceToken(dto.getDeviceToken());
        device.setIsPushEnabled(1);
        device.setLastOnlineTime(LocalDateTime.now());
        this.saveOrUpdate(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindDevice(String deviceId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<ImUserDevice> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImUserDevice::getUserId, userId)
                .eq(ImUserDevice::getDeviceId, deviceId);
        this.remove(wrapper);
    }

    @Override
    public List<ImUserDevice> getUserDevices(Long userId) {
        LambdaQueryWrapper<ImUserDevice> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImUserDevice::getUserId, userId)
                .eq(ImUserDevice::getIsPushEnabled, 1);
        return this.list(wrapper);
    }

    @Override
    public Integer cleanExpiredDevice(Integer expireDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
        int total = 0;
        long lastId = 0;
        int batchSize = 500;

        while (true) {
            LambdaQueryWrapper<ImUserDevice> wrapper = Wrappers.lambdaQuery();
            wrapper.lt(ImUserDevice::getLastOnlineTime, expireTime)
                    .gt(ImUserDevice::getId, lastId)
                    .orderByAsc(ImUserDevice::getId)
                    .last("limit " + batchSize);
            List<ImUserDevice> list = this.list(wrapper);

            if (CollUtil.isEmpty(list)) {
                break;
            }

            List<Long> ids = list.stream().map(ImUserDevice::getId).toList();
            this.removeByIds(ids);
            total += ids.size();
            lastId = list.get(list.size() - 1).getId();
        }
        return total;
    }
}