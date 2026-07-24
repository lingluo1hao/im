package com.im.nettyserver.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImSessionManager {

    private final ConcurrentHashMap<Long, Map<Integer, Channel>> userChannelsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChannelId, Long> channelUserMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ChannelId, Boolean> channelAuthMap = new ConcurrentHashMap<>();

    public void markAuthenticated(Channel channel, Long userId) {
        channelAuthMap.put(channel.id(), true);
        bind(userId, channel);
    }

    public boolean isAuthenticated(Channel channel) {
        return Boolean.TRUE.equals(channelAuthMap.get(channel.id()));
    }

    public void bind(Long userId, Channel channel) {
        int deviceType = detectDeviceType(channel);
        userChannelsMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(deviceType, channel);
        channelUserMap.put(channel.id(), userId);
    }

    public void unbind(Channel channel) {
        Long userId = channelUserMap.remove(channel.id());
        channelAuthMap.remove(channel.id());
        if (userId != null) {
            Map<Integer, Channel> deviceMap = userChannelsMap.get(userId);
            if (deviceMap != null) {
                deviceMap.values().removeIf(ch -> ch.id().equals(channel.id()));
                if (deviceMap.isEmpty()) {
                    userChannelsMap.remove(userId);
                }
            }
        }
    }

    public Set<Map.Entry<Integer, Channel>> getAllChannels(Long userId) {
        Map<Integer, Channel> deviceMap = userChannelsMap.get(userId);
        return deviceMap != null ? deviceMap.entrySet() : Collections.emptySet();
    }

    public Channel getChannel(Long userId) {
        Map<Integer, Channel> deviceMap = userChannelsMap.get(userId);
        return deviceMap != null ? deviceMap.values().stream().findFirst().orElse(null) : null;
    }

    public Long getUserId(Channel channel) {
        return channelUserMap.get(channel.id());
    }

    public boolean isOnline(Long userId) {
        return userChannelsMap.containsKey(userId);
    }

    public int getOnlineCount() {
        return userChannelsMap.size();
    }

    private int detectDeviceType(Channel channel) {
        return channel.attr(io.netty.util.AttributeKey.valueOf("deviceType"))
                .get() != null ? (int) channel.attr(io.netty.util.AttributeKey.valueOf("deviceType")).get() : 0;
    }
}
