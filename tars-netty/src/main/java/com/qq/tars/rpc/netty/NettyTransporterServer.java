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

import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.TransporterServer;
import com.qq.tars.common.util.CommonUtils;
import com.qq.tars.common.util.Constants;
import com.qq.tars.server.config.ServantAdapterConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NettyTransporterServer implements TransporterServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyTransporterServer.class);
    private Map<String, NettyServerChannel> remoteChannels;
    private ServerBootstrap bootstrap;
    private io.netty.channel.Channel serverChannel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final ServantAdapterConfig servantAdapterConfig;
    private final ChannelHandler channelHandler;

    private final InetSocketAddress serverInetSocketAddress;

    public NettyTransporterServer(ServantAdapterConfig servantAdapterConfig, ChannelHandler handler) {
        this.servantAdapterConfig = servantAdapterConfig;
        this.channelHandler = handler;
        this.serverInetSocketAddress = new InetSocketAddress(this.servantAdapterConfig.getEndpoint().host(),
                this.servantAdapterConfig.getEndpoint().port());
    }

    public void bind() {
        bootstrap = new ServerBootstrap();
        final ThreadFactory threadFactoryBoss = new DefaultThreadFactory("NettyServerBoss", true);
        final ThreadFactory threadFactoryWorker = new DefaultThreadFactory("NettyServerWorker", true);
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(2, threadFactoryBoss);
            workerGroup = new EpollEventLoopGroup(Constants.DEFAULT_CORE_POOL_SIZE, threadFactoryWorker);
            bootstrap.channel(EpollServerSocketChannel.class);
        } else if (KQueue.isAvailable()) {
            bossGroup = new KQueueEventLoopGroup(2, threadFactoryBoss);
            workerGroup = new KQueueEventLoopGroup(Constants.DEFAULT_CORE_POOL_SIZE, threadFactoryWorker);
            bootstrap.channel(KQueueServerSocketChannel.class);
        } else {
            bossGroup = new NioEventLoopGroup(2, threadFactoryBoss);
            workerGroup = new NioEventLoopGroup(Constants.DEFAULT_CORE_POOL_SIZE, threadFactoryWorker);
            bootstrap.channel(NioServerSocketChannel.class);
        }
        final NettyServerHandler nettyServerHandler = new NettyServerHandler(servantAdapterConfig, channelHandler);
        remoteChannels = nettyServerHandler.getChannels();
        bootstrap.group(bossGroup, workerGroup)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("decoder", new TarsDecoder(StandardCharsets.UTF_8, Boolean.TRUE))
                                .addLast("encoder", new TarsEncoder(StandardCharsets.UTF_8))
                                .addLast("server-idle-handler", new IdleStateHandler(0, 0, 3000, MILLISECONDS))
                                .addLast("handler", nettyServerHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(serverInetSocketAddress);
        channelFuture.syncUninterruptibly();
        serverChannel = channelFuture.channel();

    }


    protected void shutdown() {
        try {
            if (serverChannel != null) {
                serverChannel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<NettyServerChannel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (NettyServerChannel channel : channels) {
                    try {
                        channel.getChannel().close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public Collection<NettyServerChannel> getChannels() {
        final Collection<NettyServerChannel> chs = new HashSet<>();
        for (NettyServerChannel channel : this.remoteChannels.values()) {
            if (channel.getChannel().isActive()) {
                chs.add(channel);
            } else {
                remoteChannels.remove(channel.getChannel().remoteAddress().toString());
            }
        }
        return chs;
    }

    public NettyServerChannel getChannel(InetSocketAddress remoteAddress) {
        return remoteChannels.get(CommonUtils.getIPAndPort(remoteAddress));
    }

    public boolean canHandleIdle() {
        return true;
    }

}
