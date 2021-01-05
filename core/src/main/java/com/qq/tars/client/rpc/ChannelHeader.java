package com.qq.tars.client.rpc;

import io.netty.channel.Channel;

public interface ChannelHeader {
    void connected(Channel channel);

    void disconnected(Channel channel);

    void send(Channel channel, Object message);

    void received(Channel channel, Object message);

    void caught(Channel channel, Throwable exception);

    void destroy();
}
