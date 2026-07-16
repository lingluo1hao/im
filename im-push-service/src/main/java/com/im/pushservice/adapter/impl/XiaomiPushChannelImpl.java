package com.im.pushservice.adapter.impl;


import com.im.pushservice.adapter.PushChannel;
import com.im.pushservice.enums.PushChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class XiaomiPushChannelImpl implements PushChannel {

    @Override
    public PushChannelEnum getChannel() {
        return PushChannelEnum.XIAOMI;
    }

    @Override
    public boolean push(String deviceToken, String title, String content) {
        try {
            // 真实接入：调用小米 Push SDK，配置 packageName + appSecret
            log.info("[小米推送] 单推 token={}, title={}, content={}", deviceToken, title, content);
            return true;
        } catch (Exception e) {
            log.error("[小米推送] 单推失败", e);
            return false;
        }
    }

    @Override
    public boolean batchPush(List<String> deviceTokens, String title, String content) {
        try {
            log.info("[小米推送] 批量推送 数量={}, title={}", deviceTokens.size(), title);
            return true;
        } catch (Exception e) {
            log.error("[小米推送] 批量推送失败", e);
            return false;
        }
    }
}