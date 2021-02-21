package com.qq.tars.rpc.netty;

import com.google.common.collect.Maps;
import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.TicketFeature;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Sharable
public class NettyClientHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private static final Map<Channel, NettyChannel> CHANNEL_MAP = Maps.newConcurrentMap();

    public static NettyChannel getOrAddChannel(Channel ioChannel, ServantProxyConfig config) {
        if (ioChannel == null) {
            return null;
        }
        NettyChannel channel = CHANNEL_MAP.get(ioChannel);
        if (channel == null) {
            NettyChannel nettyChannel = new NettyChannel(ioChannel, config);
            channel = CHANNEL_MAP.putIfAbsent(ioChannel, nettyChannel);
            if (channel == null) {
                return nettyChannel;
            }
        }
        return channel;
    }

    public static void removeBrokenChannel(Channel channel) {
        if (channel != null && !channel.isActive()) {
            final NettyChannel nettyChannel = CHANNEL_MAP.remove(channel);
            if (null != nettyChannel) {
                logger.info("Remove channel [" + nettyChannel + "] from NettyChannelManager");
            } else {
                logger.info("the channel [" + nettyChannel + "] already removed");
            }
        }

    }

    private final ServantProxyConfig servantProxyConfig;
    private final ChannelHandler channelHeader;

    public NettyClientHandler(ChannelHandler handler, ServantProxyConfig servantProxyConfig) {
        this.channelHeader = handler;
        this.servantProxyConfig = servantProxyConfig;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            NettyChannel channel = getOrAddChannel(ctx.channel(), servantProxyConfig);
            channelHeader.connected(channel.getChannel());
        } finally {
            removeBrokenChannel(ctx.channel());
        }
    }

    /**
     * 这里收到了消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            NettyChannel nettyChannel = getOrAddChannel(ctx.channel(), servantProxyConfig);
            TarsServantResponse response = (TarsServantResponse) msg;
            if (logger.isDebugEnabled()) {
                System.out.println();
                logger.debug("[tars]netty receive message id is " + response.getRequestId());
            }
            TicketFeature.getFeature(response.getRequestId()).complete(msg);
            channelHeader.received(nettyChannel.getChannel(), msg);
        } finally {
            removeBrokenChannel(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            cause.printStackTrace();
            NettyChannel channel = getOrAddChannel(ctx.channel(), servantProxyConfig);
            channelHeader.caught(channel.getChannel(), cause);
        } finally {
            removeBrokenChannel(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel channel = getOrAddChannel(ctx.channel(), servantProxyConfig);
        try {
            channelHeader.disconnected(channel.getChannel());
        } finally {
            removeBrokenChannel(ctx.channel());
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
            throws Exception {
        super.write(ctx, msg, promise);
        NettyChannel channel = getOrAddChannel(ctx.channel(), servantProxyConfig);
        channelHeader.send(channel.getChannel(), msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    }

}
