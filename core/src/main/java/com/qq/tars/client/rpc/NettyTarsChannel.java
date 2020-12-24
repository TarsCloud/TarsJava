package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import io.netty.channel.Channel;

public class NettyTarsChannel {
    private final Channel channel;
    private final ServantProxyConfig servantProxyConfig;

    public NettyTarsChannel(Channel channel, ServantProxyConfig servantProxyConfig) {
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
