package com.im.pushservice.adapter.impl;

import com.im.pushservice.adapter.PushChannel;
import com.im.pushservice.enums.PushChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class HuaweiPushChannelImpl implements PushChannel {

    @Override
    public PushChannelEnum getChannel() {
        return PushChannelEnum.HUAWEI;
    }

    @Override
    public boolean push(String deviceToken, String title, String content) {
        try {
            // 真实接入：调用华为 Push SDK 单发接口
            // 需引入 com.huawei.hms:push 依赖，配置 appId/appSecret
            log.info("[华为推送] 单推 token={}, title={}, content={}", deviceToken, title, content);
            return true;
        } catch (Exception e) {
            log.error("[华为推送] 单推失败", e);
            return false;
        }
    }

    @Override
    public boolean batchPush(List<String> deviceTokens, String title, String content) {
        try {
            // 真实接入：调用华为推送批量接口，单次最多支持 1000 个 token
            log.info("[华为推送] 批量推送 数量={}, title={}", deviceTokens.size(), title);
            return true;
        } catch (Exception e) {
            log.error("[华为推送] 批量推送失败", e);
            return false;
        }
    }
}