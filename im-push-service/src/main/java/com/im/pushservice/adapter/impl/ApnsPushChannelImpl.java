package com.im.pushservice.adapter.impl;


import com.im.pushservice.adapter.PushChannel;
import com.im.pushservice.enums.PushChannelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ApnsPushChannelImpl implements PushChannel {

    @Override
    public PushChannelEnum getChannel() {
        return PushChannelEnum.APNs;
    }

    @Override
    public boolean push(String deviceToken, String title, String content) {
        try {
            // 真实接入：使用 okhttp + JWT 证书调用 APNs 官方接口
            // 生产环境：api.push.apple.com  开发环境：api.development.push.apple.com
            log.info("[苹果APNs] 单推 token={}, title={}, content={}", deviceToken, title, content);
            return true;
        } catch (Exception e) {
            log.error("[苹果APNs] 单推失败", e);
            return false;
        }
    }

    @Override
    public boolean batchPush(List<String> deviceTokens, String title, String content) {
        try {
            // APNs 官方无批量接口，实际可循环异步调用或使用 HTTP/2 多路复用
            log.info("[苹果APNs] 批量推送 数量={}, title={}", deviceTokens.size(), title);
            return true;
        } catch (Exception e) {
            log.error("[苹果APNs] 批量推送失败", e);
            return false;
        }
    }
}