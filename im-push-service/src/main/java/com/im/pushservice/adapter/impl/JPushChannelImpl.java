package com.im.pushservice.adapter.impl;

import com.im.pushservice.adapter.PushChannel;
import com.im.pushservice.config.PushProperties;
import com.im.pushservice.enums.PushChannelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JPushChannelImpl implements PushChannel {

    private final PushProperties pushProperties;

    @Override
    public PushChannelEnum getChannel() {
        return PushChannelEnum.JPUSH;
    }

    @Override
    public boolean push(String deviceToken, String title, String content) {
        try {
            // 实际业务替换为极光SDK调用，此处为示例逻辑
            log.info("[极光推送] 单推 token={}, title={}, content={}", deviceToken, title, content);
            return true;
        } catch (Exception e) {
            log.error("[极光推送] 单推失败", e);
            return false;
        }
    }

    @Override
    public boolean batchPush(List<String> deviceTokens, String title, String content) {
        try {
            log.info("[极光推送] 批量推送 数量={}, title={}", deviceTokens.size(), title);
            return true;
        } catch (Exception e) {
            log.error("[极光推送] 批量推送失败", e);
            return false;
        }
    }
}