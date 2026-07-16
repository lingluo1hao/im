package com.im.pushservice.adapter;

import com.im.pushservice.enums.PushChannelEnum;

import java.util.List;

public interface PushChannel {
    /**
     * 获取通道类型
     */
    PushChannelEnum getChannel();

    /**
     * 单设备推送
     */
    boolean push(String deviceToken, String title, String content);

    /**
     * 批量设备推送
     */
    boolean batchPush(List<String> deviceTokens, String title, String content);
}