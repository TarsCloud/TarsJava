/**
 * Tencent is pleased to support the open source community by making Tars available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.qq.tars.rpc.netty;

import com.google.common.collect.Maps;
import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.common.util.CommonUtils;
import com.qq.tars.server.config.ServantAdapterConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Sharable
public class NettyServerHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private final Map<String, NettyServerChannel> channels = new ConcurrentHashMap<>();
    private static final Map<Channel, NettyServerChannel> CHANNEL_MAP = Maps.newConcurrentMap();
    private final ChannelHandler handler;
    private final ServantAdapterConfig servantAdapterConfig;

    public Map<String, NettyServerChannel> getChannels() {
        return channels;
    }


    public NettyServerHandler(ServantAdapterConfig servantAdapterConfig, ChannelHandler channelHandler) {
        this.servantAdapterConfig = servantAdapterConfig;
        this.handler = channelHandler;
    }


    static NettyServerChannel getOrAddChannel(Channel ioChannel, ServantAdapterConfig config, ChannelHandler handler) {
        if (ioChannel == null) {
            return null;
        }
        NettyServerChannel channel = CHANNEL_MAP.get(ioChannel);
        if (channel == null) {
            NettyServerChannel nettyChannel = new NettyServerChannel(ioChannel, config, handler);
            channel = CHANNEL_MAP.putIfAbsent(ioChannel, nettyChannel);
            if (channel == null) {
                return nettyChannel;
            }
        }
        return channel;
    }

    public static void removeBrokenChannel(Channel channel) {
        if (channel != null && !channel.isActive()) {
            final NettyServerChannel nettyChannel = CHANNEL_MAP.remove(channel);
            if (null != nettyChannel) {
                logger.info(" channel " + nettyChannel + "  removed");
            } else {
                logger.info("the channel " + nettyChannel + "  null!");
            }
        }

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyServerChannel channel = getOrAddChannel(ctx.channel(), servantAdapterConfig, handler);
        if (channel != null) {
            channels.put(CommonUtils.getIPAndPort((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            handler.connected(channel.getChannel());
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyServerChannel channel = getOrAddChannel(ctx.channel(), servantAdapterConfig, handler);
        try {
            channels.remove(CommonUtils.getIPAndPort((InetSocketAddress) ctx.channel().remoteAddress()));
            handler.disconnected(channel.getChannel());
        } finally {
            removeBrokenChannel(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyServerChannel channel = getOrAddChannel(ctx.channel(), servantAdapterConfig, handler);
        handler.received(channel.getChannel(), msg);
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        NettyServerChannel channel = getOrAddChannel(ctx.channel(), servantAdapterConfig, handler);
        handler.send(channel.getChannel(), msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            NettyServerChannel channel = getOrAddChannel(ctx.channel(), servantAdapterConfig, handler);
            try {
                channel.getChannel().close();
            } finally {
                removeBrokenChannel(ctx.channel());
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        NettyServerChannel channel = getOrAddChannel(ctx.channel(), servantAdapterConfig, handler);
        try {
            handler.caught(channel.getChannel(), cause);
        } finally {
            removeBrokenChannel(ctx.channel());
        }

    }
}
