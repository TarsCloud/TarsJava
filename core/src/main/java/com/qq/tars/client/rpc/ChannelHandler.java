package com.qq.tars.client.rpc;

import io.netty.channel.Channel;

public interface ChannelHandler {
    void connected(Channel channel);

    /**
     * @param channel
     */
    void disconnected(Channel channel);

    /**
     * @param channel
     * @param message
     */
    void send(Channel channel, Object message);

    /**
     * @param channel
     * @param message
     */
    void received(Channel channel, Object message);

    /**
     * @param channel
     * @param exception
     */
    void caught(Channel channel, Throwable exception);

    /**
     *
     */
    void destroy();
}
