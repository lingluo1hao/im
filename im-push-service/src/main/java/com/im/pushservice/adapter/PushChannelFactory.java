package com.im.pushservice.adapter;

import com.im.common.exception.BusinessException;
import com.im.pushservice.enums.DeviceTypeEnum;
import com.im.pushservice.enums.PushChannelEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PushChannelFactory {

    private final Map<Integer, PushChannel> channelMap = new ConcurrentHashMap<>();

    public PushChannelFactory(List<PushChannel> channelList) {
        for (PushChannel channel : channelList) {
            channelMap.put(channel.getChannel().getCode(), channel);
        }
    }

    public PushChannel getChannel(Integer channelCode) {
        PushChannel channel = channelMap.get(channelCode);
        if (channel == null) {
            throw new BusinessException(400, "不支持的推送通道");
        }
        return channel;
    }

    /**
     * 根据设备类型自动匹配最优推送通道
     */
    public Integer matchBestChannel(Integer deviceType) {
        // iOS 优先走系统级 APNs
        if (DeviceTypeEnum.IOS.getCode().equals(deviceType)) {
            return PushChannelEnum.APNs.getCode();
        }
        // 安卓设备：兜底走极光，生产环境可按设备品牌细分华为/小米/OPPO
        if (DeviceTypeEnum.ANDROID.getCode().equals(deviceType)) {
            return PushChannelEnum.JPUSH.getCode();
        }
        // Web/PC 端不推送离线通知，返回null
        return null;
    }
}