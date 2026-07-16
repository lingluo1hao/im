package com.im.nettyserver.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImSessionManager {

    /**
     * userId -> Channel 映射
     */
    private final ConcurrentHashMap<Long, Channel> userChannelMap = new ConcurrentHashMap<>();

    /**
     * channelId -> userId 映射
     */
    private final ConcurrentHashMap<ChannelId, Long> channelUserMap = new ConcurrentHashMap<>();

    /**
     * 连接鉴权状态标记
     */
    private final ConcurrentHashMap<ChannelId, Boolean> channelAuthMap = new ConcurrentHashMap<>();

    /**
     * 标记连接已鉴权，并绑定用户会话
     */
    public void markAuthenticated(Channel channel, Long userId) {
        channelAuthMap.put(channel.id(), true);
        bind(userId, channel);
    }

    /**
     * 判断连接是否已完成鉴权
     */
    public boolean isAuthenticated(Channel channel) {
        return Boolean.TRUE.equals(channelAuthMap.get(channel.id()));
    }
    /**
     * 绑定用户与会话
     */
    public void bind(Long userId, Channel channel) {
        userChannelMap.put(userId, channel);
        channelUserMap.put(channel.id(), userId);
    }

    /**
     * 解绑会话
     */
    public void unbind(Channel channel) {
        Long userId = channelUserMap.remove(channel.id());
        if (userId != null) {
            userChannelMap.remove(userId);
        }
    }

    /**
     * 根据用户ID获取本地连接
     */
    public Channel getChannel(Long userId) {
        return userChannelMap.get(userId);
    }

    /**
     * 根据channel获取用户ID
     */
    public Long getUserId(Channel channel) {
        return channelUserMap.get(channel.id());
    }

    /**
     * 判断用户是否在本节点在线
     */
    public boolean isOnline(Long userId) {
        return userChannelMap.containsKey(userId);
    }
}