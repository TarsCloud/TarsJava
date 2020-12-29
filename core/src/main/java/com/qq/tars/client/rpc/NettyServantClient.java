package com.qq.tars.client.rpc;

import com.qq.tars.client.ServantProxyConfig;
import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class NettyServantClient implements RPCClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyServantClient.class);
    private final Channel channel;
    private final ServantProxyConfig servantProxyConfig;

    public NettyServantClient(Channel channel, ServantProxyConfig servantProxyConfig) {
        this.channel = channel;
        this.servantProxyConfig = servantProxyConfig;
    }


    public void reConnect() throws IOException {
        if (!channel.isActive()) {
            channel.isOpen();
        }

    }

    public void close() throws IOException {
        this.channel.close();
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void ensureConnected() throws IOException {
        if (!this.channel.isOpen() || !this.channel.isActive()) {
            throw new IOException("[Tars] channel is closed!" + this.channel);
        }

    }

    public CompletableFuture<TarsServantResponse> send(ServantRequest request) throws IOException {
        TicketFeature ticketFeature = TicketFeature.createFeature(this.channel, request, servantProxyConfig.getSyncTimeout());
        this.channel.writeAndFlush(request);
        return ticketFeature.thenCompose(obj -> CompletableFuture.completedFuture((TarsServantResponse) obj));

    }

}
