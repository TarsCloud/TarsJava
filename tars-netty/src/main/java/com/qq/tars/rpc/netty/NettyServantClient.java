package com.qq.tars.rpc.netty;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.client.rpc.ChannelHandler;
import com.qq.tars.client.rpc.*;
import com.qq.tars.rpc.common.Url;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NettyServantClient implements RPCClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyServantClient.class);
    private volatile Channel channel;
    private final ServantProxyConfig servantProxyConfig;
    private final Url url;
    private volatile AtomicBoolean isClosed = new AtomicBoolean(false);
    private static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 8, 32);
    private static final EventLoopGroup EVENT_LOOP_GROUP = getNioEventLoopGroup();
    private Bootstrap bootstrap;
    private final ChannelHandler channelHandler;

    public NettyServantClient(Url url, ServantProxyConfig servantProxyConfig, ChannelHandler clientHandler) {
        this.channelHandler = clientHandler;
        this.servantProxyConfig = servantProxyConfig;
        this.url = url;
        init();
        connect();
    }


    private static EventLoopGroup getNioEventLoopGroup() {
        ThreadFactory threadFactory = new DefaultThreadFactory("netty-client-worker", true);
        if (Epoll.isAvailable()) {
            return new EpollEventLoopGroup(DEFAULT_IO_THREADS, threadFactory);
        } else if (KQueue.isAvailable()) {
            return new KQueueEventLoopGroup(DEFAULT_IO_THREADS, threadFactory);
        } else {
            return new NioEventLoopGroup(DEFAULT_IO_THREADS, threadFactory);
        }
    }

    public void init() {
        bootstrap = new Bootstrap();
        bootstrap.group(EVENT_LOOP_GROUP);
        if (Epoll.isAvailable()) {
            bootstrap.channel(EpollSocketChannel.class);
        } else if (KQueue.isAvailable()) {
            bootstrap.channel(KQueueSocketChannel.class);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, NettyServantClient.this.servantProxyConfig.isTcpNoDelay())
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, servantProxyConfig.getConnectTimeout());

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                IdleStateHandler clientIdleHandler =
                        new IdleStateHandler(0, servantProxyConfig.getConnectTimeout(), 0, MILLISECONDS);
                ChannelPipeline p = ch.pipeline();
                p.addLast("encoder", new TarsEncoder(Charset.forName(servantProxyConfig.getCharsetName())))
                        .addLast("decoder", new TarsDecoder(Charset.forName(servantProxyConfig.getCharsetName())))
                        .addLast("idle", clientIdleHandler)
                        .addLast("handler", new NettyClientHandler(NettyServantClient.this.channelHandler, servantProxyConfig))
                ;
            }
        });
    }


    public NettyServantClient connect() {
        ChannelFuture ioChannelFuture = bootstrap.connect(url.getHost(), url.getPort());
        try {
            boolean ret = ioChannelFuture.awaitUninterruptibly(this.servantProxyConfig.getConnectTimeout(), MILLISECONDS);
            if (ret && ioChannelFuture.isSuccess()) {
                Channel newChannel = ioChannelFuture.channel();
                try {
                    final Channel oldChannel = NettyServantClient.this.channel;
                    if (oldChannel != null) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("[Tars] reconnect new channel");
                            }
                            oldChannel.close();
                        } finally {
                            NettyClientHandler.removeBrokenChannel(oldChannel);
                        }
                    }
                } finally {
                    if (NettyServantClient.this.isClosed.get()) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("[tars]close channel");
                            }
                            newChannel.close();
                        } finally {
                            NettyServantClient.this.channel = null;
                            NettyClientHandler.removeBrokenChannel(newChannel);
                        }
                    } else {
                        NettyServantClient.this.channel = newChannel;
                    }
                }
            } else if (ioChannelFuture.cause() != null) {
                throw new RuntimeException("[tars]cannot connect server");
            } else {
                throw new RuntimeException("[tars] invoke timeout");
            }
        } finally {
            // just add new valid channel to NettyChannel's cache
            if (!isConnected()) {
                //future.cancel(true);
            }
        }
        return this;
    }

    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null) {
            return false;
        }
        return isClosed.get();
    }

    public void reConnect() throws IOException {
        if (!channel.isActive()) {
            channel.isOpen();
        }

    }

    public void close() throws IOException {
        this.isClosed.set(true);
        this.channel.close();
    }

    @Override
    public Channel getChannel() {
        return this.channel;
    }

    public void ensureConnected() throws IOException {
        if (!this.channel.isOpen() || !this.channel.isActive()) {
            throw new IOException("[Tars] channel is closed!" + this.channel);
        }

    }


    public CompletableFuture<Response> send(Request request) {
        TicketFeature ticketFeature = TicketFeature.createFeature(this.channel, request, servantProxyConfig.getSyncTimeout());
        this.channel.writeAndFlush(request);
        return ticketFeature.thenCompose(obj -> CompletableFuture.completedFuture((TarsServantResponse) obj));

    }

}
