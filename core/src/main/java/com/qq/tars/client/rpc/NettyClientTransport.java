package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NettyClientTransport {
    private Bootstrap bootstrap;
    private final ServantProxyConfig servantProxyConfig;

    public NettyClientTransport(ServantProxyConfig servantProxyConfig, ChannelHandler channelHandler) {
        this.servantProxyConfig = servantProxyConfig;
        this.channelHander = channelHandler;
    }

    private ChannelHandler channelHander;

    public void init() {
        bootstrap = new Bootstrap();
        EventLoopGroup myEventLoopGroup;
        if (KQueue.isAvailable()) {
            myEventLoopGroup = new KQueueEventLoopGroup(4);
            bootstrap.group(myEventLoopGroup).channel(KQueueSocketChannel.class);
        } else if (Epoll.isAvailable()) {
            myEventLoopGroup = new EpollEventLoopGroup(4);
            bootstrap.group(myEventLoopGroup).channel(EpollSocketChannel.class);
        } else {
            myEventLoopGroup = new NioEventLoopGroup(4);
            bootstrap.group(myEventLoopGroup).channel(NioSocketChannel.class);
        }
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, servantProxyConfig.getConnectTimeout());

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                IdleStateHandler clientIdleHandler =
                        new IdleStateHandler(0, servantProxyConfig.getConnectTimeout(), 0, MILLISECONDS);
                ChannelPipeline p = ch.pipeline();
                p.addLast("encoder", new TarsEncoder("UTF-8"))
                        .addLast("decoder", new TarsDecoder("UTF-8"))
                        .addLast("idle", clientIdleHandler)
                        .addLast("handler", new NettyClientHandler(channelHander, servantProxyConfig))
                ;
            }
        });
    }


    public NettyServantClient connect(String ip, int port) {
        NettyServantClient nettyServantClient = new NettyServantClient(bootstrap.connect(ip, port).channel(), this.servantProxyConfig);
        return nettyServantClient;
    }


}
