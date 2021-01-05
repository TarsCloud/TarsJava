package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyChannel {

    private static final Map<Channel, NettyChannel> CHANNEL_MAP = new ConcurrentHashMap<>();
    private final Channel channel;
    private final ServantProxyConfig servantProxyConfig;

    public NettyChannel(Channel channel, ServantProxyConfig servantProxyConfig) {
        this.channel = channel;
        this.servantProxyConfig = servantProxyConfig;

    }

    public Channel getChannel() {
        return channel;
    }

    public ServantProxyConfig getServantProxyConfig() {
        return servantProxyConfig;
    }

    @Override
    public String toString() {
        return "NettyTarsChannel{" +
                "channel=" + channel +
                ", servantProxyConfig=" + servantProxyConfig +
                '}';
    }
}
