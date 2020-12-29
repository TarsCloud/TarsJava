package com.qq.tars.client.rpc;

import com.qq.tars.rpc.protocol.ServantRequest;
import com.qq.tars.rpc.protocol.tars.TarsServantResponse;
import io.netty.channel.Channel;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public interface RPCClient {
    /**
     * @throws IOException
     */
    void reConnect() throws IOException;

    void close() throws IOException;


    Channel getChannel();

    /**
     * @throws IOException
     */
    void ensureConnected() throws IOException;

    /**
     * @param request
     * @throws IOException
     */
    CompletableFuture<TarsServantResponse> send(ServantRequest request) throws IOException;


}
