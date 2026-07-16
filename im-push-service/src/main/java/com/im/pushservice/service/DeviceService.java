package com.im.pushservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.im.pushservice.dto.DeviceBindDTO;
import com.im.pushservice.entity.ImUserDevice;

import java.util.List;

public interface DeviceService extends IService<ImUserDevice> {
    /**
     * 绑定设备
     */
    void bindDevice(DeviceBindDTO dto);

    /**
     * 解绑设备
     */
    void unbindDevice(String deviceId);

    /**
     * 查询用户所有有效设备
     */
    List<ImUserDevice> getUserDevices(Long userId);

    /**
     * 清理超期离线设备
     * @param expireDays 离线超期天数
     * @return 清理条数
     */
    Integer cleanExpiredDevice(Integer expireDays);
}