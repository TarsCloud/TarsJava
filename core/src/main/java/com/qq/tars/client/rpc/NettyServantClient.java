package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.net.client.Callback;
import com.qq.tars.protocol.tars.TarsInputStream;
import com.qq.tars.protocol.tars.TarsOutputStream;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.ServantResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class NettyServantClient implements RPCClient {
    private Bootstrap bootstrap;
    private ConcurrentHashMap<Channel, Long> channelMap = new ConcurrentHashMap<>();
    private ChannelHandler channelHandler;
    private final ServantProxyConfig servantProxyConfig;

    public NettyServantClient(ServantProxyConfig servantProxyConfig) {
        this.servantProxyConfig = servantProxyConfig;
    }

    protected void init() {
        bootstrap = new Bootstrap();
        EventLoopGroup myEventLoopGroup;
        if (Epoll.isAvailable()) {
            myEventLoopGroup = new EpollEventLoopGroup(12);
            bootstrap.group(myEventLoopGroup).channel(EpollSocketChannel.class);
        } else {
            myEventLoopGroup = new NioEventLoopGroup(12);
            bootstrap.group(myEventLoopGroup).channel(NioSocketChannel.class);
        }
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, servantProxyConfig.getConnectTimeout());
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                IdleStateHandler clientIdleHandler =
                        new IdleStateHandler(0, servantProxyConfig.getConnectTimeout(), 0, MILLISECONDS);
                ChannelPipeline p = ch.pipeline();
                p.addLast("encoder", new TarsEncoder())
                        .addLast("decoder", new TarsDecoder())
                        .addLast("client-idle", clientIdleHandler);
            }
        });
    }


    @Override
    public void reConnect() throws IOException {

    }

    @Override
    public void ensureConnected() throws IOException {

    }

    @Override
    public <T extends ServantResponse> T invokeWithSync(ServantRequest request) throws IOException {
        return null;
    }

    @Override
    public <T extends ServantResponse> void invokeWithAsync(ServantRequest request, Callback<T> callback) throws IOException {

    }

    @Override
    public <T extends ServantResponse> void invokeWithFuture(ServantRequest request, Callback<T> callback) throws IOException {

    }

    public static void main(String[] args) {
        StatInfo statInfo = new StatInfo();
        statInfo.setMasterName("userinfo");
        TarsOutputStream tarsOutputStream = new TarsOutputStream();
        statInfo.writeTo(tarsOutputStream);

        StatInfo statInfo1 = new StatInfo();
        TarsInputStream tarsInputStream = new TarsInputStream(tarsOutputStream.getByteBuffer());
        statInfo1.readFrom(tarsInputStream);

        System.out.println(statInfo1.getMasterName());

    }
}
